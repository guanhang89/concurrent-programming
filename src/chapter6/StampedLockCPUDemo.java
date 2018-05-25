package chapter6;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.StampedLock;

public class StampedLockCPUDemo {
    static Thread[] hodCpuThreads = new Thread[3];
    static final StampedLock lock = new StampedLock();

    public static void main(String[] args) throws InterruptedException {
        new Thread(){
            //占用写锁
            public void run() {
                long readLong = lock.writeLock();
                LockSupport.parkNanos(6000000000L);
                lock.unlockWrite(readLong);
            }
        }.start();
        Thread.sleep(100);
        for (int i = 0; i < 3; i++) {
            hodCpuThreads[i] = new Thread(new HoldCPUReadThread());
            //请求读锁会失败，被中断后会进入循环
            hodCpuThreads[i].start();
        }
        Thread.sleep(10000);

        for (int i = 0; i < 3; i++) {
            //中断线程，CPU占用飙升
            hodCpuThreads[i].interrupt();
        }
    }

    private static class HoldCPUReadThread implements Runnable {
        @Override
        public void run() {
            long lockr = lock.readLock();
            System.out.println(Thread.currentThread().getName() + " 获得锁");
            lock.unlockRead(lockr);
        }
    }
}
