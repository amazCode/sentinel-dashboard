/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MachineInfoVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.RequestRecordVo;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Carpenter Lee
 */
@Slf4j
@RestController
@RequestMapping(value = "/app")
public class AppController {

    @Autowired
    private AppManagement appManagement;
    
    @Autowired
    @Qualifier("jpaMetricsRepository")
    private MetricsRepository<MetricEntity> metricStore;
    

    @GetMapping("/names.json")
    public Result<List<String>> queryApps(HttpServletRequest request) {
        return Result.ofSuccess(appManagement.getAppNames());
    }

    @GetMapping("/briefinfos.json")
    public Result<List<AppInfo>> queryAppInfos(HttpServletRequest request) {
        List<AppInfo> list = new ArrayList<>(appManagement.getBriefApps());
        Collections.sort(list, Comparator.comparing(AppInfo::getApp));
        return Result.ofSuccess(list);
    }

    @GetMapping(value = "/{app}/machines.json")
    public Result<List<MachineInfoVo>> getMachinesByApp(@PathVariable("app") String app) {
        AppInfo appInfo = appManagement.getDetailApp(app);
        if (appInfo == null) {
            return Result.ofSuccess(null);
        }
        List<MachineInfo> list = new ArrayList<>(appInfo.getMachines());
        Collections.sort(list, Comparator.comparing(MachineInfo::getApp).thenComparing(MachineInfo::getIp).thenComparingInt(MachineInfo::getPort));
        return Result.ofSuccess(MachineInfoVo.fromMachineInfoList(list));
    }
    
    @GetMapping(value = "/{app}/requestrecord.json")
    public Result<List<RequestRecordVo>> getRequestRecordByApp(@PathVariable("app") String app) {
    	log.info("调用的接口：  "+"/app/{app}/requestrecord.json");
    	List<MetricEntity>  metrics = metricStore.getRequestRecord();
    	 Map<String, List<MetricEntity>>  metricMap = metrics.stream()
    			 .filter(MetricEntity ->  !MetricEntity.getResource().contains("route") )
    			 .collect(Collectors.groupingBy(MetricEntity::getResource));
    	 List<RequestRecordVo> requestRecordVos = new ArrayList<>();
    	 for(Entry<String, List<MetricEntity>> entry:metricMap.entrySet()) {
    		 RequestRecordVo recordVo = new RequestRecordVo();
    		 recordVo.setCountNum(entry.getValue().size()); 
    		 recordVo.setResource(entry.getKey());
    		 int success_qps = 0; 
    		 int pass_qps = 0;
    		 int exception_qps = 0;
    		 double spendTime = 0;
    		 List<MetricEntity> mapValues = entry.getValue();
    		 for (MetricEntity entity : mapValues) {
    			 success_qps += entity.getSuccessQps();
    			 pass_qps += entity.getPassQps();
    			 exception_qps += entity.getExceptionQps();
				spendTime += entity.getRt();
			}
    		 recordVo.setException_qps(exception_qps);
    		 recordVo.setPass_qps(pass_qps);
    		 recordVo.setSuccess_qps(success_qps);
    		 recordVo.setSpendTime(spendTime);
    		 requestRecordVos.add(recordVo);
    	 }
    	 requestRecordVos = requestRecordVos.stream()
    			 .sorted(Comparator.comparing(RequestRecordVo::getCountNum).reversed())
    			 .collect(Collectors.toList());
    	 int ranking = 1;
    	for (RequestRecordVo vo : requestRecordVos) {
    		vo.setRanking(ranking);
    		ranking++;
		}
    	Result<List<RequestRecordVo>> result =  Result.ofSuccess(requestRecordVos);
		return result;
    }
    
    
    
    
    @RequestMapping(value = "/{app}/machine/remove.json")
    public Result<String> removeMachineById(
            @PathVariable("app") String app,
            @RequestParam(name = "ip") String ip,
            @RequestParam(name = "port") int port) {
        AppInfo appInfo = appManagement.getDetailApp(app);
        if (appInfo == null) {
            return Result.ofSuccess(null);
        }
        if (appManagement.removeMachine(app, ip, port)) {
            return Result.ofSuccessMsg("success");
        } else {
            return Result.ofFail(1, "remove failed");
        }
    }
}
