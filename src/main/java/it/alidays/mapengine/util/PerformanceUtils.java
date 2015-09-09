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
package it.alidays.mapengine.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

public class PerformanceUtils {

	private static final Map<String, Long> PERFORMANCE_MAP = new ConcurrentHashMap<>();
	
	public static void notifyStart(Class<?> caller, String method, UUID vuid) {
		PERFORMANCE_MAP.put(String.format("%s[%s-%s]", caller.getName(), method, vuid.toString()), System.currentTimeMillis());
	}
	
	public static void notifyEnd(Class<?> caller, String method, UUID vuid, Logger logger) {
		Long end = System.currentTimeMillis();
		Long start = PERFORMANCE_MAP.get(String.format("%s[%s-%s]", caller.getName(), method, vuid.toString()));
		String delay = start != null ? String.valueOf(end - start) : "ND";
		
		logger.info("Execution time of {} -> {}: {}", new Object[] {caller.getName(), method, delay});
	}
	
	private PerformanceUtils() {}
	
}
