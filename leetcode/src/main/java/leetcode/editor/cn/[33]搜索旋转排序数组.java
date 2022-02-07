package leetcode.editor.cn;

//假设按照升序排序的数组在预先未知的某个点上进行了旋转。 
//
// ( 例如，数组 [0,1,2,4,5,6,7] 可能变为 [4,5,6,7,0,1,2] )。 
//
// 搜索一个给定的目标值，如果数组中存在这个目标值，则返回它的索引，否则返回 -1 。 
//
// 你可以假设数组中不存在重复的元素。 
//
// 你的算法时间复杂度必须是 O(log n) 级别。 
//
// 示例 1: 
//
// 输入: nums = [4,5,6,7,0,1,2], target = 0
//输出: 4
// 
//
// 示例 2: 
//
// 输入: nums = [4,5,6,7,0,1,2], target = 3
//输出: -1 
// Related Topics 数组 二分查找 
// 👍 963 👎 0

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class SearchInRotatedSortedArray {
    public static void main(String[] args) {
        Solution solution = new SearchInRotatedSortedArray().new Solution();
        System.out.println(solution.search(new int[] {4, 5, 6, 7, 0, 1, 2}, 3));
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {

        public int search(int[] nums, int target) {
            List<Integer> numsList = Arrays.stream(nums).boxed().collect(Collectors.toList());
            Collections.sort(numsList);
            return Collections.binarySearch(numsList, target);
        }

        /**
         * 能通过，但是时间复杂度不符合要求
         */
        public int search_2(int[] nums, int target) {

            int index = -1;
            for (int i = 0; i < nums.length; i++) {
                if (nums[i] == target) {
                    index = i;
                }
            }
            return index;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
