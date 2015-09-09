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
package it.alidays.mapengine.core.fetch;

import it.alidays.mapengine.core.fetch.function.FunctionFactory;
import it.alidays.mapengine.core.fetch.function.FunctionFactoryException;
import it.alidays.mapengine.enginedirectives.fetch.Bind;
import it.alidays.mapengine.enginedirectives.fetch.Entity;
import it.alidays.mapengine.enginedirectives.fetch.Fetch;
import it.alidays.mapengine.enginedirectives.fetch.ForEach;
import it.alidays.mapengine.util.PerformanceUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fetcher {

	private static final Logger logger = LoggerFactory.getLogger(Fetcher.class);

	private static String convertToXPath(String value, Boolean root) {
		StringBuilder result = new StringBuilder();

		String[] pathElements = value.split("/");
		for (int i = 0; i < pathElements.length; i++) {
			String pathElement = pathElements[i];
			if (i == (pathElements.length - 1) && pathElement.charAt(0) == '@') {
				result.append(String.format("/@*[name() = '%s']", pathElement.substring(1)));
			}
			else {
				if (root && i == 0) {
					result.append(String.format("///%s", pathElement));
				}
				else {
					result.append(String.format("/*[name() = '%s']", pathElement));
				}
			}
		}
		result = result.deleteCharAt(0);

		return result.toString();
	}

	private final ExecutorService executor;
	private final String basePath;
	private final Map<String, ToEntityMethod> toEntityMap;

	public Fetcher(Fetch fetch) throws FetcherException {
		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.basePath = convertToXPath(fetch.getBasePath(), true);
		this.toEntityMap = new HashMap<>();

		if (fetch.getEntities().isEmpty()) {
			throw new FetcherException("At least one 'to-entity' node is required");
		}
		else {
			for (Entity toEntity : fetch.getEntities()) {
				String entityName = toEntity.getName();
				ToEntityMethod toEntityMethod = null;

				if (toEntity.getForEach() != null) {
					ForEach forEach = toEntity.getForEach();

					toEntityMethod = new ForEachMethod(convertToXPath(forEach.getValue(), false));

					if (forEach.getBindings().isEmpty()) {
						throw new FetcherException(String.format("No bind found for to-entity '%s'", entityName));
					}
					else {
						for (Bind bind : forEach.getBindings()) {
							((ForEachMethod)toEntityMethod).addBinder(bind.getAttribute(), createBinder(bind));
						}
					}
				}
				else {
					throw new FetcherException(String.format("ToEntityMethod not found for to-entity '%s'", entityName));
				}

				this.toEntityMap.put(entityName, toEntityMethod);
			}
		}
	}

	public void shutdown() {
		this.executor.shutdownNow();
	}

	public Map<String, List<Map<String, Object>>> run(InputStream inputStream, UUID vuid) throws FetcherException {
		logger.info("Fetcher started...");
		PerformanceUtils.notifyStart(Fetcher.class, "run", vuid);

		Map<String, List<Map<String, Object>>> result = new HashMap<>();

		try {
			PerformanceUtils.notifyStart(Fetcher.class, "run(Parse XML)", vuid);
			final SAXReader reader = new SAXReader();
			final Document document = reader.read(inputStream);
			PerformanceUtils.notifyEnd(Fetcher.class, "run(Parse XML)", vuid, logger);

			PerformanceUtils.notifyStart(Fetcher.class, "run(Get BaseNode)", vuid);
			Element baseNode = (Element)document.selectSingleNode(this.basePath);
			PerformanceUtils.notifyEnd(Fetcher.class, "run(Get BaseNode)", vuid, logger);

			PerformanceUtils.notifyStart(Fetcher.class, "run(Fetch)", vuid);

			ExecutorCompletionService<FetchEntityResult> service = new ExecutorCompletionService<>(this.executor);
			int tasks = 0;
			for (String entity : this.toEntityMap.keySet()) {
				tasks++;
				service.submit(new FetchTask(entity, this.toEntityMap.get(entity), baseNode, vuid));
			}
			while (tasks > 0) {
				FetchEntityResult fetchEntityResult = service.take().get();
				result.put(fetchEntityResult.getEntity(), fetchEntityResult.getResult());
				tasks--;
			}

			PerformanceUtils.notifyEnd(Fetcher.class, "run(Fetch)", vuid, logger);
		}
		catch (Exception e) {
			throw new FetcherException(e);
		}

		PerformanceUtils.notifyEnd(Fetcher.class, "run", vuid, logger);
		logger.info("Fetcher completed");

		return result;
	}

	private Binder createBinder(Bind bind) throws FetcherException {
		Binder result = null;

		if (FunctionFactory.isFunction(bind.getValue())) {
			try {
				result = new FunctionBinder(FunctionFactory.makeFunction(bind.getValue()));
			}
			catch (FunctionFactoryException ffe) {
				throw new FetcherException("Failed to create function", ffe);
			}
		}
		else {
			result = new PathBinder(convertToXPath(bind.getValue(), false));
		}

		return result;
	}

}
