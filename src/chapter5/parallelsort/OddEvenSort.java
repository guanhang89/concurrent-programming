package chapter5.parallelsort;

import java.util.Arrays;

//串行的奇偶排序
public class OddEvenSort {

    public static void oddEvenSort(int[] arr) {
        int exchFlag = 1, start = 0;
        while (exchFlag == 1 || start == 1) {
            exchFlag = 0;
            for (int i = start; i < arr.length - 1; i += 2) {
                if (arr[i] > arr[i + 1]) {
                    int temp = arr[i];
                    arr[i] = arr[i + 1];
                    arr[i + 1] = temp;
                    exchFlag = 1;
                }
            }
            if (start == 0) {
                start = 1;
            } else {
                start = 0;
            }
        }
    }

    public static void main(String[] args) {
        int[] ints = {5, 52, 6, 3, 4};
        oddEvenSort(ints);
        System.out.println(Arrays.toString(ints));
    }
}
