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
package it.alidays.mapengine.core.fetch.function;

import it.alidays.mapengine.configuration.Configuration;
import it.alidays.mapengine.configuration.FetchFunction;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionFactory {

	private static final Logger logger = LoggerFactory.getLogger(FunctionFactory.class);
	private static final String FUNCTION_PREFIX = "#";
	private static final Pattern FUNCTION_PATTERN = Pattern.compile("#(?<name>[a-zA-Z]+)\\((?<value>[a-zA-Z0-9'/\\.]*)\\)");
	private static final Map<String, Class<Function>> FUNCTION_MAP = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static void initialize(Configuration configuration) throws FunctionFactoryException {
		logger.info("Initializing function factory...");
		
		for (FetchFunction fetchFunction : configuration.getFetchFunctions()) {
			try {
				FUNCTION_MAP.put(fetchFunction.getName(), (Class<Function>)Class.forName(fetchFunction.getClass_()));
				logger.info("\tAdded function '{}'", fetchFunction.getName());
			}
			catch (ClassNotFoundException cnfe) {
				throw new FunctionFactoryException(String.format("\tFailed to add the function '%s'", fetchFunction.getName()), cnfe);
			}
		}
		
		logger.info("Function factory successfully started");
	}
	
	public static Boolean isFunction(String value) {
		return value.startsWith(FUNCTION_PREFIX);
	}
	
	public static Function makeFunction(String value) throws FunctionFactoryException {
		Function result = null;
		
		Matcher matcher = FUNCTION_PATTERN.matcher(value);
		if (matcher.matches()) {
			Class<Function> function = FUNCTION_MAP.get(matcher.group("name"));
			if (function != null) {
				try {
					result = function.getConstructor(String.class).newInstance(matcher.group("value"));
				}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new FunctionFactoryException(String.format("\tFailed to instantiate the function '%s'", matcher.group("name")), e);
				}
			}
		}
		
		return result;
	}
	
	private FunctionFactory() {}
	
}
