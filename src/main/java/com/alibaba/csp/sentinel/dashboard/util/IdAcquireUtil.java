package com.alibaba.csp.sentinel.dashboard.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class IdAcquireUtil {

	
	    private enum SnowFlakeSingleton{
	    	
	        Singleton;
	    	
	        private SnowFlakeUtils snowFlakeUtil;
	        
	        SnowFlakeSingleton () {
	        	
	        	snowFlakeUtil = new SnowFlakeUtils( getDataCenterId(),getWorkId());
	        }
	        
	        public SnowFlakeUtils getInstance () {
	            return snowFlakeUtil;
	        }
	    }

	    private static Long getWorkId () {
	        try {
	            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
	            int[] ints = StringUtils.toCodePoints(hostAddress);
	            int sums = 0;
	            for(int b : ints){
	                sums += b;
	            }
	            return (long)(sums % 32);
	        } catch (UnknownHostException e) {
	            return RandomUtils.nextLong(0,31);
	        }
	    }
	 
	    private static Long getDataCenterId(){
	    	  try {
	    		  int[] ints = StringUtils.toCodePoints(SystemUtils.getHostName());
	  	        int sums = 0;
	  	        for (int i: ints) {
	  	            sums += i;
	  	        }
	  	        return (long)(sums % 32);
	    	  } catch (Exception e) {
		            return RandomUtils.nextLong(0,31);
		        }
	      
	    }

	    

	    public static long getID () {
	    	
	        return SnowFlakeSingleton.Singleton.getInstance().nextId();
	    }
	
}
