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
package com.yametech.yangjian.agent.core.jvm.collector;

import com.yametech.yangjian.agent.api.log.ILogger;
import com.yametech.yangjian.agent.api.log.LoggerFactory;
import com.yametech.yangjian.agent.core.jvm.metrics.ProcessMetrics;
import com.yametech.yangjian.agent.core.jvm.process.LinuxProcessProvider;
import com.yametech.yangjian.agent.core.jvm.process.NoopProcessProvider;
import com.yametech.yangjian.agent.core.jvm.process.WinProcessProvider;
import com.yametech.yangjian.agent.util.OSUtil;

/**
 * @author zcn
 * @date: 2019-10-21
 */
public final class ProcessCollector implements IMetricsCollector {

    private static final ILogger logger = LoggerFactory.getLogger(ProcessCollector.class);

    public ProcessCollector() {
    }

    @Override
    public ProcessMetrics collect() {
        if (OSUtil.isWindows()) {
            return WinProcessProvider.INSTANCE.getProcessMetrics();
        } else if (OSUtil.isLinux()) {
            return LinuxProcessProvider.INSTANCE.getProcessMetrics();
        }
        logger.error(" Fail to collect process metrics , don't support os {}, use {}", OSUtil.OS, NoopProcessProvider.class.getName());
        return NoopProcessProvider.INSTANCE.getProcessMetrics();
    }
}
