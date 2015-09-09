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
package it.alidays.mapengine.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "configuration")
public class Configuration {

	private Persistence persistence;
	private List<DatabaseTypeConverter> databaseTypeConverters;
	private List<FetchFunction> fetchFunctions;

	@XmlElement(name = "persistence", required = true)
	public Persistence getPersistence() {
		return this.persistence;
	}

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@XmlElementWrapper(name = "database-type-converters", required = true)
	@XmlElement(name = "database-type-converter", required = true)
	public List<DatabaseTypeConverter> getDatabaseTypeConverters() {
		return this.databaseTypeConverters;
	}

	public void setDatabaseTypeConverters(List<DatabaseTypeConverter> databaseTypeConverters) {
		this.databaseTypeConverters = databaseTypeConverters;
	}

	@XmlElementWrapper(name = "fetch-functions", required = true)
	@XmlElement(name = "fetch-function", required = true)
	public List<FetchFunction> getFetchFunctions() {
		return this.fetchFunctions;
	}

	public void setFetchFunctions(List<FetchFunction> fetchFunctions) {
		this.fetchFunctions = fetchFunctions;
	}
	
}
