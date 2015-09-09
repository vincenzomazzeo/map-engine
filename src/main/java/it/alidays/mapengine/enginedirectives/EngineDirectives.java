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
package it.alidays.mapengine.enginedirectives;

import it.alidays.mapengine.enginedirectives.fetch.Fetch;
import it.alidays.mapengine.enginedirectives.map.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "engine-directives")
public class EngineDirectives {

	private Boolean debug = false;
	private Fetch fetch;
	private Map map;

	@XmlAttribute(name = "debug", required = false)
	public Boolean getDebug() {
		return this.debug;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	@XmlElement(name = "fetch", required = true)
	public Fetch getFetch() {
		return this.fetch;
	}

	public void setFetch(Fetch fetch) {
		this.fetch = fetch;
	}

	@XmlElement(name = "map", required = true)
	public Map getMap() {
		return this.map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

}
