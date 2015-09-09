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

import it.alidays.mapengine.util.PerformanceUtils;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchTask implements Callable<FetchEntityResult> {

	private static final Logger logger = LoggerFactory.getLogger(FetchTask.class);
	
	private final String entity;
	private final ToEntityMethod toEntityMethod;
	private final Element baseNode;
	private final UUID vuid;
	
	protected FetchTask(String entity, ToEntityMethod toEntityMethod, Element baseNode, UUID vuid) {
		this.entity = entity;
		this.toEntityMethod = toEntityMethod;
		this.baseNode = baseNode;
		this.vuid = vuid;
	}
	
	@Override
	public FetchEntityResult call() throws Exception {
		FetchEntityResult result = null;
		
		PerformanceUtils.notifyStart(Fetcher.class, String.format("run(Fetch-%s)", this.entity), this.vuid);
		result = new FetchEntityResult(this.entity, this.toEntityMethod.run(this.baseNode));
		PerformanceUtils.notifyEnd(Fetcher.class, String.format("run(Fetch-%s)", this.entity), this.vuid, logger);
		
		return result;
	}

}
