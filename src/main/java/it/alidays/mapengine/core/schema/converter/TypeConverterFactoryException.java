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

public class TypeConverterFactoryException extends Exception {

	private static final long serialVersionUID = 5539510879118191523L;

	protected TypeConverterFactoryException() {
		super();
	}

	protected TypeConverterFactoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	protected TypeConverterFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

	protected TypeConverterFactoryException(String message) {
		super(message);
	}

	protected TypeConverterFactoryException(Throwable cause) {
		super(cause);
	}

}