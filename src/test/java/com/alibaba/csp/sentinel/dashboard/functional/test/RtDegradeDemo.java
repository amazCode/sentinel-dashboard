package com.alibaba.csp.sentinel.dashboard.functional.test;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 降级是在资源处于不稳定状态时使用的，这些资源将在下一个定义的时间窗口内降级。
 * 有三种方法衡量资源是否稳定:
 * 平均响应时间 (DEGRADE_GRADE_RT)：当资源的平均响应时间超过阈值（DegradeRule 中的 count，以 ms 为单位）之后，
 * 资源进入准降级状态。接下来如果持续进入 5 个请求，它们的 RT 都持续超过这个阈值，
 * 那么在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，
 * 对这个方法的调用都会自动地返回（抛出 DegradeException）
 * **异常比率，见 {@link ExceptionRatioDegradeDemo}.
 * **异常统计，见 {@link ExceptionCountDegradeDemo}.
 */
public class RtDegradeDemo {

    private static final String KEY = "平均响应时间";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static volatile boolean stop = false;
    private static final int threadCount = 100;
    private static int seconds = 10;

    public static void main(String[] args) throws Exception {
        tick();
        initDegradeRule();// 设置资源降级规则

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {
                int j = 0;

                @Override
                public void run() {
                    while (true) {
                        Entry entry = null;
                        try {
                            TimeUnit.MILLISECONDS.sleep(5);
                            entry = SphU.entry(KEY);
                            // 令牌已获得
                            pass.incrementAndGet();// 先+1,然后在返回值,相当于++i
                            // sleep 600 ms, as rt
                            TimeUnit.MILLISECONDS.sleep(600);
                        } catch (Exception e) {
                            // System.out.println("异常:"+e);
                            // 降级计数
                            block.incrementAndGet();// 先+1,然后在返回值,相当于++i
                        } finally {
                            // 记录总数
                            total.incrementAndGet();// 先+1,然后在返回值,相当于++i
                            if (entry != null) {
                                entry.exit();
                            }
                        }
                    }
                }

            });
            entryThread.setName("working-thread");
            entryThread.start();
        }
    }

    /**
     * 当资源的平均响应时间超过阈值（DegradeRule 中的 count，以 ms 为单位）之后，资源进入准降级状态。
     * 接下来如果持续进入 5 个请求，它们的 RT 都持续超过这个阈值，
     * 那么在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，
     * 对这个方法的调用都会自动地返回（抛出 DegradeException）。
     */
    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(KEY);// 设置资源名，资源名是限流规则的作用对象
        rule.setCount(10);// 设置平均响应时间阈值为10ms
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);// 设置降级模式，根据平均响应时间降级
        rule.setTimeWindow(10);// 设置降级的时间，以s为单位
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    static class TimerTask implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("统计开始......");
            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowTime = "";

            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }

                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                nowTime = formatter.format(new java.util.Date(TimeUtil.currentTimeMillis()));
                System.out.println(nowTime + ", total:" + oneSecondTotal + ", pass:" + oneSecondPass + ", block:" + oneSecondBlock);

                if (seconds-- <= 0) {
                    stop = true;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("耗时:" + cost + " ms");
            System.out.println("总计数:" + total.get() + ", 通过数:" + pass.get() + ", 降级数:" + block.get());
            System.out.println("统计结束！");
            System.exit(0);
        }
    }

}
