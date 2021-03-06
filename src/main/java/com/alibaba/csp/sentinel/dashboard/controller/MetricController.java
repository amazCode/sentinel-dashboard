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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricVo;

/**
 * @author leyou
 */
@Controller
@RequestMapping(value = "/metric", produces = MediaType.APPLICATION_JSON_VALUE)
public class MetricController {

    private static Logger logger = LoggerFactory.getLogger(MetricController.class);

    private static final long maxQueryIntervalMs = 1000 * 60 * 60;

    @Autowired
    @Qualifier("jpaMetricsRepository")
    private MetricsRepository<MetricEntity> metricStore;

    @ResponseBody
    @RequestMapping("/queryTopResourceMetric.json")
    public Result<?> queryTopResourceMetric(final String app,
                                            Integer pageIndex,
                                            Integer pageSize,
                                            Boolean desc,
                                            Long startTime, Long endTime, String searchKey) {
    	
    	List<String> topResource = new ArrayList<>();
        final Map<String, Iterable<MetricVo>> map = new ConcurrentHashMap<>();
     
     Integer totalCount = 0;
     Integer totalPage = 0;
        totalCount = metricStore.countByTime(searchKey);
        if (totalCount==0){
            return Result.ofSuccess(null);
        }
        totalPage = (totalCount + pageSize - 1) / pageSize;
        if(StringUtil.isNotBlank(searchKey)&&totalPage==1){
            pageIndex = 1;
        }
            List<MetricEntity> results = metricStore.queryByTime(pageIndex,pageSize,searchKey,null,null);
            List<MetricEntity> entities = new ArrayList<>();
            for (MetricEntity metric : results) {
  	    	  Date whenTime = new Date(System.currentTimeMillis() - 6 * 3600 * 1000);
  	         if (metric.getGmtCreate().after(whenTime)) {
  	        	entities.add(metric);
  	         }
  	        }
            if (entities!=null && entities.size()!=0) {
               for (MetricEntity entity:entities) {
                  String res = entity.getResource();
              List<MetricVo> vos = MetricVo.fromMetricEntities(entities, res);
              map.put(res, vos);
             }
             topResource = entities.stream().map(MetricEntity::getResource).collect(Collectors.toList());
     
            }
     if (topResource == null || topResource.isEmpty()) {
      return Result.ofSuccess(null);
     }
     
        Map<String, Object> resultMap = new HashMap<>(16);
        resultMap.put("totalCount", totalCount);
        resultMap.put("totalPage", totalPage);
        resultMap.put("pageIndex", pageIndex);
        resultMap.put("pageSize", pageSize);
     
        Map<String, Iterable<MetricVo>> map2 = new LinkedHashMap<>();
        // order matters.
        for (String identity : topResource) {
            map2.put(identity, map.get(identity));
        }
        resultMap.put("metric", map2);
        return Result.ofSuccess(resultMap);

    	
//        if (StringUtil.isEmpty(app)) {
//            return Result.ofFail(-1, "app can't be null or empty");
//        }
//        if (pageIndex == null || pageIndex <= 0) {
//            pageIndex = 1;
//        }
//        if (pageSize == null) {
//            pageSize = 6;
//        }
//        if (pageSize >= 20) {
//            pageSize = 20;
//        }
//        if (desc == null) {
//            desc = true;
//        }
//        if (endTime == null) {
//            endTime = System.currentTimeMillis();
//        }
//        if (startTime == null) {
//            startTime = endTime - 1000 * 60 * 5;
//        }
//        if (endTime - startTime > maxQueryIntervalMs) {
//            return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
//        }
//        List<String> resources = metricStore.listResourcesOfApp(app);
//        logger.debug("queryTopResourceMetric(), resources.size()={}", resources.size());
//
//        if (resources == null || resources.isEmpty()) {
//            return Result.ofSuccess(null);
//        }
//        if (!desc) {
//            Collections.reverse(resources);
//        }
//        if (StringUtil.isNotEmpty(searchKey)) {
//            List<String> searched = new ArrayList<>();
//            for (String resource : resources) {
//                if (resource.contains(searchKey)) {
//                    searched.add(resource);
//                }
//            }
//            resources = searched;
//        }
//        int totalPage = (resources.size() + pageSize - 1) / pageSize;
//        List<String> topResource = new ArrayList<>();
//        if (pageIndex <= totalPage) {
//            topResource = resources.subList((pageIndex - 1) * pageSize,
//                Math.min(pageIndex * pageSize, resources.size()));
//        }
//        final Map<String, Iterable<MetricVo>> map = new ConcurrentHashMap<>();
//        logger.debug("topResource={}", topResource);
//        long time = System.currentTimeMillis();
//        for (final String resource : topResource) {
//            List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(
//                app, resource, startTime, endTime);
//            logger.debug("resource={}, entities.size()={}", resource, entities == null ? "null" : entities.size());
//            List<MetricVo> vos = MetricVo.fromMetricEntities(entities, resource);
//            Iterable<MetricVo> vosSorted = sortMetricVoAndDistinct(vos);
//            map.put(resource, vosSorted);
//        }
//        logger.debug("queryTopResourceMetric() total query time={} ms", System.currentTimeMillis() - time);
//        Map<String, Object> resultMap = new HashMap<>(16);
//        resultMap.put("totalCount", resources.size());
//        resultMap.put("totalPage", totalPage);
//        resultMap.put("pageIndex", pageIndex);
//        resultMap.put("pageSize", pageSize);
//
//        Map<String, Iterable<MetricVo>> map2 = new LinkedHashMap<>();
//        // order matters.
//        for (String identity : topResource) {
//            map2.put(identity, map.get(identity));
//        }
//        resultMap.put("metric", map2);
//        return Result.ofSuccess(resultMap);
    }
    
    
    @ResponseBody
    @RequestMapping("/queryResourceMetricByTime")
    public Result<?> queryResourceMetricByTime(final String app,
                                            Integer pageIndex,
                                            Integer pageSize,
                                            Boolean desc,
                                            String searchStartTime, String searchEndTime, String searchKey) throws ParseException {
    	
    	List<String> topResource = new ArrayList<>();
    	 
    	
    	Date startTime =  getTime(searchStartTime);  
    	Date endTime = getTime(searchEndTime);
        final Map<String, Iterable<MetricVo>> map = new ConcurrentHashMap<>();
     
     Integer totalCount = 0;
     Integer totalPage = 0;
//        totalCount = metricStore.countByTime(searchKey);
//        if (totalCount==0){
//            return Result.ofSuccess(null);
//        }
        totalPage = (totalCount + pageSize - 1) / pageSize;
        if(StringUtil.isNotBlank(searchKey)&&totalPage==1){
            pageIndex = 1;
        }
            List<MetricEntity> entities = metricStore.queryByTime(pageIndex,pageSize,searchKey,startTime,endTime);
            if (entities!=null && entities.size()!=0) {
               for (MetricEntity entity:entities) {
                  String res = entity.getResource();
              List<MetricVo> vos = MetricVo.fromMetricEntities(entities, res);
              map.put(res, vos);
             }
             topResource = entities.stream().map(MetricEntity::getResource).collect(Collectors.toList());
     
            }
     if (topResource == null || topResource.isEmpty()) {
      return Result.ofSuccess(null);
     }
     
        Map<String, Object> resultMap = new HashMap<>(16);
        resultMap.put("totalCount", totalCount);
        resultMap.put("totalPage", totalPage);
        resultMap.put("pageIndex", pageIndex);
        resultMap.put("pageSize", pageSize);
     
        Map<String, Iterable<MetricVo>> map2 = new LinkedHashMap<>();
        // order matters.
        for (String identity : topResource) {
            map2.put(identity, map.get(identity));
        }
        resultMap.put("metric", map2);
        return Result.ofSuccess(resultMap);

    }
    
    

    private Date getTime(String searchTime) throws ParseException {
		// TODO Auto-generated method stub
    	Date date = null;
    	SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	if(!"Invalid date".equals(searchTime)) {
    		date = format.parse(searchTime);
    	}
		return date;
	}


	@ResponseBody
    @RequestMapping("/queryByAppAndResource.json")
    public Result<?> queryByAppAndResource(String app, String identity, Long startTime, Long endTime) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(identity)) {
            return Result.ofFail(-1, "identity can't be null or empty");
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - 1000 * 60;
        }
        if (endTime - startTime > maxQueryIntervalMs) {
            return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
        }
        List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(
            app, identity, startTime, endTime);
        List<MetricVo> vos = MetricVo.fromMetricEntities(entities, identity);
        return Result.ofSuccess(sortMetricVoAndDistinct(vos));
    }

    private Iterable<MetricVo> sortMetricVoAndDistinct(List<MetricVo> vos) {
        if (vos == null) {
            return null;
        }
        Map<Long, MetricVo> map = new TreeMap<>();
        for (MetricVo vo : vos) {
            MetricVo oldVo = map.get(vo.getTimestamp());
            if (oldVo == null || vo.getGmtCreate() > oldVo.getGmtCreate()) {
                map.put(vo.getTimestamp(), vo);
            }
        }
        return map.values();
    }
}
