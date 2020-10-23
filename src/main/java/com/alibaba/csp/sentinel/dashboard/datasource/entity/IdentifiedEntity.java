package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import com.alibaba.csp.sentinel.dashboard.util.IdAcquireUtil;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@MappedSuperclass
public  class  IdentifiedEntity {
	
	 
	 @Id
	 @JsonSerialize(using = ToStringSerializer.class)
	 protected Long id;
	
	
	@PrePersist
	public void init(){
		if(id == null ){
			id = IdAcquireUtil.getID();
		}
	}
	public Long getId() {		
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}