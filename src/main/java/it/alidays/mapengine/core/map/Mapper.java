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
package it.alidays.mapengine.core.map;

import it.alidays.mapengine.core.database.DatabaseManager;
import it.alidays.mapengine.enginedirectives.map.Map;
import it.alidays.mapengine.enginedirectives.map.Retrieve;
import it.alidays.mapengine.util.PerformanceUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mapper {

	private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

	private final DatabaseManager databaseManager;
	private final AggregatorFactory aggregatorFactory;
	private final java.util.Map<String, RetrieveHandler> retrieveHandlerMap;
	private final ExecutorService executor;

	public Mapper(Map map, DatabaseManager databaseManager) throws MapperException {
		this.databaseManager = databaseManager;
		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		try {
			this.aggregatorFactory = (AggregatorFactory)Class.forName(map.getAggregatorFactory()).newInstance();
		}
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new MapperException("Failed to istantiate the aggregator factory", e);
		}

		this.retrieveHandlerMap = new LinkedHashMap<>();
		for (Retrieve retrieve : map.getRetrieves()) {
			try {
				AbstractRetrieve<?> abstractRetrieve = (AbstractRetrieve<?>)Class.forName(String.format("%s.%sRetrieve", map.getMapPackage(), retrieve.getId())).getConstructor(String.class).newInstance(retrieve.getId());
				this.retrieveHandlerMap.put(retrieve.getId(), new RetrieveHandler(abstractRetrieve, retrieve.getContent()));
				logger.info("Added retrieve with id '{}'", retrieve.getId());
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				throw new MapperException(String.format("Failed to istantiate the retrieve with id %s", retrieve.getId()), e);
			}
		}
	}

	public void shutdown() {
		this.executor.shutdownNow();
	}

	public Object map(UUID vuid) throws MapperException {
		Object result = null;

		logger.info("Mapper started...");
		PerformanceUtils.notifyStart(Mapper.class, "map", vuid);

		Aggregator aggregator = this.aggregatorFactory.make();

		try (Connection connection = this.databaseManager.getConnection()) {
			// Esecuzione parallela query
			Queue<Future<RetrieveResult>> queryFutures = new ArrayDeque<>();
			for (String id : this.retrieveHandlerMap.keySet()) {
				RetrieveTask task = new RetrieveTask(id, this.retrieveHandlerMap.get(id), connection, vuid);
				queryFutures.add(this.executor.submit(task));
			}

			try {
				do {
					Future<RetrieveResult> queryFuture = queryFutures.poll();
					RetrieveResult retrieveResult = queryFuture.get();
					PerformanceUtils.notifyStart(Mapper.class, String.format("map(aggregate->%s)", retrieveResult.getId()), vuid);
					aggregator.notifyRetrieveResult(retrieveResult.getId(), retrieveResult.getResult());
					PerformanceUtils.notifyEnd(Mapper.class, String.format("map(aggregate->%s)", retrieveResult.getId()), vuid, logger);
				} while (!queryFutures.isEmpty());
			}
			catch (InterruptedException | ExecutionException | AggregatorException e) {
				throw new MapperException(e);
			}
		}
		catch (SQLException sqle) {
			throw new MapperException(sqle);
		}

		PerformanceUtils.notifyStart(Mapper.class, "map(getMapResult)", vuid);
		try {
			result = aggregator.getMapResult();
		}
		catch (AggregatorException ae) {
			throw new MapperException(ae);
		}
		PerformanceUtils.notifyEnd(Mapper.class, "map(getMapResult)", vuid, logger);

		PerformanceUtils.notifyEnd(Mapper.class, "map", vuid, logger);
		logger.info("Mapper completed");

		return result;
	}

}
