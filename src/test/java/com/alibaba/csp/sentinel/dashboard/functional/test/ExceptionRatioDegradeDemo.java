package com.alibaba.csp.sentinel.dashboard.functional.test;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
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
 * 异常比率:当异常计数/秒与成功的比率qps大于或等于阈值时，对资源的访问将被阻塞在即将到来的时间窗口。
 * **异常统计，见 {@link ExceptionCountDegradeDemo}.
 * **平均响应时间, 见 {@link RtDegradeDemo}.
 */
public class ExceptionRatioDegradeDemo {
    private static final String KEY = "异常比率";

    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger bizException = new AtomicInteger();

    private static volatile boolean stop = false;
    private static final int threadCount = 1;
    private static int seconds = 60 + 40;

    public static void main(String[] args) throws Exception {
        tick();
        initDegradeRule();// 设置资源降级规则

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    while (true) {
                        count++;
                        Entry entry = null;
                        try {
                            Thread.sleep(20);
                            entry = SphU.entry(KEY);
                            // 获得令牌表示通过
                            pass.addAndGet(1);// 先+n,然后在返回值
                            if (count % 2 == 0) {
                                // 业务代码引发异常
                                throw new RuntimeException("throw runtime exception");
                            }
                        } catch (BlockException e) {
                            // 降级计数
                            block.addAndGet(1);// 先+n,然后在返回值
                        } catch (Throwable t) {
                            // 记录业务异常
                            bizException.incrementAndGet();// 先+1,然后在返回值,相当于++i
                            // 为了统计异常比例或异常数，需要通过 Tracer.trace(ex) 记录业务异常。
                            Tracer.trace(t);
                        } finally {
                            // 记录总数
                            total.addAndGet(1);// 先+n,然后在返回值
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
     * 当资源的每秒异常总数占通过量的比值超过阈值（DegradeRule 中的 count）之后，
     * 资源进入降级状态，即在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，对这个方法的调用都会自动地返回。
     * 异常比率的阈值范围是 [0.0, 1.0]，代表 0% - 100%。
     */
    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(KEY);// 设置资源名，资源名是限流规则的作用对象
        // 将限制异常比率设置为0.1
        rule.setCount(0.5);// 异常比率的阈值范围是 [0.0, 1.0]，代表 0% - 100%。
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);// 设置降级模式，根据异常比率降级
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
            long oldBizException = 0;

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

                long globalBizException = bizException.get();
                long oneSecondBizException = globalBizException - oldBizException;
                oldBizException = globalBizException;

                nowTime = formatter.format(new java.util.Date(TimeUtil.currentTimeMillis()));
                System.out.println(nowTime + ", oneSecondTotal:" + oneSecondTotal
                        + ", oneSecondPass:" + oneSecondPass
                        + ", oneSecondBlock:" + oneSecondBlock
                        + ", oneSecondBizException:" + oneSecondBizException);
                if (seconds-- <= 0) {
                    stop = true;
                }
            }
            long cost = System.currentTimeMillis() - start;
            System.out.println("耗时:" + cost + " ms");
            System.out.println("总计数:" + total.get() + ", 通过数:" + pass.get()
                    + ", 降级数:" + block.get() + ", 业务异常数:" + bizException.get());
            System.out.println("统计结束！");
            System.exit(0);
        }
    }
}
