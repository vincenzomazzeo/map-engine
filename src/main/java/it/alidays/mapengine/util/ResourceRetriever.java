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
package it.alidays.mapengine.util;

import it.alidays.mapengine.configuration.Configuration;
import it.alidays.mapengine.enginedirectives.EngineDirectives;

import java.io.InputStream;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceRetriever {

	private static final Logger logger = LoggerFactory.getLogger(ResourceRetriever.class);

	public static Configuration loadConfiguration() throws JAXBException {
		Configuration result = null;

		UUID vuid = UUID.randomUUID();
		
		logger.info("Loading configuration...");
		PerformanceUtils.notifyStart(ResourceRetriever.class, "loadConfiguration", vuid);

		JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		result = (Configuration)unmarshaller.unmarshal(Configuration.class.getClassLoader().getResourceAsStream("configuration.xml"));

		PerformanceUtils.notifyEnd(ResourceRetriever.class, "loadConfiguration", vuid, logger);
		logger.info("Configuration successfully loaded");

		return result;
	}
	
	public static EngineDirectives loadEngineDirectives(InputStream engineDirectivesSource) throws JAXBException {
		EngineDirectives result = null;
		
		UUID vuid = UUID.randomUUID();
		
		logger.info("Loading engine directives...");
		PerformanceUtils.notifyStart(ResourceRetriever.class, "loadEngineDirectives", vuid);

		JAXBContext jaxbContext = JAXBContext.newInstance(EngineDirectives.class);
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		result = (EngineDirectives)unmarshaller.unmarshal(engineDirectivesSource);

		PerformanceUtils.notifyEnd(ResourceRetriever.class, "loadEngineDirectives", vuid, logger);
		logger.info("Engine directives successfully loaded");
		
		return result;
	}
	
	private ResourceRetriever() {
	}

}
