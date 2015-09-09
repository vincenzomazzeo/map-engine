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

import javax.xml.bind.annotation.XmlAttribute;

public class Database {

	private String url;
	private String user;
	private String password;

	@XmlAttribute(name = "url", required = true)
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlAttribute(name = "user", required = true)
	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@XmlAttribute(name = "password", required = true)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
