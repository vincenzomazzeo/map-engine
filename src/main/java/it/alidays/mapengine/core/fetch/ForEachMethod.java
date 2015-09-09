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
package it.alidays.mapengine.core.fetch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

public class ForEachMethod implements ToEntityMethod {

	private final String path;
	private final Map<String, Binder> binderMap;
	
	protected ForEachMethod(String path) {
		this.path = path;
		this.binderMap = new LinkedHashMap<>();
	}
	
	@Override
	public List<Map<String, Object>> run(Element baseNode) {
		List<Map<String, Object>> result = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<Element> nodes = baseNode.selectNodes(this.path);
		for (Element node : nodes) {
			Map<String, Object> tupla = new HashMap<>();
			result.add(tupla);
			
			for (String attribute : this.binderMap.keySet()) {
				Binder binder = this.binderMap.get(attribute);
				tupla.put(attribute, binder.bind(node));
			}
		}
		
		return result;
	}
	
	protected void addBinder(String attribute, Binder binder) {
		this.binderMap.put(attribute, binder);
	}
	
}
