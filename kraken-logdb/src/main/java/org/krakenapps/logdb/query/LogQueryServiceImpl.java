/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.logdb.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LookupHandler;
import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logdb.query.LogQueryImpl;

@Component(name = "logdb-query")
@Provides
public class LogQueryServiceImpl implements LogQueryService {
	@Requires
	private LogStorage logStorage;

	@Requires
	private LogTableRegistry tableRegistry;

	private Map<Integer, LogQuery> queries = new HashMap<Integer, LogQuery>();

	private Map<String, LookupHandler> lookupHandlers = new HashMap<String, LookupHandler>();

	@Override
	public LogQuery createQuery(String query) {
		LogQuery lq = new LogQueryImpl(this, logStorage, tableRegistry, query);
		queries.put(lq.getId(), lq);
		return lq;
	}

	@Override
	public void removeQuery(int id) {
		LogQuery lq = queries.get(id);
		if (lq == null)
			return;

		if (!lq.isEnd())
			lq.cancel();

		FileBufferList<Map<String, Object>> fbl = (FileBufferList<Map<String, Object>>) lq.getResult();
		if (fbl != null)
			fbl.close();

		queries.remove(id);
	}

	@Override
	public Collection<LogQuery> getQueries() {
		return queries.values();
	}

	@Override
	public LogQuery getQuery(int id) {
		return queries.get(id);
	}

	@Override
	public void addLookupHandler(String name, LookupHandler handler) {
		lookupHandlers.put(name, handler);
	}

	@Override
	public LookupHandler getLookupHandler(String name) {
		return lookupHandlers.get(name);
	}

	@Override
	public void removeLookupHandler(String name) {
		lookupHandlers.remove(name);
	}
}
