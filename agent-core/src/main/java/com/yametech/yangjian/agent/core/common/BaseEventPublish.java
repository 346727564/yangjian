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
package com.yametech.yangjian.agent.core.common;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.yametech.yangjian.agent.api.IAppStatusListener;
import com.yametech.yangjian.agent.api.IConfigReader;
import com.yametech.yangjian.agent.api.ISchedule;
import com.yametech.yangjian.agent.api.base.IReportData;
import com.yametech.yangjian.agent.api.bean.MetricData;
import com.yametech.yangjian.agent.api.common.CustomThreadFactory;
import com.yametech.yangjian.agent.api.common.MultiReportFactory;
import com.yametech.yangjian.agent.api.log.ILogger;
import com.yametech.yangjian.agent.api.log.LoggerFactory;
import com.yametech.yangjian.agent.core.util.Util;
import com.yametech.yangjian.agent.util.eventbus.EventBusBuilder;
import com.yametech.yangjian.agent.util.eventbus.consume.ConsumeConfig;
import com.yametech.yangjian.agent.util.eventbus.process.EventBus;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 事件缓存基类，附带metric
 * @Description 
 * 
 * @author liuzhao
 * @date 2020年4月3日 下午4:52:35 
 * @param <T>
 */
public abstract class BaseEventPublish<T> implements IAppStatusListener, ISchedule, IConfigReader {
    private static final ILogger log = LoggerFactory.getLogger(BaseEventPublish.class);
    private static final int MIN_BUFFER_SIZE = 1;
    private static final String BUFFER_SIZE_KEY_PREFIX = "bufferSize.";
    private static final String INTERVAL_KEY_PREFIX = "metricOutput.interval.publish.";
    private static final String DISCARD_KEY_PREFIX = "eventPublish.discard.";
    private IReportData report = MultiReportFactory.getReport("eventPublish");
    private EventBus<T> eventBus;
    private String metricType;
    private String configKeySuffix;
    private int bufferSize = 1 << 6;
    private int interval = 10;
    private boolean discard = true;
    
    public BaseEventPublish(EventBusType configKeySuffix) {
    	this.metricType = configKeySuffix.getMetricType();
    	this.configKeySuffix = configKeySuffix.getConfigKeySuffix();
	}
    
    @Override
    public Set<String> configKey() {
    	return new HashSet<>(Arrays.asList(BUFFER_SIZE_KEY_PREFIX.replaceAll("\\.", "\\\\.") + configKeySuffix, 
    			INTERVAL_KEY_PREFIX.replaceAll("\\.", "\\\\.") + configKeySuffix, 
    			DISCARD_KEY_PREFIX.replaceAll("\\.", "\\\\.") + configKeySuffix));
    }
    
    @Override
    public void configKeyValue(Map<String, String> kv) {
    	if (kv == null) {
            return;
        }
    	String bufferSizeStr = kv.get(BUFFER_SIZE_KEY_PREFIX + configKeySuffix);
    	if(bufferSizeStr != null) {
    		try {
            	int bufferSizeConfig = Integer.parseInt(bufferSizeStr);
            	if(bufferSizeConfig > MIN_BUFFER_SIZE) {
            		this.bufferSize = bufferSizeConfig;
            	}
            } catch(Exception e) {
            	log.warn("{} config error: {}", BUFFER_SIZE_KEY_PREFIX + configKeySuffix, bufferSizeStr);
            }
    	}
    	
    	String intervalStr = kv.get(INTERVAL_KEY_PREFIX + configKeySuffix);
    	if(intervalStr != null) {
    		try {
    			interval = Integer.parseInt(intervalStr);
            } catch(Exception e) {
            	log.warn("{} config error: {}", INTERVAL_KEY_PREFIX + configKeySuffix, intervalStr);
            }
    	}
    	
    	String discardStr = kv.get(DISCARD_KEY_PREFIX + configKeySuffix);
    	if(discardStr != null) {
    		try {
    			discard = Boolean.parseBoolean(discardStr);
            } catch(Exception e) {
            	log.warn("{} config error: {}", DISCARD_KEY_PREFIX + configKeySuffix, discardStr);
            }
    	}
    }
    
    protected abstract List<ConsumeConfig<T>> consumes();
    
    @Override
    public void beforeRun() {
        @SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) Util.superClassGeneric(this.getClass(), 0);
        eventBus = EventBusBuilder
                .create(this::consumes)
                .parallelPublish(true)
                .executor(new CustomThreadFactory(metricType, true))
                .bufferSize(bufferSize)
                .waitStrategy(new BlockingWaitStrategy())
//				.waitStrategy(new YieldingWaitStrategy())
                .setDiscardFull(discard)
                .build(cls, new ExceptionHandler<T>() {
                    @Override
                    public void handleEventException(Throwable e, long sequence, T callEvent) {
                        log.error(e, "Disruptor handleEvent({}) error.", callEvent);
                    }

                    @Override
                    public void handleOnStartException(Throwable e) {
                        log.error(e, "Disruptor start error.");
                    }

                    @Override
                    public void handleOnShutdownException(Throwable e) {
                        log.error(e, "Disruptor shutdown error.");
                    }
                });
        log.info("{} eventPublish inited.", metricType);
    }

    public boolean publish(Consumer<T> consumer) {
        if(eventBus == null) {
            log.warn("eventBus未初始化");
            return false;
        }
        return eventBus.publish(event -> {
        	incrTotalNum();
        	if (event == null) {
            	incrDiscardNum();
                return;
            }
        	consumer.accept(event);
        });
    }

    private void incrDiscardNum() {
        discardNum.getAndIncrement();
        periodDiscardNum.getAndIncrement();
    }

    private void incrTotalNum() {
        totalNum.getAndIncrement();
        periodTotalNum.getAndIncrement();
    }

    @Override
    public boolean shutdown(Duration duration) {
    	boolean success = true;
        long currentTotal = 0;
        while (currentTotal < totalNum.get()) {// N毫秒内无调用事件则关闭，避免因关闭服务导致事件丢失
            try {
                currentTotal = totalNum.get();
                //noinspection BusyWait
                Thread.sleep(602L);
            } catch (InterruptedException e) {
                log.warn(e, "shutdown interrupted");
                Thread.currentThread().interrupt();
                success = false;
                break;
            }
        }
        duration = duration == null ? Duration.ofSeconds(10) : duration;
        if (eventBus != null) {
            return eventBus.shutdown(duration) && success;
        }
        return success;
    }

    @Override
    public int interval() {
        return interval;
    }
    
    @Override
    public int weight() {
    	return IAppStatusListener.super.weight() + 10;
    }

    private AtomicLong discardNum = new AtomicLong(0);// 总共丢弃的数据量
    private AtomicLong periodDiscardNum = new AtomicLong(0);// 最近一个输出周期丢弃的数据量
    private AtomicLong totalNum = new AtomicLong(0);// 总产生的事件量
    private AtomicLong periodTotalNum = new AtomicLong(0);// 最近一个输出周期产生的事件量

    @Override
    public void execute() {
        long periodTotal = periodTotalNum.getAndSet(0);
        long periodDiscard = periodDiscardNum.getAndSet(0);
        Map<String, Object> params = new HashMap<>();
        params.put("total_num", totalNum.get());
        params.put("period_seconds", interval);
        params.put("period_num", periodTotal);
        params.put("total_discard_num", discardNum.get());
        params.put("period_discard_num", periodDiscard);
        MetricData metricData = MetricData.get(null, "product/" + metricType, params);
        if(!report.report(metricData)) {
        	log.warn("report failed {}", metricData);
        }
    }
}
