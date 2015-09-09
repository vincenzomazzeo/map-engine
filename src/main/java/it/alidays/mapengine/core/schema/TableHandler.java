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

import it.alidays.mapengine.core.schema.converter.AbstractTypeConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

public class TableHandler {

	private final String name;
	private final LinkedHashMap<String, AbstractTypeConverter> attributes;

	protected TableHandler(String name, LinkedHashMap<String, AbstractTypeConverter> attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	protected void create(Connection connection, Logger logger) throws SQLException {
		StringBuilder create = new StringBuilder();

		create.append(String.format("CREATE TABLE %s (", this.name));
		create.append("vid identity not null, vuid uuid not null, ");
		for (String attributeName : this.attributes.keySet()) {
			AbstractTypeConverter typeConverter = this.attributes.get(attributeName);
			
			create.append(attributeName);
			create.append(" ");
			create.append(typeConverter.getType());
			if (typeConverter.getLength() != null) {
				create.append("(");
				create.append(typeConverter.getLength());
				if (typeConverter.getDecimal() != null) {
					create.append(", ");
					create.append(typeConverter.getDecimal());
				}
				create.append(")");
			}
			create.append(", ");
		}
		create.append("primary key (vid))");

		logger.debug(create.toString());
		
		try {		
			connection.prepareStatement(create.toString()).execute();
		}
		catch (SQLException sqle) {
			if (sqle.getErrorCode() == 42101) {
				// Tabella già esistente.
				logger.info("\tTable {} already exists", this.name);
				delete(connection);
				logger.info("\tTable {} cleaned", this.name);
			}
			else {
				throw sqle;
			}
		}
	}

	protected void insert(Connection connection, Map<String, Object> data, UUID vuid) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(createInsert())) {
			int index = 1;
			for (String attributeName : this.attributes.keySet()) {
				this.attributes.get(attributeName).set(preparedStatement, index++, data.get(attributeName));
			}
			preparedStatement.setObject(index++, vuid);
			preparedStatement.execute();
		}
	}
	
	protected void delete(Connection connection) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DELETE FROM %s", this.name))) {
			preparedStatement.execute();
		}
	}
	
	protected void delete(Connection connection, UUID vuid) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DELETE FROM %s WHERE vuid = ?", this.name))) {
			preparedStatement.setObject(1, vuid);
			preparedStatement.execute();
		}
	}
	
	protected String getName() {
		return this.name;
	}

	private String createInsert() {
		String result = null;

		StringBuilder columns = new StringBuilder();
		StringBuilder questionMarks = new StringBuilder();

		for (String attributeName : this.attributes.keySet()) {
			columns.append(String.format("%s, ", attributeName));
			questionMarks.append("?, ");
		}
		columns.append("vuid");
		questionMarks.append("?");

		result = String.format("INSERT INTO %s (%s) values (%s)", this.name, columns.toString(), questionMarks.toString());

		return result;
	}

}
