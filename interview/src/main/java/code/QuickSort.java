package code;

/**
 * Created by Jeff_xu on 06/03/2019.
 *
 * @author Jeff_xu
 */
public class QuickSort {

    public int[] quickSort(int[] arr, int left, int right) {
        if (left < right) {
            // 分组
            int i = left, j = right, x = arr[i];
            while (i < j) {
                while (i < j && arr[j] >= x) { j--; }
                if (i < j) {
                    arr[i++] = arr[j];
                }
                while (i < j && arr[i] < x) {
                    i++;
                }
                if (i < j) {
                    arr[j--] = arr[i];
                }
            }
            arr[i] = x;
            quickSort(arr, left, i - 1);
            quickSort(arr, i + 1, right);
        }
        return arr;
    }
}
