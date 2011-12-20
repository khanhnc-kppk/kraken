/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.msgbus;

import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.User;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-user-plugin")
@MsgbusPlugin
public class UserPlugin {
	@Requires
	private UserApi userApi;

	@Requires
	private ConfigManager conf;

	@MsgbusMethod
	public void getUsers(Request req, Response resp) {
		if (!req.has("ou_guid"))
			resp.put("users", PrimitiveConverter.serialize(userApi.getUsers(req.getOrgDomain())));
		else {
			String orgUnitGuid = req.getString("ou_guid");
			boolean includeChildren = req.has("inc_children") ? req.getBoolean("inc_children") : false;
			Collection<User> users = userApi.getUsers(req.getOrgDomain(), orgUnitGuid, includeChildren);
			resp.put("users", PrimitiveConverter.serialize(users));
		}
	}

	@MsgbusMethod
	public void getUser(Request req, Response resp) {
		String loginName = req.getString("login_name");
		User user = userApi.getUser(req.getOrgDomain(), loginName);
		resp.put("user", PrimitiveConverter.serialize(user));
	}

	@MsgbusMethod
	// @MsgbusPermission(group = "dom.org", code = "manage")
	public void createUser(Request req, Response resp) {
		User user = (User) PrimitiveConverter.overwrite(new User(), req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		userApi.createUser(req.getOrgDomain(), user);
	}

	@MsgbusMethod
	// @MsgbusPermission(group = "dom.org", code = "manage")
	public void updateUser(Request req, Response resp) {
		User before = userApi.getUser(req.getOrgDomain(), req.getString("login_name"));
		User user = (User) PrimitiveConverter.overwrite(before, req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		userApi.updateUser(req.getOrgDomain(), user);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	// @MsgbusPermission(group = "dom.org", code = "manage")
	public void removeUsers(Request req, Response resp) {
		List<String> loginNames = (List<String>) req.get("login_names");
		for (String loginName : loginNames)
			userApi.removeUser(req.getOrgDomain(), loginName);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void removeUser(Request req, Response resp) {
		String loginName = req.getString("login_name");
		userApi.removeUser(req.getOrgDomain(), loginName);
	}
}
