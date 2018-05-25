package chapter6;

import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class LongAddrDemo {

    private static final int MAX_THREADS = 3;
    private static final int TASK_COUNT = 3;
    private static final int TARGET_COUNT = 100000000;

    private AtomicLong acount = new AtomicLong(0L);
    private LongAdder lacount = new LongAdder();
    private long count = 0;

    static CountDownLatch cdlsyn = new CountDownLatch(TASK_COUNT);
    static CountDownLatch cdlatomic = new CountDownLatch(TASK_COUNT);
    static CountDownLatch cdladdr = new CountDownLatch(TASK_COUNT);

    protected synchronized long inc() {
        return ++count;
    }

    protected synchronized long getCount() {
        return count;
    }

    public class SynThread implements Runnable {
        protected String name;
        protected long starttime;
        LongAddrDemo out;

        public SynThread(long starttime, LongAddrDemo out) {
            this.starttime = starttime;
            this.out = out;
        }

        @Override
        public void run() {
            long v = out.getCount();
            while (v < TARGET_COUNT) {
                v = out.inc();
            }
            long endtime = System.currentTimeMillis();
            System.out.println("SynThread spend:" + (endtime - starttime) + "ms");
            cdlsyn.countDown();

        }
    }
    //采用传统同步方式
    public void testSync() throws InterruptedException{
        ExecutorService exe = Executors.newFixedThreadPool(MAX_THREADS);
        long starttime = System.currentTimeMillis();
        SynThread synThread = new SynThread(starttime, this);
        for (int i = 0; i < TARGET_COUNT; i++) {
            exe.submit(synThread);
        }
        cdlsyn.await();
        exe.shutdown();
    }


    public class AtomicThread implements Runnable {

        protected String name;
        protected long starttime;

        public AtomicThread(long starttime) {
            this.starttime = starttime;
        }

        @Override
        public void run() {
            long v = acount.get();
            while (v < TARGET_COUNT) {
                v = acount.incrementAndGet();
            }
            long endtime = System.currentTimeMillis();
            System.out.println("AtomicThread spend: " + (endtime - starttime));
            cdlatomic.countDown();
        }
        //采用原子类
        public void testAtomci() throws InterruptedException{
            ExecutorService exe = Executors.newFixedThreadPool(MAX_THREADS);
            long starttime = System.currentTimeMillis();
            AtomicThread atomic = new AtomicThread(starttime);
            for (int i = 0; i < TASK_COUNT; i++) {
                exe.submit(atomic);
            }
            cdlatomic.await();
            exe.shutdown();
        }

        public class LongAddrThread implements Runnable {
            protected String name;
            protected long starttime;

            public LongAddrThread(long starttime) {
                this.starttime = starttime;
            }

            @Override
            public void run() {
                long v = lacount.sum();
                while (v < TARGET_COUNT) {
                    lacount.increment();
                    v = lacount.sum();
                }
                long endtime = System.currentTimeMillis();
                System.out.println("LongAddr spend: " + (endtime - starttime) + "ms");
                cdladdr.countDown();

            }
        }
        //采用LongAdder
        public void testAtomiclong() throws InterruptedException {
            ExecutorService exe = Executors.newFixedThreadPool(MAX_THREADS);
            long starttime = System.currentTimeMillis();
            LongAddrThread atomic = new LongAddrThread(starttime);
            for (int i = 0; i < TASK_COUNT; i++) {
                exe.submit(atomic);
            }
            cdladdr.await();
            exe.shutdown();
        }
    }

}
