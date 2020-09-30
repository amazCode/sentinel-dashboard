package com.alibaba.csp.sentinel.dashboard.domain.vo;



/**
 * 请求记录
 * @author xqw
 *
 */
public class RequestRecordVo {

	/**
	 * 排名
	 */
	private Integer ranking;
	/**
	 * 请求地址
	 */
	private String resource;
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

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
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

	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
	
	
	
}
