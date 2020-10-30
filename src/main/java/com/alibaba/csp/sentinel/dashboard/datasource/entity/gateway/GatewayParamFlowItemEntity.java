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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.IdentifiedEntity;

import java.util.Date;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entity for {@link GatewayParamFlowItem}.
 *
 * @author cdfive
 * @since 1.7.0
 */
@Entity
@Table(name = "t_gateway_param_flow_item")
public class GatewayParamFlowItemEntity extends IdentifiedEntity{
//    @Id
//    @GeneratedValue  已经继承了IdentifiedEntity 这里记得去掉
//	private Long id;
	
    private Integer parseStrategy;

    private String fieldName;

    private String pattern;

    private Integer matchStrategy;
    
    private Date gmtCreate;
    private Date gmtModified;
    
    public Integer getParseStrategy() {
        return parseStrategy;
    }

    public void setParseStrategy(Integer parseStrategy) {
        this.parseStrategy = parseStrategy;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMatchStrategy() {
        return matchStrategy;
    }

    public void setMatchStrategy(Integer matchStrategy) {
        this.matchStrategy = matchStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        GatewayParamFlowItemEntity that = (GatewayParamFlowItemEntity) o;
        return Objects.equals(parseStrategy, that.parseStrategy) &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(pattern, that.pattern) &&
                Objects.equals(matchStrategy, that.matchStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parseStrategy, fieldName, pattern, matchStrategy);
    }

    @Override
    public String toString() {
        return "GatewayParamFlowItemEntity{" +
                "parseStrategy=" + parseStrategy +
                ", fieldName='" + fieldName + '\'' +
                ", pattern='" + pattern + '\'' +
                ", matchStrategy=" + matchStrategy +
                '}';
    }

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

}
