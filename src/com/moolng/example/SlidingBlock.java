package com.moolng.example;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * 每天进步0.01（1.01 的 365次方），一年进步37.78
 * 每天退步0.01（0.99 的 365次方），一年退步0.03
 *
 * @Desc 滑动时间窗口算法
 * 使用场景：限流
 * 注明：仅供参考学习，如果有疑问可以发送邮件
 *
 * @Author blucelee2014@gmail.com
 *
 * @Date 2020-4-30 16:07:06
 *
 */
public class SlidingBlock {


    /** 阀值 **/
    private final static long THRESHOLD_VALUE = 300;
    private final static int WINDOW_SIZE = 6;
    private final static Map<Long, AtomicLong> QUEUE_MAP = new ConcurrentHashMap<Long, AtomicLong>();
    public static void main(String[] args) throws InterruptedException {
        // 模拟并发线程池
        ExecutorService service = Executors.newFixedThreadPool(10);
        // 模拟高并发
        while (true){
            service.execute(new MyThread());
        }

    }


    /**
     * 业务逻辑
     */
    static class MyThread implements Runnable{

        @Override
        public void run() {
            // 请求时间
            long time = System.currentTimeMillis() / 1000;
            // 获取这个时间片段的次数
            AtomicLong atomicLong = QUEUE_MAP.get(time);
            // 如果为空，创建一个时间片段
            if(atomicLong == null){
                atomicLong = new AtomicLong(0);
                QUEUE_MAP.put(time, atomicLong);
            }
            // 统计当前时间窗口里面总和
            long nums = 0;
            for(int i=0;i<WINDOW_SIZE;i++){
                if(QUEUE_MAP.get(time - i) == null){
                    nums += 0;
                }else {
                    nums += QUEUE_MAP.get(time - i).get();
                }
            }
            // 判断当前时间的次数是否小于阀值
            if(QUEUE_MAP.get(time).get() < THRESHOLD_VALUE){
                QUEUE_MAP.get(time).incrementAndGet();
            }

            System.out.println("[" + Thread.currentThread().getName() + "] time = " + time + ", nums = " + nums);
            // 如果时间窗口的综合大于阀值，进行限流
            if(nums >= THRESHOLD_VALUE){
                System.out.println("限流了排队等待, nums = " + nums);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}