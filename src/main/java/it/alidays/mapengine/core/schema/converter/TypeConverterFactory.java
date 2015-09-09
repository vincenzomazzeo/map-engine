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
package it.alidays.mapengine.core.schema.converter;

import it.alidays.mapengine.configuration.Configuration;
import it.alidays.mapengine.configuration.DatabaseTypeConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeConverterFactory {

	private static final Logger logger = LoggerFactory.getLogger(TypeConverterFactory.class);
	private static final Map<String, Class<AbstractTypeConverter>> TYPE_CONVERTER_MAP = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static void initialize(Configuration configuration) throws TypeConverterFactoryException {
		logger.info("Initializing type converter factory...");

		for (DatabaseTypeConverter databaseTypeConverter : configuration.getDatabaseTypeConverters()) {
			try {
				TYPE_CONVERTER_MAP.put(databaseTypeConverter.getType(), (Class<AbstractTypeConverter>)Class.forName(databaseTypeConverter.getClass_()));
				logger.info("\tAdded database type converter for {} type", databaseTypeConverter.getType());
			}
			catch (ClassNotFoundException cnfe) {
				throw new TypeConverterFactoryException(String.format("\tFailed to add database type converter for %s type", databaseTypeConverter.getType()), cnfe);
			}
		}

		logger.info("Type converter factory successfully started");
	}

	public static AbstractTypeConverter makeTypeConverter(String type) throws TypeConverterFactoryException {
		AbstractTypeConverter result = null;

		Class<AbstractTypeConverter> typeConverter = TYPE_CONVERTER_MAP.get(type);
		if (typeConverter != null) {
			try {
				result = typeConverter.getConstructor(String.class).newInstance(type);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
				throw new TypeConverterFactoryException(String.format("\tFailed to instantiate the database type converter for %s type", type), e);
			}
		}

		return result;
	}

	private TypeConverterFactory() {
	}

}
