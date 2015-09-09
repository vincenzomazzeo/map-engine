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

public class DatabaseTypeConverter {

	private String type;
	private String class_;

	@XmlAttribute(name = "type", required = true)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlAttribute(name = "class", required = true)
	public String getClass_() {
		return this.class_;
	}

	public void setClass_(String class_) {
		this.class_ = class_;
	}

}
