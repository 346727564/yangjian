/*
 * Copyright 2020 yametech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yametech.yangjian.agent.plugin.mysql;

import com.yametech.yangjian.agent.api.base.IContext;
import com.yametech.yangjian.agent.api.bean.BeforeResult;
import com.yametech.yangjian.agent.api.interceptor.IMethodAOP;
import com.yametech.yangjian.agent.plugin.mysql.commons.bean.ConnectionInfo;
import com.yametech.yangjian.agent.plugin.mysql.commons.context.ContextConstants;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author dengliming
 * @date 2020/5/4
 */
public class CreatePreparedStatementInterceptor implements IMethodAOP {

    @Override
    public BeforeResult before(Object thisObj, Object[] allArguments, Method method) throws Throwable {
        return null;
    }

    @Override
    public Object after(Object thisObj, Object[] allArguments, Method method, BeforeResult beforeResult, Object ret, Throwable t, Map globalVar) throws Throwable {
        if (ret instanceof IContext) {
            ConnectionInfo connectionInfo = (ConnectionInfo) ((IContext) thisObj)._getAgentContext(ContextConstants.MYSQL_CONNECTION_INFO_CONTEXT_KEY);
            if (connectionInfo != null) {
                ((IContext) ret)._setAgentContext(ContextConstants.MYSQL_CONNECTION_INFO_CONTEXT_KEY, connectionInfo);
            }
        }
        return ret;
    }
}
