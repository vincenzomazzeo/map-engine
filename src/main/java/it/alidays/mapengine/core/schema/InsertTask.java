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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class InsertTask implements Callable<Void> {

	private final SchemaHandler schemaHandler;
	private final String entity;
	private final List<Map<String, Object>> rows;
	private final UUID vuid;
	
	public InsertTask(SchemaHandler schemaHandler, String entity, List<Map<String, Object>> rows, UUID vuid) {
		this.schemaHandler = schemaHandler;
		this.entity = entity;
		this.rows = rows;
		this.vuid = vuid;
	}
	
	@Override
	public Void call() throws Exception {
		this.schemaHandler.insert(this.entity, this.rows, this.vuid);
		
		return null;
	}

}
