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
package com.yametech.yangjian.agent.plugin.client.metric;

import com.yametech.yangjian.agent.api.IMetricMatcher;
import com.yametech.yangjian.agent.api.base.IConfigMatch;
import com.yametech.yangjian.agent.api.base.MethodType;
import com.yametech.yangjian.agent.api.bean.LoadClassKey;
import com.yametech.yangjian.agent.api.bean.MethodDefined;
import com.yametech.yangjian.agent.api.common.Constants;
import com.yametech.yangjian.agent.api.configmatch.ClassMatch;
import com.yametech.yangjian.agent.api.configmatch.CombineAndMatch;
import com.yametech.yangjian.agent.api.configmatch.CombineOrMatch;
import com.yametech.yangjian.agent.api.configmatch.MethodArgumentIndexMatch;
import com.yametech.yangjian.agent.api.configmatch.MethodArgumentNumMatch;
import com.yametech.yangjian.agent.api.configmatch.MethodNameMatch;

import java.util.Arrays;

public class MethodMetricMatcher implements IMetricMatcher {

    @Override
    public IConfigMatch match() {
        return new CombineOrMatch(Arrays.asList(
                new CombineAndMatch(Arrays.asList(
                        new ClassMatch("com.yametech.yangjian.agent.client.MetricUtil"),
                        new MethodNameMatch("mark"),
                        new MethodArgumentNumMatch(3),
                        new MethodArgumentIndexMatch(0, "java.lang.String"),
                        new MethodArgumentIndexMatch(1, "int"),
                        new MethodArgumentIndexMatch(2, "java.util.function.Supplier")
                )),
                new CombineAndMatch(Arrays.asList(
                        new ClassMatch("com.yametech.yangjian.agent.client.MetricUtil"),
                        new MethodNameMatch("mark"),
                        new MethodArgumentNumMatch(2),
                        new MethodArgumentIndexMatch(0, "java.lang.String"),
                        new MethodArgumentIndexMatch(1, "int")
                ))
        ));
    }

    @Override
    public String type() {
        return Constants.EventType.METHOD;
    }

    @Override
    public LoadClassKey loadClass(MethodType type, MethodDefined methodDefined) {
        return new LoadClassKey("com.yametech.yangjian.agent.plugin.client.metric.MethodMetricConvert");
    }

}
