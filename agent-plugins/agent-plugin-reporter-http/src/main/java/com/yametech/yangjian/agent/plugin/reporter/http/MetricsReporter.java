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
package com.yametech.yangjian.agent.plugin.reporter.http;

/**
 * QPS&RT HTTP上报方式
 * <p>
 * 注：agent.properties配置report.statistic=http-statistic、report.http-statistic.url=xxx
 *
 * @author dengliming
 */
public class MetricsReporter extends AbstractHttpReporter {

    @Override
    public String type() {
        return "http-statistic";
    }

    @Override
    public String getConfigKey() {
        return "report.http-statistic.url";
    }
}
