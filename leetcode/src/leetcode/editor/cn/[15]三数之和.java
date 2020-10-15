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
            int n = nums.length;
            Arrays.sort(nums);
            List<List<Integer>> ans = new ArrayList<List<Integer>>();
            // 枚举 a
            for (int first = 0; first < n; ++first) {
                // 需要和上一次枚举的数不相同
                if (first > 0 && nums[first] == nums[first - 1]) {
                    continue;
                }
                // c 对应的指针初始指向数组的最右端
                int third = n - 1;
                int target = -nums[first];
                // 枚举 b
                for (int second = first + 1; second < n; ++second) {
                    // 需要和上一次枚举的数不相同
                    if (second > first + 1 && nums[second] == nums[second - 1]) {
                        continue;
                    }
                    // 需要保证 b 的指针在 c 的指针的左侧
                    while (second < third && nums[second] + nums[third] > target) {
                        --third;
                    }
                    // 如果指针重合，随着 b 后续的增加
                    // 就不会有满足 a+b+c=0 并且 b<c 的 c 了，可以退出循环
                    if (second == third) {
                        break;
                    }
                    if (nums[second] + nums[third] == target) {
                        List<Integer> list = new ArrayList<Integer>();
                        list.add(nums[first]);
                        list.add(nums[second]);
                        list.add(nums[third]);
                        ans.add(list);
                    }
                }
            }
            return ans;
        }

        /**
         * 超时解法
         *
         * @param nums
         * @return
         */
        public List<List<Integer>> threeSum_1(int[] nums) {
            if (nums == null || nums.length < 3) {
                return new ArrayList<>();
            }
            Arrays.sort(nums);
            if (!(nums[0] <= 0 && nums[nums.length - 1] >= 0)) {
                return new ArrayList<>();
            }

            List<List<Integer>> result = new ArrayList<>();
            for (int i = 0; nums[i] <= 0 && i < nums.length - 2; i++) {
                if (i > 0 && nums[i] == nums[i - 1]) {
                    continue;
                }
                for (int j = nums.length - 1; j > i && nums[j] >= 0; j--) {
                    if (j < nums.length - 1 && nums[j] == nums[j + 1]) {
                        continue;
                    }
                    int min = nums[i];
                    int max = nums[j];
                    int sum = min + max;
                    if (sum >= 0) {
                        //和大于0，从前迭代
                        for (int k = i + 1; nums[k] <= -sum && k < j; k++) {
                            if (nums[k] + sum == 0) {
                                ArrayList<Integer> item = new ArrayList<>(Arrays.asList(nums[i], nums[k], nums[j]));
                                if (!result.contains(item)) {
                                    result.add(item);
                                }
                            }
                        }
                    } else {
                        // 和小于0，从后迭代
                        for (int k = j - 1; nums[k] >= -sum; k--) {
                            if (nums[k] + sum == 0) {
                                ArrayList<Integer> item = new ArrayList<>(Arrays.asList(nums[i], nums[k], nums[j]));
                                if (!result.contains(item)) {
                                    result.add(item);
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
