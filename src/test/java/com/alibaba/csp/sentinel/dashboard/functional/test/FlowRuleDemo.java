package com.alibaba.csp.sentinel.dashboard.functional.test;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

public class FlowRuleDemo {

	public static void main(String[] args) {
		
	//	testFlowRule();//一秒内，只需要通过两次请求，其他的均失败
//		testFlowRuleForThreadNum();//基于并发线程数控制
		testFlowRuleForCaller();//定义根据调用者的流控规则
	}
	
	
	/*初始化规则
	 * 
	 * limitApp : 流控针对的调用来源，若为default 则不区分来源
	  strategy : 调用关系限流策略
	  controlBehavior ：流量控制效果(直接拒绝,Warm Up,均速排队)
	  当使用QPS为阈值类型时，并设置阈值为2，定义资源，其他默认，则表示一秒内，只需要通过两次请求，其他的均失败。
	  
	 * */
	public static void initRule(){
	    List<FlowRule> rules = new ArrayList<>();
	  	//定义规则
	    FlowRule rule = new FlowRule();
	  	//定义资源
	    rule.setResource("echo");//资源名，即限流规则的作用对象
	  	//定义模式
	    rule.setGrade(RuleConstant.FLOW_GRADE_QPS);// 限流阈值类型(QPS或是并发线程数)
	  	//定义阈值
	    rule.setCount(2);
	    rules.add(rule);
	    FlowRuleManager.loadRules(rules);
	}

	public static void testFlowRule(){
	    initRule();
	    Entry entry = null;
	    for (int i = 0; i < 10; i++) {
	      try {
	        entry = SphU.entry("echo");
	        System.out.println("访问成功");
	      } catch (BlockException e) {
	        System.out.println("当前访问人数过多，请刷新后重新!");
	      }finally {
	        if (entry != null){
	          entry.exit();
	        }
	      }
	    }
	}

	public static void initFlowRuleForThreadNum() {
	    List<FlowRule> rules = new ArrayList<>();
	    FlowRule rule = new FlowRule();
	    //定义以线程数控制
	    rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
	    //定义资源名
	    rule.setResource("echo");
	    //定义并发线程数阈值
	    rule.setCount(2);
	    rules.add(rule);
	    FlowRuleManager.loadRules(rules);
	}

	public static void testFlowRuleForThreadNum() {
	    initFlowRuleForThreadNum();
	    for (int i = 0; i < 5; i++) {
	        new Thread() {
	            @Override
	            public void run() {
	                for (int j = 0; j < 5; j++) {
	                  Entry entry = null;
	                  try {
	                      entry = SphU.entry("echo");
	                      System.out.println("操作成功！");
	                  } catch (BlockException ex) {
	                    	System.out.println("当前访问人数过多，请刷新后重试!");
	                  } finally {
	                      if (entry != null) {
	                          entry.exit();
	                      }
	                  }
	                }
	            }
	        }.start();
	    }
	}

	/*定义根据调用者的流控规则*/
	public static void initFlowRuleForCaller(){
	    List<FlowRule> rules = new ArrayList<>();
	    FlowRule rule = new FlowRule();
	    //定义资源名
	    rule.setResource("echo");
	    //定义阈值类型
	    rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
	    //定义阈值
	    rule.setCount(2);
	    //定义限制调用者
	    rule.setLimitApp("caller");
	    rules.add(rule);

	    FlowRule rule1 = new FlowRule();
	    rule1.setResource("echo");
	    rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
	    rule1.setLimitApp("other");
	    rule1.setCount(3);
	    rules.add(rule1);
	    FlowRuleManager.loadRules(rules);
	}

   //定义根据调用者的流控规则
	public static void testFlowRuleForCaller(){
	  initFlowRuleForCaller();
	  for (int i = 0; i < 5; i++) {
	    ContextUtil.enter("c1","caller1");
	    Entry entry = null;
	    try {
	      entry = SphU.entry("echo");
	      System.out.println("访问成功");
	    } catch (BlockException e) {
	      System.out.println("网络异常，请刷新！");
	    }finally {
	      if (entry != null){
	        entry.exit();
	      }
	    }
	  }
	}
	

	
	
	
}
