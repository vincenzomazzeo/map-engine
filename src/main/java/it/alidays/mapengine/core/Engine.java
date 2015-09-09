/*
 * Copyright 2015 Alidays S.p.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.alidays.mapengine.core;

import it.alidays.mapengine.configuration.Configuration;
import it.alidays.mapengine.configuration.Database;
import it.alidays.mapengine.core.database.DatabaseManager;
import it.alidays.mapengine.core.fetch.Fetcher;
import it.alidays.mapengine.core.fetch.FetcherException;
import it.alidays.mapengine.core.fetch.function.FunctionFactory;
import it.alidays.mapengine.core.fetch.function.FunctionFactoryException;
import it.alidays.mapengine.core.map.Mapper;
import it.alidays.mapengine.core.map.MapperException;
import it.alidays.mapengine.core.schema.InsertTask;
import it.alidays.mapengine.core.schema.SchemaHandler;
import it.alidays.mapengine.core.schema.SchemaHandlerException;
import it.alidays.mapengine.core.schema.converter.TypeConverterFactory;
import it.alidays.mapengine.core.schema.converter.TypeConverterFactoryException;
import it.alidays.mapengine.enginedirectives.EngineDirectives;
import it.alidays.mapengine.util.PerformanceUtils;
import it.alidays.mapengine.util.ResourceRetriever;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {

	private static final Logger logger = LoggerFactory.getLogger(Engine.class);

	private final Boolean debug;
	private final SchemaHandler schemaHandler;
	private final Fetcher fetcher;
	private final Mapper mapper;
	private final ExecutorService executor;

	public Engine(InputStream engineDirectivesSource) throws EngineException, FetcherException {
		logger.info("Starting engine...");
		UUID vuid = UUID.randomUUID();
		
		PerformanceUtils.notifyStart(Engine.class, "<init>", vuid);
		/*****************
		 * CONFIGURATION *
		 *****************/
		Configuration configuration;
		try {
			configuration = ResourceRetriever.loadConfiguration();
		}
		catch (JAXBException jaxbe) {
			throw new EngineException("Failed to load configuration", jaxbe);
		}
		/********************
		 * FUNCTION FACTORY *
		 ********************/
		try {
			FunctionFactory.initialize(configuration);
		}
		catch (FunctionFactoryException ffe) {
			throw new EngineException("Failed initialize FunctionFactory", ffe);
		}
		/**************************
		 * TYPE CONVERTER FACTORY *
		 **************************/
		try {
			TypeConverterFactory.initialize(configuration);
		}
		catch (TypeConverterFactoryException tcfe) {
			throw new EngineException("Failed initialize TypeConverterFactory", tcfe);
		}
		/*********************
		 * ENGINE DIRECTIVES *
		 *********************/
		EngineDirectives engineDirectives;
		try {
			engineDirectives = ResourceRetriever.loadEngineDirectives(engineDirectivesSource);
		}
		catch (JAXBException jaxbe) {
			throw new EngineException("Failed to load engine directives", jaxbe);
		}

		this.debug = engineDirectives.getDebug();
		if (this.debug) {
			logger.info("**************");
			logger.info("* DEBUG MODE *");
			logger.info("**************");
		}

		Database database = this.debug ? configuration.getPersistence().getDebug() : configuration.getPersistence().getProduction();
		DatabaseManager databaseManager = null;
		try {
			databaseManager = new DatabaseManager(database.getUrl(), database.getUser(), database.getPassword());
		}
		catch (SQLException sqle) {
			throw new EngineException("Failed to initialize database manager", sqle);
		}

		try {
			this.schemaHandler = new SchemaHandler(engineDirectives.getFetch().getEntities(), databaseManager);
		}
		catch (SchemaHandlerException she) {
			throw new EngineException("Failed to initialize schema handler", she);
		}
		this.fetcher = new Fetcher(engineDirectives.getFetch());
		try {
			this.mapper = new Mapper(engineDirectives.getMap(), databaseManager);
		}
		catch (MapperException me) {
			throw new EngineException("Failed to initialize mapper", me);
		}

		try {
			PerformanceUtils.notifyStart(Engine.class, "<init>(Schema creator)", vuid);
			this.schemaHandler.create();
			PerformanceUtils.notifyEnd(Engine.class, "<init>(Schema creator)", vuid, logger);
		}
		catch (SchemaHandlerException sce) {
			throw new EngineException("Failed to create schema", sce);
		}

		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		PerformanceUtils.notifyEnd(Engine.class, "<init>", vuid, logger);
		logger.info("Engine successfully started");
	}

	public Object run(InputStream inputStream) throws FetcherException, SchemaHandlerException, MapperException {
		Object result = null;

		UUID vuid = UUID.randomUUID();

		logger.info("Running ({})", vuid.toString());
		PerformanceUtils.notifyStart(Engine.class, "run", vuid);

		PerformanceUtils.notifyStart(Engine.class, "run(Fetcher)", vuid);
		Map<String, List<Map<String, Object>>> fetcherResult = this.fetcher.run(inputStream, vuid);
		PerformanceUtils.notifyEnd(Engine.class, "run(Fetcher)", vuid, logger);

		Queue<Future<Void>> insertFutures = new ArrayDeque<>();
		PerformanceUtils.notifyStart(Engine.class, "run(DB insert)", vuid);
		for (String entity : fetcherResult.keySet()) {
			InsertTask task = new InsertTask(this.schemaHandler, entity, fetcherResult.get(entity), vuid);
			insertFutures.offer(this.executor.submit(task));
		}

		try {
    		do {
    			insertFutures.poll().get();
    		} while (!insertFutures.isEmpty());
		}
		catch (ExecutionException ee) {
			throw (SchemaHandlerException)ee.getCause();
		}
		catch (InterruptedException ie) {}
		PerformanceUtils.notifyEnd(Engine.class, "run(DB insert)", vuid, logger);

		PerformanceUtils.notifyStart(Engine.class, "run(Mapper)", vuid);
		result = this.mapper.map(vuid);
		PerformanceUtils.notifyEnd(Engine.class, "run(Mapper)", vuid, logger);

		PerformanceUtils.notifyStart(Engine.class, "run(DB clean)", vuid);
		if (this.debug) {
			logger.info("DB not cleaned (debug mode)");
		}
		else {
			for (String entity : fetcherResult.keySet()) {
				this.schemaHandler.delete(entity, vuid);
			}
		}
		PerformanceUtils.notifyEnd(Engine.class, "run(DB clean)", vuid, logger);

		PerformanceUtils.notifyEnd(Engine.class, "run", vuid, logger);
		logger.info("Completed {}", vuid.toString());

		return result;
	}

	public void shutdown() {
		logger.info("Shutting down...");
		
		this.fetcher.shutdown();
		this.mapper.shutdown();
		this.schemaHandler.shutdown();
		this.executor.shutdownNow();
		
		logger.info("Shutted down");
	}
	
}
