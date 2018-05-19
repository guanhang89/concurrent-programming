package chapter5.parallelsort;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class POddEvenSort {

    static int exchFlag = 1;


    static synchronized void setExchFlag(int v) {
        exchFlag = v;
    }

    static synchronized int getExchFlag() {
        return exchFlag;
    }

    public static class OddEvenSortTask implements Runnable {

        int i;
        CountDownLatch latch;
        int arr[];

        public OddEvenSortTask(int i, CountDownLatch latch, int[] arr) {
            this.i = i;
            this.latch = latch;
            this.arr = arr;
        }

        @Override
        public void run() {
            if (arr[i] > arr[i + 1]) {
                int temp = arr[i];
                arr[i] = arr[i + 1];
                arr[i + 1] = temp;
                setExchFlag(1);
            }
            latch.countDown();
        }

    }

    public static void pOddEvenSort(int[] arr) throws InterruptedException {
        int start = 0;
        ExecutorService pool = Executors.newCachedThreadPool();
        while (getExchFlag() == 1 || start == 1) {
            setExchFlag(0);
            CountDownLatch latch = new CountDownLatch(arr.length / 2 - (arr.length % 2 == 0 ? start : 0));
            for (int i = start; i < arr.length - 1; i += 2) {
                pool.submit(new OddEvenSortTask(i, latch,arr));
            }
            latch.wait();
            if (start == 0) {
                start = 1;
            } else {
                start = 0;
            }
        }
    }
}
