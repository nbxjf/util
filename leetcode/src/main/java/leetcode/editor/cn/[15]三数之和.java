package leetcode.editor.cn;

//给你一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？请你找出所有满足条件且不重复
//的三元组。 
//
// 注意：答案中不可以包含重复的三元组。 
//
// 
//
// 示例： 
//
// 给定数组 nums = [-1, 0, 1, 2, -1, -4]，
//
//满足要求的三元组集合为：

//[
//  [-1, 0, 1],
//  [-1, -1, 2]
//]
// 
// Related Topics 数组 双指针 
// 👍 2572 👎 0

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ThreeSum {
    public static void main(String[] args) {
        Solution solution = new ThreeSum().new Solution();
        List<List<Integer>> lists = solution.threeSum(new int[] {-1, 0, 1, 2, -1, -4});
        System.out.println(lists);
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {

        public List<List<Integer>> threeSum(int[] nums) {

            Arrays.sort(nums);

            List<List<Integer>> result = new ArrayList<>();

            for (int first = 0; first < nums.length; first++) {
                if (first > 0 && nums[first] == nums[first - 1]) {
                    continue;
                }
                int need = -nums[first];
                int third = nums.length - 1;
                for (int second = first + 1; second < nums.length; second++) {
                    if (second > first + 1 && nums[second] == nums[second - 1]) {
                        continue;
                    }

                    while (second < third && nums[third] + nums[second] > need) {
                        third--;
                    }
                    if (third == second) {
                        break;
                    }
                    if (nums[first] + nums[third] + nums[second] == 0) {
                        List<Integer> list = new ArrayList<>();
                        list.add(nums[first]);
                        list.add(nums[second]);
                        list.add(nums[third]);
                        result.add(list);
                    }
                }
            }
            return result;
        }

    }
    //leetcode submit region end(Prohibit modification and deletion)

}
