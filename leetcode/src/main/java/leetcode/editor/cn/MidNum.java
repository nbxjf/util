package leetcode.editor.cn;

/**
 * Created by Jeff_xu on 2021/2/8.
 * 给定两个数组，求这两个数组的中位数大小
 *
 * @author Jeff_xu
 */
public class MidNum {

    public static void main(String[] args) {
        double medianSortedArrays = findMedianSortedArrays(new int[0], new int[] {2, 3});
    }

    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int[] mergeResult = merge(nums1, nums2);
        int length = mergeResult.length;
        if (length % 2 == 0) {
            return (mergeResult[(length - 1) / 2] + mergeResult[length / 2]) / 2D;
        } else {
            return mergeResult[length / 2];
        }
    }

    public static int[] merge(int[] nums1, int[] nums2) {
        int i = 0;
        int j = 0;
        int[] result = new int[nums1.length + nums2.length];

        while (i < nums1.length && j < nums2.length) {
            if (nums1[i] <= nums2[j]) {
                result[i + j] = nums1[i];
                i++;
            } else {
                result[i + j] = nums2[j];
                j++;
            }
        }
        while (i < nums1.length) {
            result[i + j] = nums1[i];
            i++;
        }
        while (j < nums2.length) {
            result[i + j] = nums2[j];
            j++;
        }
        return result;
    }
}
