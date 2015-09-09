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
package it.alidays.mapengine.core.fetch.function;

import java.util.List;

import org.dom4j.Element;

public class PositionFunction extends Function {

	private final String[] path;
	
	public PositionFunction(String value) {
		super(value);

		this.path = value.split("/");
	}

	@Override
	public Object evaluate(Element node) {
		Object result = null;

		Element parent = node;

		for (String pathElement : this.path) {
			if (pathElement.equals("..")) {
				parent = parent.getParent();
			}
		}

		@SuppressWarnings("rawtypes")
		List parents = parent.getParent().selectNodes(parent.getPath(parent.getParent()));
		for (int index = 0, length = parents.size(); index < length; index++) {
			Element currentParent = (Element)parents.get(index);
			if (currentParent.getUniquePath().equals(parent.getUniquePath())) {
				result = index + 1;
				break;
			}
		}

		return result;
	}

}
