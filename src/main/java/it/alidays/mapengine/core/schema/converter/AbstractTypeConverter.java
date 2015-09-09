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

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractTypeConverter {

	private final String type;
	private Integer length;
	private Integer decimal;
	
	protected AbstractTypeConverter(String type) {
		this.type = type;
	}
	
	public abstract void set(PreparedStatement preparedStatement, Integer index, Object value) throws SQLException;
	
	public String getType() {
		return this.type;
	}
	
	public Integer getLength() {
		return this.length;
	}
	
	public void setLength(Integer length) {
		this.length = length;
	}
	
	public Integer getDecimal() {
		return this.decimal;
	}

	public void setDecimal(Integer decimal) {
		this.decimal = decimal;
	}
	
}
