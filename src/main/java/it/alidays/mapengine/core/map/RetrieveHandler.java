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

import it.alidays.mapengine.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrieveHandler {

	public final static String VUID_KEY = "#vuid#";
	private final static Pattern VUID_PATTERN = Pattern.compile(VUID_KEY);
	
	public static Integer getVuidCount(String content) {
		Integer result = 0;
		
		Matcher matcher = VUID_PATTERN.matcher(content);
		while (matcher.find()) {
			result++;
		}
		
		return result;
	}
	
	private final AbstractRetrieve<?> retrieve;
	private final Integer vuidCount;
	private final String content;
	private final List<String> columns;
	
	protected RetrieveHandler(AbstractRetrieve<?> retrieve, String content) {
		this.retrieve = retrieve;
		this.vuidCount = getVuidCount(content);
		this.content = content.replaceAll(VUID_KEY, "?");
		this.columns = new ArrayList<>();
	}
	
	protected List<Object> execute(Connection connection, UUID vuid) throws SQLException {
		List<Object> result = new ArrayList<>();
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(this.content)) {
			for (int index = 1; index <= this.vuidCount; index++) {
				preparedStatement.setObject(index, vuid);
			}
			
			ResultSet resultSet = preparedStatement.executeQuery();
			retrieveColumns(resultSet);
			
			while (resultSet.next()) {
				Map<String, Object> data = new HashMap<>();
				for (String column : columns) {
					data.put(Utils.arrangeColumnName(column), resultSet.getObject(column));
				}
				
				result.add(this.retrieve.getMap(data));
			}
		}
		
		return result;
	}
	
	private void retrieveColumns(ResultSet resultSet) throws SQLException {
		if (this.columns.isEmpty()) {
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			for (int i = 1, n = resultSetMetaData.getColumnCount(); i <= n; i++) {
				this.columns.add(resultSetMetaData.getColumnLabel(i));
			}
		}
	}
	
}
