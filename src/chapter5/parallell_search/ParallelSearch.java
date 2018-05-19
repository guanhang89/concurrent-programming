package chapter5.parallell_search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSearch {
    static int[] arr;
    static ExecutorService pool = Executors.newCachedThreadPool();
    static final int Thread_Num = 2;
    static AtomicInteger result = new AtomicInteger(-1);

    public static int search(int searchValue, int beginPos, int endpos) {
        int i = 0;
        for (int j = beginPos; j < endpos; j++) {
            if (result.get() >= 0) {
                return result.get();
            }
            if (arr[i] == searchValue) {
                if (!result.compareAndSet(-1, i)) {
                    return result.get();
                }
            }
        }
        return -1;
    }

    static class SearchTask implements Callable<Integer> {
        int begin,end,searchValue;

        public SearchTask(int begin, int end, int searchValue) {
            this.begin = begin;
            this.end = end;
            this.searchValue = searchValue;
        }

        @Override
        public Integer call() throws Exception {
            int re = search(searchValue, begin, end);
            return re;
        }
    }

    public static int pSearche(int searcheValue) throws ExecutionException, InterruptedException {
        int subArrSize = arr.length / Thread_Num + 1;
        List<Future<Integer>> re = new ArrayList<>();
        for (int i = 0; i < arr.length; i += subArrSize) {
            int end = i + subArrSize;
            if (end >= arr.length) {
                end = arr.length;
            }
            re.add(pool.submit(new SearchTask(searcheValue, i, end)));
        }
        for (Future<Integer> fu : re) {
            if (fu.get() >= 0) {
                return fu.get();
            }
        }
        return -1;
    }
}
