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
package com.yametech.yangjian.agent.core.report;

import com.yametech.yangjian.agent.api.base.IReportData;
import com.yametech.yangjian.agent.api.common.InstanceManage;
import com.yametech.yangjian.agent.core.report.async.ReportPublish;

import java.util.Arrays;
import java.util.List;

/**
 * 注意不要修改路径、类名、构造方法，api中有使用反射获取类实例
 * @Description 
 * 
 * @author liuzhao
 * @date 2020年5月6日 下午10:47:06
 */
public class AsyncMultiReport implements IReportData {
	private String reportType;
	private ReportPublish publish;
	private IReportData report;

	/**
	 * 有反射调用
	 * @param reportConfigKey
	 */
	public AsyncMultiReport(String reportConfigKey) {
		this.reportType = reportConfigKey;
		this.report = MultiReport.getReport(reportConfigKey);
		this.publish = new ReportPublish();
		InstanceManage.registryInit(this.publish);
	}
	
	/**
	 * 根据reportConfigKey获取上报实例，并注册配置通知
	 * @param reportConfigKey	用于读取配置
	 * @return
	 */
//    static IReportData getReport(String reportConfigKey) {
//    	return new AsyncMultiReport(reportConfigKey);
//    }
	
	@Override
	public boolean report(Object data) {
		return batchReport(Arrays.asList(data));
	}
	
	@Override
	public boolean batchReport(List<Object> datas) {
		return publish.publish(event -> event.reset(reportType, report, datas));
	}
	
}
