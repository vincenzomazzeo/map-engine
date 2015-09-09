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

public class AggregatorException extends Exception {

	private static final long serialVersionUID = 122513428735957707L;

	public AggregatorException() {
		super();
	}

	public AggregatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AggregatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public AggregatorException(String message) {
		super(message);
	}

	public AggregatorException(Throwable cause) {
		super(cause);
	}

}
