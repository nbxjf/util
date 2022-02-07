package leetcode.editor.cn;

//给定一个包含 n 个整数的数组 nums 和一个目标值 target，判断 nums 中是否存在四个元素 a，b，c 和 d ，使得 a + b + c +
// d 的值与 target 相等？找出所有满足条件且不重复的四元组。 
//
// 注意：答案中不可以包含重复的四元组。 
//
// 
//
// 示例 1： 
//
// 
//输入：nums = [1,0,-1,0,-2,2], target = 0
//输出：[[-2,-1,1,2],[-2,0,0,2],[-1,0,0,1]]
// 
//
// 示例 2： 
//
// 
//输入：nums = [], target = 0
//输出：[]
// 
//
// 
//
// 提示： 
//
// 
// 0 <= nums.length <= 200 
// -109 <= nums[i] <= 109 
// -109 <= target <= 109 
// 
// Related Topics 数组 哈希表 双指针 
// 👍 772 👎 0

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FourSum {
    public static void main(String[] args) {
        Solution solution = new FourSum().new Solution();
        List<List<Integer>> lists = solution.fourSum(new int[] {1, 0, -1, 0, -2, 2}, 0);
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public List<List<Integer>> fourSum(int[] nums, int target) {
            Arrays.sort(nums);

            List<List<Integer>> result = new ArrayList<>();
            for (int i = 0; i < nums.length; i++) {
                if (i > 0 && nums[i] == nums[i - 1]) {
                    continue;
                }
                List<List<Integer>> subResult = threeSum(Arrays.copyOfRange(nums, i + 1, nums.length), target - nums[i]);
                if (subResult.size() == 0) {
                    continue;
                }
                for (List<Integer> integers : subResult) {
                    List<Integer> allNums = new ArrayList<>();
                    allNums.add(nums[i]);
                    allNums.addAll(integers);
                    result.add(allNums);
                }
            }
            return result;
        }

        public List<List<Integer>> threeSum(int[] nums, int target) {
            List<List<Integer>> result = new ArrayList<>();

            for (int first = 0; first < nums.length; first++) {
                if (first > 0 && nums[first] == nums[first - 1]) {
                    continue;
                }
                int need = target - nums[first];
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
                    if (nums[first] + nums[third] + nums[second] == target) {
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
