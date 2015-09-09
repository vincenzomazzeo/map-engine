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

public class MapperException extends Exception {

	private static final long serialVersionUID = -6700649750639631563L;

	protected MapperException() {
		super();
	}

	protected MapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	protected MapperException(String message, Throwable cause) {
		super(message, cause);
	}

	protected MapperException(String message) {
		super(message);
	}

	protected MapperException(Throwable cause) {
		super(cause);
	}

}
