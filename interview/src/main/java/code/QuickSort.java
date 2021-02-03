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

    public static void main(String[] args) {
        QuickSort quickSort = new QuickSort();
        int[] ints = quickSort.quickSort(new int[] {4, 1, 5, 3, 7, 6, 2}, 0, 6);
        for (int anInt : ints) {
            System.out.println(anInt);
        }
    }
}
