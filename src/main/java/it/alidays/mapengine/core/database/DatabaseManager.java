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
package it.alidays.mapengine.core.database;

import it.alidays.mapengine.util.PerformanceUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
	
	private final JdbcConnectionPool connectionPool;
	
	public DatabaseManager(String url, String user, String password) throws SQLException {
		UUID vuid = UUID.randomUUID();
		
		PerformanceUtils.notifyStart(DatabaseManager.class, "<init>", vuid);

		this.connectionPool = JdbcConnectionPool.create(url, user, password);
		
		PerformanceUtils.notifyEnd(DatabaseManager.class, "<init>", vuid, logger);
	}
	
	public void shutdown() {
		this.connectionPool.dispose();
	}

	public Connection getConnection() throws SQLException {
		return this.connectionPool.getConnection();
	}
	
}
