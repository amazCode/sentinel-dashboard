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
package com.alibaba.csp.sentinel.dashboard.repository.rule;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.metric.JpaEntityManager;
import com.alibaba.csp.sentinel.dashboard.util.IdAcquireUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 
 */
@Transactional
@Repository("ruleRepositoryAdapter")
public  class RuleRepositoryAdapter<T extends RuleEntity> implements RuleRepository<T, Long> {
//	 private static AtomicLong ids = new AtomicLong(0);
    /**
     * {@code <machine, <id, rule>>}
     */
//    private Map<MachineInfo, Map<Long, T>> machineRules = new ConcurrentHashMap<>(16);
//    private Map<Long, T> allRules = new ConcurrentHashMap<>(16);
//
//    private Map<String, Map<Long, T>> appRules = new ConcurrentHashMap<>(16);
//
//    private static final int MAX_RULES_SIZE = 10000;

    @Autowired
    private JpaEntityManager em;
    
    
    @Override
    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(nextId());
        }
      T t = em.createOrUpdate(entity);

        return t;
    }

    @Override
    public List<T> saveAll(List<T> rules) {
        // TODO: check here.
        if (rules == null) {
            return null;
        }
        List<T> savedRules = new ArrayList<>(rules.size());
        for (T rule : rules) {
            savedRules.add(save(rule));
        }
        return savedRules;
    }

    @Override
    public T delete(Long id,Class<T> t) {
		T entity = (T) em.findById(id, t);
        if (entity != null) {
        	em.delete(entity);
        }
        return entity;
    }

	@Override
    public T findById(Long id,Class<T> t) {
        return  em.findById(id, t);
    }

    @Override
    public List<T> findAllByMachine(MachineInfo machineInfo) {
//        Map<Long, T> entities = machineRules.get(machineInfo);
//        if (entities == null) {
            return new ArrayList<>();
//        }
//        return new ArrayList<>(entities.values());
    }

	@Override
    public List<T> findAllByApp(String appName,Class<T> t) {
        AssertUtil.notEmpty(appName, "appName cannot be empty");
//        Map<Long, T> entities = appRules.get(appName);
        List<T> values = em.findByCondition(t, " app =?1 ", new Object[] {appName});
        if (values == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(values);
    }
    
    protected long nextId() {
        return IdAcquireUtil.getID();
    }

}
