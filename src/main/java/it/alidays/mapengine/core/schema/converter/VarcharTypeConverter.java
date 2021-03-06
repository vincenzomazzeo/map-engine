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
import java.sql.Types;

public class VarcharTypeConverter extends AbstractTypeConverter {

	public VarcharTypeConverter(String type) {
		super(type);
	}

	@Override
	public void set(PreparedStatement preparedStatement, Integer index, Object value) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(index, Types.VARCHAR);
		}
		else {
			preparedStatement.setString(index, value.toString());
		}
	}

}
