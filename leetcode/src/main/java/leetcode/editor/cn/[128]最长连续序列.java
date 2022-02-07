package leetcode.editor.cn;

//给定一个未排序的整数数组，找出最长连续序列的长度。 
//
// 要求算法的时间复杂度为 O(n)。 
//
// 示例: 
//
// 输入: [100, 4, 200, 1, 3, 2]
//输出: 4
//解释: 最长连续序列是 [1, 2, 3, 4]。它的长度为 4。 
// Related Topics 并查集 数组 
// 👍 556 👎 0

import java.util.ArrayList;
import java.util.List;

class LongestConsecutiveSequence {
    public static void main(String[] args) {
        Solution solution = new LongestConsecutiveSequence().new Solution();
        System.out.println(solution.longestConsecutive(new int[] {2147483646,-2147483647,0,2,2147483644,-2147483645,2147483645}));
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {

        // 未排序，算法时间复杂度为 O(n)
        public int longestConsecutive(int[] nums) {
            if (nums == null || nums.length == 0) {
                return 0;
            }

            int max = 0;
            int min = 0;
            for (int num : nums) {
                max = Math.max(max, num);
                min = Math.min(min, num);
            }

            List<Integer> test = new ArrayList<>();

            for (int num : nums) {
                test.add(num - min + 1,1);
            }

            int result = 1;
            int temp = 0;
            for (long num : test) {
                if (num == 0) {
                    temp = 0;
                } else {
                    temp++;
                    result = Math.max(result, temp);
                }
            }
            return result;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
