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
package com.yametech.yangjian.agent.plugin.dubbo.trace;

import brave.Span;
import brave.Span.Kind;
import brave.Tracing;
import brave.propagation.ExtraFieldPropagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.yametech.yangjian.agent.api.bean.BeforeResult;
import com.yametech.yangjian.agent.api.common.BraveUtil;
import com.yametech.yangjian.agent.api.common.Constants;
import com.yametech.yangjian.agent.api.common.StringUtil;
import com.yametech.yangjian.agent.api.trace.ISpanSample;
import com.yametech.yangjian.agent.api.trace.SpanInfo;
import com.yametech.yangjian.agent.api.trace.custom.IDubboCustom;
import com.yametech.yangjian.agent.api.trace.custom.IDubboServerCustom;
import com.yametech.yangjian.agent.plugin.dubbo.util.ClassUtil;
import com.yametech.yangjian.agent.plugin.dubbo.util.DubboSpanUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.model.ProviderModel;

import java.lang.reflect.Method;
import java.util.Map;

public class ApacheDubboServerSpanCreater extends ApacheDubboSpanCreater<IDubboServerCustom> {
    private TraceContext.Extractor<Map<String, String>> extractor;

    @Override
    public void init(Tracing tracing, ISpanSample spanSample) {
        super.init(tracing, spanSample);
        this.extractor = tracing.propagation().extractor(BraveUtil.MAP_GETTER);
    }

    @Override
    public BeforeResult<SpanInfo> before(Object thisObj, Object[] allArguments, Method method) throws Throwable {
        RpcContext rpcContext = RpcContext.getContext();
        Invoker<?> invoker = (Invoker<?>) allArguments[0];
        Kind kind = isConsumerSide(rpcContext, invoker.getUrl()) ? Kind.CLIENT : Kind.SERVER;
        if (!kind.equals(Kind.SERVER)) {
            return null;
        }
        Invocation invocation = (Invocation) allArguments[1];
        IDubboCustom custom = getCustom(invoker.getInterface(), invocation.getMethodName(), invocation.getParameterTypes());
        if (!generateSpan(invocation.getArguments(), custom)) {// 不需要生成
            return null;
        }
        long startTime = MICROS_CLOCK.nowMicros();
        if (startTime == -1L) {
            return null;
        }
        TraceContextOrSamplingFlags extracted = extractor.extract(invocation.getAttachments());// 注入请求中带的链路信息
		Class implClass = getImplClass(invoker.getUrl());
		String className = implClass != null ? implClass.getName() : invoker.getInterface().getName();
        Span span = tracer.nextSpan(extracted)
                .kind(kind)
                .name(DubboSpanUtil.getSpanName(className, invocation.getMethodName(), invocation.getParameterTypes()))
                .start(startTime);
        String parentServiceName = ExtraFieldPropagation.get(span.context(), Constants.ExtraHeaderKey.REFERER_SERVICE);
        String agentSign = ExtraFieldPropagation.get(span.context(), Constants.ExtraHeaderKey.AGENT_SIGN);
        DubboSpanUtil.tagAgentSign(span, parentServiceName, agentSign, invoker.getInterface().getName());
        return spanInit(span, invocation.getArguments(), custom);
    }

	/**
	 * 获取dubbo provider实际接口的实现类
	 *
	 * @param url
	 * @return
	 */
	private Class getImplClass(URL url) {
        if (url == null) {
            return null;
        }

        try {
            String pathKey = getPathKey(url);
            if (pathKey == null) {
                return null;
            }

            ProviderModel providerModel = ApplicationModel.getProviderModel(pathKey);
            if (providerModel != null) {
                return ClassUtil.getOriginalClass(providerModel.getServiceInstance());
            }
        } catch (Throwable t) { }
        return null;
    }

    /**
     * dubbo-2.7.0 URL没有getPathKey方法所以参考实现copy到这里
     *
     * @param url
     * @return
     */
    private String getPathKey(URL url) {
        String path = url.getPath();
        String inf = StringUtil.notEmpty(path) ? path : url.getServiceInterface();
        if (inf == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        String group = url.getParameter("group");
        String version = url.getParameter("version");
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }
}
