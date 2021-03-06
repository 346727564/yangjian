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
package com.yametech.yangjian.agent.plugin.dubbo;

import java.util.Arrays;

import com.yametech.yangjian.agent.api.IMetricMatcher;
import com.yametech.yangjian.agent.api.base.IConfigMatch;
import com.yametech.yangjian.agent.api.base.MethodType;
import com.yametech.yangjian.agent.api.bean.LoadClassKey;
import com.yametech.yangjian.agent.api.bean.MethodDefined;
import com.yametech.yangjian.agent.api.common.Constants;
import com.yametech.yangjian.agent.api.configmatch.CombineAndMatch;
import com.yametech.yangjian.agent.api.configmatch.CombineOrMatch;
import com.yametech.yangjian.agent.api.configmatch.MethodArgumentIndexMatch;
import com.yametech.yangjian.agent.api.configmatch.MethodArgumentNumMatch;
import com.yametech.yangjian.agent.api.configmatch.MethodRegexMatch;

/**
 * 将dubbo服务端调用，转换成实际调用的接口
 * 支持版本：
 * alibaba：dubbo-2.4.10、dubbo-2.5.3、dubbo-2.5.4、dubbo-2.5.5、dubbo-2.5.6、dubbo-2.5.7、dubbo-2.5.10、dubbo-2.6.0、dubbo-2.6.1、dubbo-2.6.2、dubbo-2.6.3、dubbo-2.6.4、dubbo-2.6.5、dubbo-2.6.6、dubbo-2.6.7、dubbo-2.8.3、dubbo-2.8.4
 * apache：dubbo-2.7.0、dubbo-2.7.1、dubbo-2.7.2、dubbo-2.7.3、dubbo-2.7.4
 *
 * @author liuzhao
 * @Description
 * @date 2019年10月9日 下午3:43:15
 */
public class DubboServerMatcher implements IMetricMatcher {

    @Override
    public IConfigMatch match() {
        return new CombineAndMatch(Arrays.asList(
                new CombineOrMatch(Arrays.asList(
                        new MethodRegexMatch(".*com\\.alibaba\\.dubbo\\.rpc\\.proxy\\.javassist\\.JavassistProxyFactory.*doInvoke\\(.*"),
                        new MethodRegexMatch(".*org\\.apache\\.dubbo\\.rpc\\.proxy\\.javassist\\.JavassistProxyFactory.*doInvoke\\(.*")
                )),
                new MethodArgumentNumMatch(4),
                new MethodArgumentIndexMatch(1, "java.lang.String")
        ));
    }

    @Override
    public String type() {
        return Constants.EventType.DUBBO_SERVER;
    }
    
    @Override
    public LoadClassKey loadClass(MethodType type, MethodDefined methodDefined) {
    	return new LoadClassKey("com.yametech.yangjian.agent.plugin.dubbo.DubboServerConvert");
    }
    
}
