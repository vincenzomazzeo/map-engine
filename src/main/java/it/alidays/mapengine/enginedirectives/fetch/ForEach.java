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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class ForEach {

	private String value;
	private List<Bind> bindings;

	@XmlAttribute(name = "value", required = true)
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@XmlElement(name = "bind", required = true)
	public List<Bind> getBindings() {
		return this.bindings;
	}

	public void setBindings(List<Bind> bindings) {
		this.bindings = bindings;
	}

}
