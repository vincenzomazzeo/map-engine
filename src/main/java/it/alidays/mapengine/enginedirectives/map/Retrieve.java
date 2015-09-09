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
package it.alidays.mapengine.enginedirectives.map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Retrieve {

	private String id;
	private String content;

	@XmlAttribute(name = "id", required = true)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlValue
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
