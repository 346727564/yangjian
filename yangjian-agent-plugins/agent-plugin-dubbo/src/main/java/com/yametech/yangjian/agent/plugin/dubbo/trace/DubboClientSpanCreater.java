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

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;

import com.yametech.yangjian.agent.api.bean.BeforeResult;
import com.yametech.yangjian.agent.api.common.BraveUtil;
import com.yametech.yangjian.agent.api.common.TraceUtil;
import com.yametech.yangjian.agent.api.trace.ISpanSample;
import com.yametech.yangjian.agent.api.trace.custom.IDubboClientCustom;
import com.yametech.yangjian.agent.api.trace.custom.IDubboCustom;

import brave.Span;
import brave.Span.Kind;
import brave.Tracing;
import brave.propagation.TraceContext;

public class DubboClientSpanCreater extends DubboSpanCreater<IDubboClientCustom> {
	private TraceContext.Injector<Map<String, String>> injector;
	
	@Override
	public void init(Tracing tracing, ISpanSample spanSample) {
		super.init(tracing, spanSample);
		this.injector = tracing.propagation().injector(BraveUtil.MAP_SETTER);
	}
	
	@Override
	public BeforeResult<SpanInfo> before(Object thisObj, Object[] allArguments, Method method) throws Throwable {
		RpcContext rpcContext = RpcContext.getContext();
		Kind kind = rpcContext.isConsumerSide() ? Kind.CLIENT : Kind.SERVER;
		if(!kind.equals(Kind.CLIENT)) {
			return null;
		}
		Invoker<?> invoker = (Invoker<?>) allArguments[0];
		Invocation invocation = (Invocation) allArguments[1];
		IDubboCustom custom = getCustom(invoker.getInterface(), invocation.getMethodName(), invocation.getParameterTypes());
		if(!generateSpan(invocation.getArguments(), custom)) {// 不需要生成
			return null;
		}
		
		Span span = tracer.nextSpan().kind(Kind.CLIENT)
				.name(getSpanName(invoker.getInterface().getName(), invocation.getMethodName(), invocation.getParameterTypes()))
				.start(TraceUtil.nowMicros());
		injector.inject(span.context(), rpcContext.getAttachments());
		return spanInit(span, invocation.getArguments(), custom);
	}
	
}