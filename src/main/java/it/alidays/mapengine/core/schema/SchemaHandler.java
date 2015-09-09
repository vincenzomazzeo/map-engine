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
package it.alidays.mapengine.core.schema;

import it.alidays.mapengine.core.database.DatabaseManager;
import it.alidays.mapengine.core.schema.converter.AbstractTypeConverter;
import it.alidays.mapengine.core.schema.converter.TypeConverterFactory;
import it.alidays.mapengine.core.schema.converter.TypeConverterFactoryException;
import it.alidays.mapengine.enginedirectives.fetch.Bind;
import it.alidays.mapengine.enginedirectives.fetch.Entity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaHandler {

	private static final Logger logger = LoggerFactory.getLogger(SchemaHandler.class);
	
	private final DatabaseManager databaseManager;
	private final Map<String, TableHandler> tableHandlerMap;
	
	public SchemaHandler(List<Entity> entities, DatabaseManager databaseManager) throws SchemaHandlerException {
		this.databaseManager = databaseManager;
		this.tableHandlerMap = new HashMap<>();
		
		for (Entity entity : entities) {
			LinkedHashMap<String, AbstractTypeConverter> attributes = new LinkedHashMap<>();
			for (Bind bind : entity.getForEach().getBindings()) {
				try {
					AbstractTypeConverter typeConverter = TypeConverterFactory.makeTypeConverter(bind.getType());
					if (typeConverter != null) {
						attributes.put(bind.getAttribute(), typeConverter);
						typeConverter.setLength(bind.getLength());
						typeConverter.setDecimal(bind.getDecimal());
					}
					else {
						throw new SchemaHandlerException(String.format("Type converter for type %s not found", bind.getType()));
					}
				}
				catch (TypeConverterFactoryException tcfe) {
					throw new SchemaHandlerException(String.format("Failed to create type converter for type %s", bind.getType()), tcfe);
				}
			}
			TableHandler tableHandler = new TableHandler(entity.getName(), attributes);
			this.tableHandlerMap.put(entity.getName(), tableHandler);
		}
	}
	
	public void shutdown() {
		this.databaseManager.shutdown();
	}
	
	public void create() throws SchemaHandlerException {
		logger.info("Creating schema...");
		
		try (Connection connection = this.databaseManager.getConnection()) {
			for (TableHandler tableHandler : this.tableHandlerMap.values()) {
				tableHandler.create(connection, logger);
			}
		}
		catch (Exception e) {
			throw new SchemaHandlerException(e);
		}
		
		logger.info("Schema successfully created");
	}
	
	public void insert(String entity, List<Map<String, Object>> rows, UUID vuid) throws SchemaHandlerException {
		TableHandler tableHandler = this.tableHandlerMap.get(entity);
		if (tableHandler != null) {
			try {
				Connection connection = null;
				try {
					connection = this.databaseManager.getConnection();
					for (Map<String, Object> row : rows) {
						tableHandler.insert(connection, row, vuid);
					}
					connection.commit();
				}
				catch (Exception e) {
					if (connection != null) {
						try {
							connection.rollback();
						}
						catch (SQLException sqle) {
						}
					}
					
					throw e;
				}
				finally {
					if (connection != null) {
						try {
							connection.close();
						}
						catch (SQLException sqle) {}
					}
				}
			}
			catch (Exception e) {
				throw new SchemaHandlerException(entity, e);
			}
		}
		else {
			throw new SchemaHandlerException(String.format("Entity '%s' not found", entity));
		}
	}
	
	public void delete(String entity, UUID vuid) throws SchemaHandlerException {
		TableHandler tableHandler = this.tableHandlerMap.get(entity);
		if (tableHandler != null) {
			try {
				Connection connection = null;
				try {
					connection = this.databaseManager.getConnection();
					tableHandler.delete(connection, vuid);
					connection.commit();
				}
				catch (Exception e) {
					if (connection != null) {
						try {
							connection.rollback();
						}
						catch (SQLException sqle) {
						}
					}
					
					throw e;
				}
				finally {
					if (connection != null) {
						try {
							connection.close();
						}
						catch (SQLException sqle) {}
					}
				}
			}
			catch (Exception e) {
				throw new SchemaHandlerException(entity, e);
			}
		}
		else {
			throw new SchemaHandlerException(String.format("Entity '%s' not found", entity));
		}
	}
	
}
