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
package com.yametech.yangjian.agent.core.config;

import com.yametech.yangjian.agent.api.IConfigReader;
import com.yametech.yangjian.agent.api.base.SPI;
import com.yametech.yangjian.agent.api.bean.ConfigNotifyType;
import com.yametech.yangjian.agent.api.common.Constants;
import com.yametech.yangjian.agent.api.interceptor.IDisableConfig;
import com.yametech.yangjian.agent.core.common.CoreConstants;
import com.yametech.yangjian.agent.core.core.interceptor.InterceptorWrapper;
import com.yametech.yangjian.agent.core.core.interceptor.InterceptorWrapperRegistry;

import java.util.*;

/**
 * @author dengliming
 * @date 2020/3/24
 */
public class DisableSpiConfigReader implements IConfigReader, SPI {

    @Override
    public Set<String> configKey() {
        return new HashSet<>(Collections.singletonList("spi\\..*"));
    }

    @Override
    public void configKeyValue(Map<String, String> kv) {
        Collection<InterceptorWrapper<?>> interceptorWrapper = InterceptorWrapperRegistry.INSTANCE.getInterceptorWrapperMap();
        if (interceptorWrapper.isEmpty()) {
            return;
        }
        boolean disableAll = CoreConstants.CONFIG_KEY_DISABLE.equals(kv.getOrDefault(CoreConstants.SPI_PLUGIN_KEY, CoreConstants.CONFIG_KEY_ENABLE));
        interceptorWrapper.forEach(wrap -> {
            String configKey;
            if(wrap.getInterceptor() instanceof IDisableConfig) {
                configKey = ((IDisableConfig)wrap.getInterceptor()).disableKey();
            } else {
                configKey = Constants.DISABLE_SPI_KEY_PREFIX + wrap.getInterceptor().getClass().getSimpleName();
            }
            if(configKey == null) {
                return;
            }
            String enableConfig = kv.get(configKey);
            wrap.setEnable((disableAll && CoreConstants.CONFIG_KEY_ENABLE.equals(enableConfig))
                    || (!disableAll && !CoreConstants.CONFIG_KEY_DISABLE.equals(enableConfig)));
        });
    }

    @Override
    public ConfigNotifyType notifyType() {
        return ConfigNotifyType.ALWAYS;
    }
}
