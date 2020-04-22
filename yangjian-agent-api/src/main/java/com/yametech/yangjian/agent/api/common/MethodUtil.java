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
package com.yametech.yangjian.agent.api.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.yametech.yangjian.agent.api.bean.ClassDefined;
import com.yametech.yangjian.agent.api.bean.MethodDefined;
import com.yametech.yangjian.agent.api.bean.MethodInfo;

public class MethodUtil {
	private MethodUtil() {}
	
	private static Map<Method, String> cacheMethod = new ConcurrentHashMap<>();
	private static Map<MethodInfo, String> cacheMethodInfo = new ConcurrentHashMap<>();
	
	public static String getId(MethodDefined methodDefined) {
		return getId(methodDefined.getClassDefined().getClassName(), methodDefined.getMethodName(), methodDefined.getParams());
	}
	
	public static String getId(Method method) {
		if(method == null) {
			return null;
		}
		return getId(method.getDeclaringClass().getTypeName(), method.getName(), method.getParameterTypes());
	}
	
	public static String getId(MethodInfo methodInfo) {
		if(methodInfo == null) {
			return null;
		}
		return getId(methodInfo.getCls().getTypeName(), methodInfo.getMethodName(), methodInfo.getParameterTypes());
	}

	public static String getId(String className, String methodName, Class<?>[] parameterTypes) {
		String[] parameters = null;
		if(parameterTypes != null) {
			parameters = new String[parameterTypes.length];
			for(int i = 0; i < parameterTypes.length; i++) {
				parameters[i] = parameterTypes[i].getTypeName();
			}
		}
		return getId(className, methodName, parameters);
	}
	public static String getId(String className, String methodName, String[] parameters) {
		StringBuilder id = new StringBuilder();
		id.append(className).append('.').append(methodName).append('(');
		if(parameters != null) {
			for (int j = 0; j < parameters.length; j++) {
				id.append(parameters[j]);
				if (j < (parameters.length - 1)) {
					id.append(',');
				}
			}
		}
		return id.append(')').toString();
	}
	
//	public static String getId(InDefinedShape inDefinedShape) {
//		if(inDefinedShape == null) {
//			return null;
//		}
//		StringBuilder id = new StringBuilder();
//		id.append(inDefinedShape.getDeclaringType().asErasure().getActualName()).append('.')
//			.append(inDefinedShape.getName()).append('(');
//		TypeList parameterTypes = inDefinedShape.getParameters().asTypeList().asErasures();
//		for (int j = 0; j < parameterTypes.size(); j++) {
//			id.append(parameterTypes.get(j).getActualName());
//            if (j < (parameterTypes.size() - 1)) {
//            	id.append(',');
//            }
//        }
//		id.append(')');
//		return id.toString();
//	}
	
	/**
	 * 根据方法实例以及匹配条件获取匹配的methodId
	 * @param method	方法
	 * @return 方法字符串标识
	 */
	public static String getCacheMethodId(Method method) {
		String methodId = cacheMethod.get(method);
		if(methodId != null) {
			return methodId;
		}
		methodId = getId(method);
		if(methodId != null) {
			cacheMethod.put(method, methodId);
		}
		return methodId;
	}
	
	public static String getCacheMethodId(Class<?> cls, String methodName, Class<?>[] parameterTypes) {
		MethodInfo methodInfo = new MethodInfo(cls, methodName, parameterTypes);
		String methodId = cacheMethodInfo.get(methodInfo);
		if(methodId != null) {
			return methodId;
		}
		methodId = getId(methodInfo);
//		if(matchMethodIds != null && !matchMethodIds.contains(methodId)) {
//			return null;
//		}
		if(methodId != null) {
			cacheMethodInfo.put(methodInfo, methodId);
		}
		return methodId;
	}
	
	public static MethodDefined getMethodDefined(Method method) {
		Class<?> cls = method.getDeclaringClass();
		return new MethodDefined(getClassDefined(cls), getAnnotations(method.getAnnotations()), 
				method.toString(), method.getName(), getMethodParamsType(method), method.getReturnType().getTypeName(), 
				Modifier.isStatic(method.getModifiers()), false, !Modifier.isStatic(method.getModifiers()));
	}
	
	public static ClassDefined getClassDefined(Class<?> cls) {
		return new ClassDefined(getInterface(cls), getSuperClass(cls), getAnnotations(cls.getDeclaredAnnotations()), cls.getTypeName());
	}
	
	public static String[] getMethodParamsType(Method method) {
		if(method.getParameterTypes() == null || method.getParameterTypes().length == 0) {
			return new String[0];
		}
		String[] types = new String[method.getParameterTypes().length];
		for(int i = 0; i < method.getParameterTypes().length; i++) {
			types[i] = method.getParameterTypes()[i].getTypeName();
		}
    	return types;
    }
	
	/**
	 * 获取类注解
	 * @param thisClass
	 * @return
	 */
	public static Set<String> getAnnotations(Annotation[] annotations) {
		Set<String> anns = new HashSet<>();
		for(Annotation ann : annotations) {
			anns.add(ann.getClass().getTypeName());
		}
    	return anns;
    }
	
	/**
     * 获取类的父类及祖类，不包含Object
     * @param cls
     * @return
     */
    public static Set<String> getSuperClass(Class<?> cls) {
    	Set<String> clsList = new HashSet<>();
    	while(cls.getSuperclass() != null) {
    		String superCls = cls.getSuperclass().getTypeName();
    		if("java.lang.Object".equals(superCls)) {
    			break;
    		}
    		clsList.add(superCls);
    		cls = cls.getSuperclass();
        }
    	return clsList;
    }
	
	/**
	 * 获取类的所有接口
	 * @param cls
	 * @return
	 */
	public static Set<String> getInterface(Class<?> cls) {
    	Set<String> interfaces = new HashSet<>();
    	while(cls != null) {
    		if("java.lang.Object".equals(cls.getTypeName())) {
    			break;
    		}
    		interfaces.addAll(getClassInterface(cls));
    		if(cls.getSuperclass() == null) {
    			break;
    		}
    		cls = cls.getSuperclass();
        }
    	return interfaces;
    }
	
	/**
	 * 获取直接接口
	 * @param cls
	 * @return
	 */
	public static Set<String> getClassInterface(Class<?> cls) {
    	Set<String> clsList = new HashSet<>();
    	if(cls.getInterfaces() == null || cls.getInterfaces().length == 0) {
    		return clsList;
    	}
    	for(Class<?> inter : cls.getInterfaces()) {
    		clsList.add(inter.getName());
			clsList.addAll(getClassInterface(inter));
    	}
    	return clsList;
    }
}
