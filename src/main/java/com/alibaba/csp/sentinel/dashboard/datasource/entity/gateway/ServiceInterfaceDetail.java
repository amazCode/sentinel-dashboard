package com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.IdentifiedEntity;

/**
 * 服务接口详细 
 * @author xqw
 *
 */
@Entity
@Table(name = "t_service_interface_detail")
public class ServiceInterfaceDetail   extends IdentifiedEntity{

	
//	@Id
//    private Long id;
	/**
	 * 接口名  如：故障档案接口
	 */
	private String urlName;
	/**
	 * 接口url地址
	 */
	@Lob
	private String urlAddress;
	/**
	 * 接口功能描述 如：用来获取历史的故障档案
	 */
	private  String description;
    /**
     * 接口所属的服务id
     */
	private String serviceId;
	/**
	 * 排名
	 */
	private Integer ranking;
	/**
	 * 总记录数
	 */
	private Integer countNum;
	/**
	 * 成功qps数
	 */
	private Integer success_qps;  
	/**
	 * 通过qps数
	 */
	private Integer pass_qps;
	/**
	 * 异常qps数
	 */
	private Integer exception_qps;
	/**
	 * 总耗时
	 */
	private Double spendTime;
	
	/**
	 * 数据统计/生成时间
	 */
	private Date createDate;
	/**
	 * 服务状态 1表示正在使用  0表示逻辑删除
	 */
    private Byte serviceStatus = 1 ;	
//	public Long getId() {
//		return id;
//	}
//	public void setId(Long id) {
//		this.id = id;
//	}
	public String getUrlAddress() {
		return urlAddress;
	}
	public void setUrlAddress(String urlAddress) {
		this.urlAddress = urlAddress;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrlName() {
		return urlName;
	}
	public void setUrlName(String urlName) {
		this.urlName = urlName;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public Integer getRanking() {
		return ranking;
	}
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}
	public Integer getCountNum() {
		return countNum;
	}
	public void setCountNum(Integer countNum) {
		this.countNum = countNum;
	}
	public Integer getSuccess_qps() {
		return success_qps;
	}
	public void setSuccess_qps(Integer success_qps) {
		this.success_qps = success_qps;
	}
	public Integer getPass_qps() {
		return pass_qps;
	}
	public void setPass_qps(Integer pass_qps) {
		this.pass_qps = pass_qps;
	}
	public Integer getException_qps() {
		return exception_qps;
	}
	public void setException_qps(Integer exception_qps) {
		this.exception_qps = exception_qps;
	}
	public Double getSpendTime() {
		return spendTime;
	}
	public void setSpendTime(Double spendTime) {
		this.spendTime = spendTime;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Byte getServiceStatus() {
		return serviceStatus;
	}
	public void setServiceStatus(Byte serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	
	
}
