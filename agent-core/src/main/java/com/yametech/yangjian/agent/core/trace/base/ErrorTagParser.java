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
package com.yametech.yangjian.agent.core.trace.base;

import brave.ErrorParser;
import com.yametech.yangjian.agent.api.common.StringUtil;

/**
 * 自定义异常Tag解析，限制异常信息的长度
 *
 * @author dengliming
 * @date 2020/6/30
 */
public class ErrorTagParser extends ErrorParser {

    private static final int MAX_ERROR_TAG_LENGTH = 500;
    private static final String ERROR_TAG_KEY = "error";

    @Override
    protected void error(Throwable error, Object span) {
        String message = error.getMessage();
        if (message == null) {
            message = error.getClass().getSimpleName();
        } else {
            message = StringUtil.shorten(message, MAX_ERROR_TAG_LENGTH);
        }
        tag(span, ERROR_TAG_KEY, message);
    }
}
