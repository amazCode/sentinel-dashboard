package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.StringUtil;



@Transactional
@Repository("jpaMetricsRepository")
public class JpaMetricsRepository implements MetricsRepository<MetricEntity> {

	
	@PersistenceContext
    private EntityManager em;
	
	
	@Override
	public void save(MetricEntity metric) {
		 if (metric == null || StringUtil.isBlank(metric.getApp())) {
	            return;
	        }
	        em.persist(metric);
	}

	@Override
	public void saveAll(Iterable<MetricEntity> metrics) {
		 if (metrics == null) {
	            return;
	        }
	        metrics.forEach(this::save);
	}

	@Override
	public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
		 List<MetricEntity> results = new ArrayList<MetricEntity>();
	        if (StringUtil.isBlank(app)) {
	            return results;
	        }
	 
	        if (StringUtil.isBlank(resource)) {
	            return results;
	        }
	 
	        StringBuilder hql = new StringBuilder();
	        hql.append("FROM MetricEntity");
	        hql.append(" WHERE app=:app");
	        hql.append(" AND resource=:resource");
	        hql.append(" AND timestamp>=:startTime");
	        hql.append(" AND timestamp<=:endTime");
	 
	        Query query = em.createQuery(hql.toString());
	        query.setParameter("app", app);
	        query.setParameter("resource", resource);
	        query.setParameter("startTime", Date.from(Instant.ofEpochMilli(startTime)));
	        query.setParameter("endTime", Date.from(Instant.ofEpochMilli(endTime)));
	 
	        List<MetricEntity> metrics = query.getResultList();
	        if (CollectionUtils.isEmpty(metrics)) {
	            return results;
	        }
	 
	        for (MetricEntity metric : metrics) {
	            results.add(metric);
	        }
	        return results;

	}

	@Override
	public List<String> listResourcesOfApp(String app) {
		 List<String> results = new ArrayList<>();
	        if (StringUtil.isBlank(app)) {
	            return results;
	        }
	 
	        StringBuilder hql = new StringBuilder();
	        hql.append("FROM MetricEntity");
	        hql.append(" WHERE app=:app");
	        hql.append(" AND timestamp>=:startTime");
	 
	        long startTime = System.currentTimeMillis() - 1000 * 60;
	        Query query = em.createQuery(hql.toString());
	        query.setParameter("app", app);
	        query.setParameter("startTime", Date.from(Instant.ofEpochMilli(startTime)));
	 
	        List<MetricEntity> metrics = query.getResultList();
	        if (CollectionUtils.isEmpty(metrics)) {
	            return results;
	        }
	 
	        List<MetricEntity> metricEntities = new ArrayList<MetricEntity>();
	        for (MetricEntity metric : metrics) {
	            metricEntities.add(metric);
	        }
	 
	        Map<String, MetricEntity> resourceCount = new HashMap<>(32);
	 
	        for (MetricEntity metricEntity : metricEntities) {
	            String resource = metricEntity.getResource();
	            if (resourceCount.containsKey(resource)) {
	                MetricEntity oldEntity = resourceCount.get(resource);
	                oldEntity.addPassQps(metricEntity.getPassQps());
	                oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
	                oldEntity.addBlockQps(metricEntity.getBlockQps());
	                oldEntity.addExceptionQps(metricEntity.getExceptionQps());
	                oldEntity.addCount(1);
	            } else {
	                resourceCount.put(resource, MetricEntity.copyOf(metricEntity));
	            }
	        }
	 
	        // Order by last minute b_qps DESC.
	        return resourceCount.entrySet()
	                .stream()
	                .sorted((o1, o2) -> {
	                    MetricEntity e1 = o1.getValue();
	                    MetricEntity e2 = o2.getValue();
	                    int t = e2.getBlockQps().compareTo(e1.getBlockQps());
	                    if (t != 0) {
	                        return t;
	                    }
	                    return e2.getPassQps().compareTo(e1.getPassQps());
	                })
	                .map(Map.Entry::getKey)
	                .collect(Collectors.toList());

	}

	@Override
	public List<MetricEntity> queryByTime(Integer pageIndex, Integer pageSize, String key,Date startTime,Date endTime) {
		 List<MetricEntity> results = new ArrayList<MetricEntity>();
		 SimpleDateFormat simple = new SimpleDateFormat("yyyy/MM/dd HH:MM:SS");
	      StringBuilder hql = new StringBuilder();
	      List<MetricEntity> metrics = new ArrayList<>();
	      try {
	      hql.append("FROM MetricEntity");
	      
	      hql.append(" WHERE   timestamp   >=:startTime  AND    timestamp<=:endTime");
	      if (StringUtil.isNotBlank(key)){
	         hql.append(" AND resource LIKE :key ");
	      }
	      hql.append(" order by timestamp desc");
	      Query query = em.createQuery(hql.toString());
	      query.setMaxResults(pageSize);
	      query.setFirstResult((pageIndex-1)*pageSize);
	      Date currentTime = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()));
	      if(startTime==null&&endTime==null) {
				query.setParameter("startTime",simple.parse("1970/1/1 00:00:00") );
				query.setParameter("endTime", currentTime);
	      }else if(startTime==null&&endTime!=null) {
	    	    query.setParameter("startTime",simple.parse("1970/1/1 00:00:00") );
				query.setParameter("endTime", endTime);
	      }else if(startTime!=null&&endTime==null) {
	    	    query.setParameter("startTime",startTime);
				query.setParameter("endTime", currentTime);
	      }else {
	    	    query.setParameter("startTime",startTime);
				query.setParameter("endTime", endTime);
	      }
	      if (StringUtil.isNotBlank(key)){
	         query.setParameter("key","%"+key+"%");
	      }
	         metrics = query.getResultList();
	      } catch (ParseException e) {
				e.printStackTrace();
			}
	      if (CollectionUtils.isEmpty(metrics)) {
	         return results;
	      }
	      return metrics;
	}

	@Override
	public Integer countByTime(String key) {
		Integer totalCount = 0;
		 
	      StringBuilder hql = new StringBuilder();
	      hql.append("FROM MetricEntity");
	      hql.append(" WHERE timestamp<=:endTime");
	      if (StringUtil.isNotBlank(key)){
	         hql.append(" AND resource LIKE :key ");
	      }
	      Query query = em.createQuery(hql.toString());
	      query.setParameter("endTime", Date.from(Instant.ofEpochMilli(System.currentTimeMillis())));
	      if (StringUtil.isNotBlank(key)){
	         query.setParameter("key","%"+key+"%");
	      }
	      List<MetricEntity> metricPOs = query.getResultList();
	      if (CollectionUtils.isEmpty(metricPOs)) {
	         return totalCount;
	      }
	 
	      for (MetricEntity metricPO : metricPOs) {
	         if (metricPO.getGmtCreate().after(new Date(System.currentTimeMillis() - 6 * 3600 * 1000))) {
	            totalCount++;
	         }
	      }
	 
	      return totalCount;

	}

	@Override
	public void removeAll() {
//		 StringBuilder hql = new StringBuilder();
//	      hql.append("FROM MetricPO");
//	      hql.append(" WHERE timestamp<:time");
//	      Query query = em.createQuery(hql.toString());
//	      query.setParameter("time", Date.from(Instant.ofEpochMilli(System.currentTimeMillis() - 6 * 3600 * 1000)));
//	      List<MetricEntity> metricPOs = query.getResultList();
//	      if (CollectionUtils.isEmpty(metricPOs)) {
//	         return;
//	      }
//	      for (MetricEntity metric : metricPOs) {
//	         //超过6个小时则删除
//	         em.remove(metric);
//	      }

		
	}

	@Override
	public  List<MetricEntity> getRequestRecord() {
		    StringBuilder hql = new StringBuilder();
	        hql.append("FROM MetricEntity");
	        Query query = em.createQuery(hql.toString());
	        List<MetricEntity> metrics =  query.getResultList();
			return metrics;
	}
	
}
