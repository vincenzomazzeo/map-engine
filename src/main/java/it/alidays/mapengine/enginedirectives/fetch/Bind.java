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
package it.alidays.mapengine.enginedirectives.fetch;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Bind {

	private String attribute;
	private String type;
	private Integer length;
	private Integer decimal;
	private String value;

	@XmlAttribute(name = "attribute", required = true)
	public String getAttribute() {
		return this.attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	@XmlAttribute(name = "type", required = true)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@XmlAttribute(name = "length", required = false)
	public Integer getLength() {
		return this.length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	@XmlAttribute(name = "decimal", required = false)
	public Integer getDecimal() {
		return this.decimal;
	}

	public void setDecimal(Integer decimal) {
		this.decimal = decimal;
	}

	@XmlValue
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
