/**
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

package com.yametech.yangjian.agent.plugin.druid;

import com.yametech.yangjian.agent.api.base.IContext;
import com.yametech.yangjian.agent.api.bean.TimeEvent;
import com.yametech.yangjian.agent.api.common.Constants;
import com.yametech.yangjian.agent.api.convert.IMethodConvert;
import com.yametech.yangjian.agent.plugin.druid.context.ContextConstants;
import com.yametech.yangjian.agent.plugin.druid.monitor.DruidDataSourceMonitor;
import com.yametech.yangjian.agent.util.Utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author dengliming
 * @date 2019/12/22
 */
public class DruidConnectionConvert implements IMethodConvert {

    @Override
    public List<TimeEvent> convert(Object thisObj, long startTime, Object[] allArguments, Method method,
                                   Object ret, Throwable t, Map<Class<?>, Object> globalVar) throws Throwable {
        if (!(thisObj instanceof IContext)) {
            return null;
        }
        DruidDataSourceMonitor druidDataSourceMonitor = (DruidDataSourceMonitor) ((IContext) thisObj)._getAgentContext(ContextConstants.DATA_SOURCE_CONTEXT_FIELD);
        TimeEvent event = get(startTime);
        event.setIdentify(Utils.parseJdbcUrl(druidDataSourceMonitor.getJdbcUrl()) + Constants.IDENTIFY_SEPARATOR + Constants.DbOperation.GET_CONNECTION);
        return Arrays.asList(event);
    }
}
