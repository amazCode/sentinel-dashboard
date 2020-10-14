package com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.IdentifiedEntity;

@Entity
@Table(name = "t_service_detail")
public class ServiceDetailEntity  extends IdentifiedEntity{

	
//	@Id
//    private Long id;
	/**
	 * 服务名称
	 */
	private String serviceName;
	/**
	 * 服务描述
	 */
	private  String description;
	/**
	 * 所属厂家
	 */
	private String manufacturer;
	/**
	 * 负责人
	 */
	private String chargePerson;
	/**
	 * 维护人员
	 */
	private String maintainPerson;
	/**
	 * 创建日期
	 */
	private Date createDate;
	/**
	 * 服务状态 1表示正在使用  0表示逻辑删除
	 */
    private Byte serviceStatus = 1 ;	




	public String getServiceName() {
		return serviceName;
	}



	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}



	public String getManufacturer() {
		return manufacturer;
	}



	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}



	public String getChargePerson() {
		return chargePerson;
	}



	public void setChargePerson(String chargePerson) {
		this.chargePerson = chargePerson;
	}



	public String getMaintainPerson() {
		return maintainPerson;
	}



	public void setMaintainPerson(String maintainPerson) {
		this.maintainPerson = maintainPerson;
	}



	public Date getCreateDate() {
		return createDate;
	}



	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public Byte getServiceStatus() {
		return serviceStatus;
	}



	public void setServiceStatus(Byte serviceStatus) {
		this.serviceStatus = serviceStatus;
	}



	
	
	
	
	
	
}
