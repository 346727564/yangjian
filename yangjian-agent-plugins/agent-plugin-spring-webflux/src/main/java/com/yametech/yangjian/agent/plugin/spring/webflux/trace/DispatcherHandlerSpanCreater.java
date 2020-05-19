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
package com.yametech.yangjian.agent.plugin.spring.webflux.trace;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import com.yametech.yangjian.agent.api.bean.BeforeResult;
import com.yametech.yangjian.agent.api.common.Constants;
import com.yametech.yangjian.agent.api.common.MethodUtil;
import com.yametech.yangjian.agent.api.common.TraceUtil;
import com.yametech.yangjian.agent.api.trace.ISpanCreater;
import com.yametech.yangjian.agent.api.trace.ISpanSample;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author dengliming
 * @date 2020/4/26
 */
public class DispatcherHandlerSpanCreater implements ISpanCreater<Void> {

    protected Tracer tracer;
    private ISpanSample spanSample;
    private TraceContext.Extractor<HttpHeaders> extractor;

    @Override
    public void init(Tracing tracing, ISpanSample spanSample) {
        this.tracer = tracing.tracer();
        this.spanSample = spanSample;
        this.extractor = tracing.propagation().extractor((carrier, key) -> {
            List<String> header = carrier.get(key);
            if (header != null && header.size() > 0) {
                return header.get(0);
            }
            return null;
        });
    }

    @Override
    public BeforeResult<Void> before(Object thisObj, Object[] allArguments, Method method) throws Throwable {
        return null;
    }

    @Override
    public Object after(Object thisObj, Object[] allArguments, Method method, Object ret, Throwable t, BeforeResult<Void> beforeResult) {
        if (!spanSample.sample()) {
            return ret;
        }
        ServerWebExchange exchange = (ServerWebExchange) allArguments[0];
        HttpHeaders headers = exchange.getRequest().getHeaders();
        ServerHttpRequest request = exchange.getRequest();
        Span span = tracer.nextSpan(extractor.extract(headers))
                .kind(Span.Kind.SERVER)
                .name(MethodUtil.getId(method))
                .start(TraceUtil.nowMicros());
        span.tag(Constants.Tags.HTTP_METHOD, request.getMethodValue());
        span.tag(Constants.Tags.URL, request.getURI().toString());
        final Map<String, List<String>> parameterMap = request.getQueryParams();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            parameterMap.forEach((k, v) -> span.tag(k, v.toString()));
        }
        Tracer.SpanInScope spanInScope = tracer.withSpanInScope(span);
        return ((Mono) ret).doFinally(s -> {
            try {
                if (t != null) {
                    span.error(t);
                }
                Object pathPattern = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                if (pathPattern != null) {
                    span.name(((PathPattern) pathPattern).getPatternString());
                }
                HttpStatus httpStatus = exchange.getResponse().getStatusCode();
                // fix webflux-2.0.0-2.1.0 version have bug. httpStatus is null. not support
                if (httpStatus != null) {
                    span.tag(Constants.Tags.STATUS_CODE, Integer.toString(httpStatus.value()));
                }
            } finally {
                span.finish();
                spanInScope.close();
            }
        });
    }
}