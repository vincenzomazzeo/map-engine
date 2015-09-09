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

import it.alidays.mapengine.util.PerformanceUtils;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveTask implements Callable<RetrieveResult> {

	private static final Logger logger = LoggerFactory.getLogger(RetrieveTask.class);
	
	private final String id;
	private final RetrieveHandler retrieveHandler;
	private final Connection connection;
	private final UUID vuid;
	
	protected RetrieveTask(String id, RetrieveHandler retrieveHandler, Connection connection, UUID vuid) {
		this.id = id;
		this.retrieveHandler = retrieveHandler;
		this.connection = connection;
		this.vuid = vuid;
	}
	
	@Override
	public RetrieveResult call() throws Exception {
		RetrieveResult result = null;
		
		PerformanceUtils.notifyStart(Mapper.class, String.format("map(retrieve->%s)", id), this.vuid);
		result = new RetrieveResult(this.id, this.retrieveHandler.execute(this.connection, this.vuid));
		PerformanceUtils.notifyEnd(Mapper.class, String.format("map(retrieve->%s)", id), this.vuid, logger);
		
		return result;
	}

}
