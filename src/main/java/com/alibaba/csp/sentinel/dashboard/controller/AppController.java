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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ServiceDetailEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ServiceInterfaceDetail;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MachineInfoVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.RequestRecordVo;
import com.alibaba.csp.sentinel.dashboard.repository.metric.JpaEntityManager;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

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
    private JpaEntityManager em;
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
    	 
    	 List<RequestRecordVo> requestRecordVos = getRequestRecordVos(metricMap);
    	 Result<List<RequestRecordVo>> result =  Result.ofSuccess(requestRecordVos);
		return result;
    }
     
    
	@GetMapping(value = "/servicedetail.json")
    public Result<List<ServiceDetailEntity>> getServiceDetails() {
    	log.info("调用的接口：  "+"/app/servicedetail.json");
    	List<ServiceDetailEntity>  serviceDetails = em.findByCondition(ServiceDetailEntity.class, " serviceStatus=1 ", new Object[] {});
    	Result<List<ServiceDetailEntity>> result =  Result.ofSuccess(serviceDetails);
		return result;
    }
    @GetMapping(value = "/request/detail")
    public Result<List<MetricEntity>> getRequestDetails(String resource) {
    	log.info("调用的接口：  "+"/app/request/detail");
    	List<MetricEntity>  serviceDetails = em.findByCondition(MetricEntity.class, " resource =?1  ", new Object[] {resource});
    	Result<List<MetricEntity>> result =  Result.ofSuccess(serviceDetails);
		return result;
    }
    @GetMapping(value = "/list/service/{id}/{name}")
    public Result<List<ServiceInterfaceDetail>> getServiceListByServiceRecord(@PathVariable(name = "id") Long id,@PathVariable(name = "name") String name) {
    	log.info("调用的接口：  "+"/app/list/service");
    	if(StringUtils.isBlank(String.valueOf(id))||StringUtils.isBlank(name)) {//服务的id
    		log.error("/app/list/service 检查该接口，参数为空");
    		return null;
    	}
	    	String serviceNameMatch = "/"+name+"/";
	    	Result<List<ServiceInterfaceDetail>> result = null ;
    	 try {
    		 List<MetricEntity> metrics = em.findByCondition(MetricEntity.class, " resource like '%"+serviceNameMatch+"%'", new Object[] {});
     		if(CollectionUtils.isEmpty(metrics))
     			return Result.ofSuccessMsg("未找到该服务的相关接口");
         	log.info("正在统计接口的服务的名称："+metrics.get(0).getResource().split("/")[1]);
         	 Map<String, List<MetricEntity>>  metricMap = metrics.stream()
         			 .filter(MetricEntity ->  MetricEntity.getResource().split("/")[1].equals(name) )
         			 .collect(Collectors.groupingBy(MetricEntity::getResource));
         	 List<ServiceInterfaceDetail> interfaceDetails =  statisticsServiceInterface(metricMap,id);
//	    	List<ServiceInterfaceDetail>  serviceDetails = em.findByCondition(ServiceInterfaceDetail.class, " serviceId =?1 and serviceStatus = 1 ", new Object[] {String.valueOf(id)});
//	    	Object[] arr = new Object[serviceDetails.size()] ;
//	    	for (int i = 0; i < serviceDetails.size(); i++) {
//	    		arr[i] = serviceDetails.get(i).getUrlAddress();
//			}
//	    	List<MetricEntity> metrics = em.findByConditionIN("resource", arr, MetricEntity.class)   ; 	
//	    	//刚开始 按照现有的统计出的接口进行更新数据  点击重新统计以后再统计所有接口 然后只新增无的接口
//			 Map<String, List<MetricEntity>>  metricMap = metrics.stream()
//	    			 .collect(Collectors.groupingBy(MetricEntity::getResource));
//	    	 List<ServiceInterfaceDetail> interfaceDetails =  statisticsServiceInterface(metricMap,id);
//	    	 updateServiceInterface(interfaceDetails,serviceDetails);
			em.persistentBatch(interfaceDetails);
			result =  Result.ofSuccess(interfaceDetails);
			return result;
		} catch (SQLException e) {
			log.error("获取接口详细信息失败，服务id："+id);
			return Result.ofSuccess(null);
		}
    }
    @GetMapping(value = "/service/interface/restatistics")
    public Result<List<ServiceInterfaceDetail>> restatisticsServiceInterface( Long id,String name,boolean status) throws SQLException {
    	
    	
    	
    	log.info("调用的接口：  "+"/app/service/interface/restatistics");
    	
    	Result<List<ServiceInterfaceDetail>> result = getServiceListByServiceRecord(id, name);
    	
//    	if(StringUtils.isBlank(String.valueOf(id))||StringUtils.isBlank(name)) {
//    		log.error("/app/service/interface/restatistics 检查该接口，参数为空");
//    		return null;d
//    	}
//    	String serviceNameMatch = "/"+name+"/";
//    	Result<List<ServiceInterfaceDetail>> result = null ;
//    	try {
//    		List<MetricEntity> metrics = em.findByCondition(MetricEntity.class, " resource like '%"+serviceNameMatch+"%'", new Object[] {});
//    		if(CollectionUtils.isEmpty(metrics))
//    			return Result.ofSuccessMsg("未找到该服务的相关接口");
//        	log.info("正在统计接口的服务的名称："+metrics.get(0).getResource().split("/")[1]);
//        	 Map<String, List<MetricEntity>>  metricMap = metrics.stream()
//        			 .filter(MetricEntity ->  MetricEntity.getResource().split("/")[1].equals(name) )
//        			 .collect(Collectors.groupingBy(MetricEntity::getResource));
//        	 List<ServiceInterfaceDetail> interfaceDetails =  statisticsServiceInterface(metricMap,id);
//        		List<ServiceInterfaceDetail>  serviceDetails = em.findByCondition(ServiceInterfaceDetail.class, " serviceId =?1 and serviceStatus = 1 ", new Object[] {String.valueOf(id)});
//        	 if(status) {//如果为true  删除历史统计   
//        		 em.delete(ServiceInterfaceDetail.class, " serviceId =?1 and serviceStatus = 1 ", new Object[] {String.valueOf(id)});
//        		 serviceDetails.clear();
//        	 }else {//为false  不删除 只更新
////        		 em.executeUpdate(" update ServiceInterfaceDetail set serviceStatus = 0  where serviceId = "+String.valueOf(id));
//        		 compareAndRemoveServiceInterfaceDetail(interfaceDetails,serviceDetails);
//        	 }
//        	 //先查数据表中该服务下的所有接口  只添加上新统计的即可
//        	em.persistentBatch( interfaceDetails);
//        	interfaceDetails.addAll(serviceDetails);
//        	result =  Result.ofSuccess(interfaceDetails);
//    	}catch(Exception e) {
//    		log.error("统计该服务的接口出现异常：服务id为："+id);
//    		return null;
//    	}
		return result;
    }
    
    
   

	@PutMapping(value = "/service/interface/save")
    public Result<String> editInterface( Long id,String urlName,String description) throws SQLException {
    	log.info("调用的接口：  "+"/app/service/interface/save");
    	try {
    		ServiceInterfaceDetail detail = em.findById(id, ServiceInterfaceDetail.class);
        	if(detail!=null) {
        		detail.setUrlName(urlName);
        		detail.setDescription(description);
        	}
        	em.createOrUpdate(detail, ServiceInterfaceDetail.class);
        	return Result.ofSuccessMsg("success");
    	}catch(Exception e) {
    		return Result.ofFail(500, "操作失败，该条数据异常");
    	}
    	
    }
    
    
    
    
   
    /**
     * 比较新统计数据和上一次统计数据的区别  去掉相同的
     * @param interfaceDetails  最新统计
     * @param serviceDetails 上一次统计
     */
	private void compareAndRemoveServiceInterfaceDetail(List<ServiceInterfaceDetail> interfaceDetails,
			List<ServiceInterfaceDetail> serviceDetails) {
		List<String> urlNames = serviceDetails.stream().map(ServiceInterfaceDetail::getUrlAddress).collect(Collectors.toList());
		Iterator<ServiceInterfaceDetail> newDetail =  interfaceDetails.iterator();
		while(newDetail.hasNext()) {
			ServiceInterfaceDetail next = newDetail.next();
			if(urlNames.contains(next.getUrlAddress())) {
				newDetail.remove();
			}
		}
	}

	@PutMapping(value = "/service/detail/save.json" )//改为post
    public Result<List<ServiceDetailEntity>> saveServiceDetail(String id,String serviceName,String description,String manufacturer
    		,String chargePerson,String maintainPerson) throws Exception {
    	ServiceDetailEntity entity = new ServiceDetailEntity();
    	if(StringUtils.isNotBlank(id)) {
    		entity.setId(Long.valueOf(id));
    	}
    	try {
    		entity.setChargePerson(chargePerson);
    		entity.setDescription(description);
    		entity.setMaintainPerson(maintainPerson);
    		entity.setManufacturer(manufacturer);
    		entity.setServiceName(serviceName);
    		entity.setCreateDate(new Date());
        	em.createOrUpdate(entity,ServiceDetailEntity.class);
    	}catch(Exception e) {
    		throw new Exception("服务新增失败");
    	}
    	 List<ServiceDetailEntity> requestServiceDetails = Arrays.asList(entity);
    	Result<List<ServiceDetailEntity>> result =  Result.ofSuccess(requestServiceDetails);
    	return result;
    }
   
    @RequestMapping(value = "/servicedetail/remove/{id}",method = RequestMethod.DELETE)
    public Result<String> removeServiceRecord( @PathVariable(name = "id") Long id ) {
        if (StringUtils.isBlank(id.toString())) {
            return Result.ofSuccess(null);
        }
         try {
        	  em.executeUpdate("update ServiceDetailEntity set serviceStatus = 0 where id ="+id);
              return Result.ofSuccessMsg("success");
         }catch(Exception e) {
        	 return Result.ofFail(1, "remove failed");
         }
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
    
    /**
     * 对接口进行统计
     * @param metricMap
     * @param interfaceDetails
     * @param id
     * @return 
     */
    private List<ServiceInterfaceDetail> statisticsServiceInterface(Map<String, List<MetricEntity>> metricMap,
			 Long id) {
    	 List<ServiceInterfaceDetail> interfaceDetails = new ArrayList<>();
    	 Date createDate = new Date();
    	 for(Entry<String, List<MetricEntity>> entry:metricMap.entrySet()) {
    		 ServiceInterfaceDetail detail = new ServiceInterfaceDetail();
    		 detail.setUrlAddress(entry.getKey());;
    		 detail.setServiceId(String.valueOf(id));
    		 detail.setCreateDate(createDate);
    		 int success_qps = 0; 
    		 int pass_qps = 0;
    		 int exception_qps = 0;
    		 double spendTime = 0;
    		 int countNum = 0;
    		 List<MetricEntity> mapValues = entry.getValue();
    		 for (MetricEntity entity : mapValues) {
    			 countNum += entity.getCount();
    			 success_qps += entity.getSuccessQps();
    			 pass_qps += entity.getPassQps();
    			 exception_qps += entity.getExceptionQps();
				spendTime += entity.getRt();
			}
    		 detail.setCountNum(countNum);
    		 detail.setException_qps(exception_qps);
    		 detail.setPass_qps(pass_qps);
    		 detail.setSuccess_qps(success_qps);
    		 detail.setSpendTime(spendTime);
    		 interfaceDetails.add(detail);
    	 }
    	 interfaceDetails = interfaceDetails.stream()
    			 .sorted(Comparator.comparing(ServiceInterfaceDetail::getCountNum).reversed())
    			 .collect(Collectors.toList());
    	 int ranking = 1;
    	for (ServiceInterfaceDetail vo : interfaceDetails) {
    		vo.setRanking(ranking);
    		ranking++;
		}
		return interfaceDetails;
	}
    
    /**
     * 更新接口信息
     * @param interfaceDetails 新统计的接口信息
     * @param serviceDetails 上一次统计的接口信息
     */
    private void updateServiceInterface(List<ServiceInterfaceDetail> interfaceDetails,
			List<ServiceInterfaceDetail> serviceDetails) {
    		for (ServiceInterfaceDetail detail : serviceDetails) {
				for (ServiceInterfaceDetail newDetail : interfaceDetails) {
					if(newDetail.getUrlAddress().equals(detail.getUrlAddress())) {
						detail.setCountNum(newDetail.getCountNum());
						detail.setException_qps(newDetail.getException_qps());
						detail.setPass_qps(newDetail.getPass_qps());
						detail.setRanking(newDetail.getRanking());
						detail.setSpendTime(newDetail.getSpendTime());
						detail.setSuccess_qps(newDetail.getSuccess_qps());
					}
				}
			}
	}
    /**
     * 构造前台显示的vo
     * @param metricMap
     * @return
     */
    private List<RequestRecordVo> getRequestRecordVos(Map<String, List<MetricEntity>> metricMap) {
    	List<RequestRecordVo> requestRecordVos = new ArrayList<>();
    	 for(Entry<String, List<MetricEntity>> entry:metricMap.entrySet()) {
    		 RequestRecordVo recordVo = new RequestRecordVo();
    		 recordVo.setResource(entry.getKey());
    		 int success_qps = 0; 
    		 int pass_qps = 0;
    		 int exception_qps = 0;
    		 double spendTime = 0;
    		 int countNum = 0;
    		 List<MetricEntity> mapValues = entry.getValue();
    		 for (MetricEntity entity : mapValues) {
    			 countNum += entity.getCount();
    			 success_qps += entity.getSuccessQps();
    			 pass_qps += entity.getPassQps();
    			 exception_qps += entity.getExceptionQps();
				spendTime += entity.getRt();
			}
    		 recordVo.setCountNum(countNum);
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
    	setManufacturer(requestRecordVos);
		return requestRecordVos;
	}
    /**
     * 根据接口地址查找所属的厂家
     * @param requestRecordVos
     */
	private void setManufacturer(List<RequestRecordVo> requestRecordVos) {
			for (RequestRecordVo vo : requestRecordVos) {
				List<ServiceInterfaceDetail> details = em.findByCondition(ServiceInterfaceDetail.class, " urlAddress =?1 and serviceStatus = 1 ", new Object[] {vo.getResource()});
				if(!CollectionUtils.isEmpty(details)&&StringUtils.isNotBlank(details.get(0).getServiceId())) {
					ServiceDetailEntity  service = em.findById(Long.valueOf(details.get(0).getServiceId()), ServiceDetailEntity.class);
					if(service!=null&&StringUtils.isNotBlank(service.getManufacturer())){
						vo.setManufacturer(service.getManufacturer());
					}else {
						vo.setManufacturer("--");
					}
				}else {
					vo.setManufacturer("--");
				}
			}
	}

    
}
