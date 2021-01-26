package leetcode.editor.cn;

//给出集合 [1,2,3,...,n]，其所有元素共有 n! 种排列。 
//
// 按大小顺序列出所有排列情况，并一一标记，当 n = 3 时, 所有排列如下： 
//
// 
// "123" 
// "132" 
// "213" 
// "231" 
// "312" 
// "321" 
// 
//
// 给定 n 和 k，返回第 k 个排列。 
//
// 
//
// 示例 1： 
//
// 
//输入：n = 3, k = 3
//输出："213"
// 
//
// 示例 2： 
//
// 
//输入：n = 4, k = 9
//输出："2314"
// 
//
// 示例 3： 
//
// 
//输入：n = 3, k = 1
//输出："123"
// 
//
// 
//
// 提示： 
//
// 
// 1 <= n <= 9 
// 1 <= k <= n! 
// 
// Related Topics 数学 回溯算法 
// 👍 458 👎 0

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class PermutationSequence {
    public static void main(String[] args) {
        Solution solution = new PermutationSequence().new Solution();
        String permutation = solution.getPermutation(4, 9);
        System.out.println(permutation);
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {

        // 解2：使用计算(n-1)! 来判断当前的值是第几个
        // 如输入 4，9
        // 3! = 6  => (9 -1) / 6 = 1  => 第一位是2
        // 2! = 2  => 2 / 2 = 1  => 第二位是3
        // 1! = 1 => 0 / 1 = 0 => 第三位是1

        public String getPermutation(int n, int k) {

            int[] subPerm = new int[9];

            List<Integer> choise = new ArrayList<>();

            int b = 1;
            subPerm[0] = 1;
            for (int i = 1; i <= n; i++) {
                b = b * i;
                subPerm[i] = b;
                choise.add(i);
            }

            k --;
            StringBuilder result = new StringBuilder();
            while (!choise.isEmpty()) {

                for (int i = n; i >= 1; i--) {
                  int index = k / subPerm[i - 1];
                  Integer a = choise.remove(index);
                  result.append(a);
                  k = k - index * subPerm[i -1];
                }
            }
            return result.toString();
        }

        // 解1：列举出所有解的全排列，但是会有超时问题
        public String getPermutation1(int n, int k) {
            List<String> result = getArrange(n);
            result = result.stream().sorted().collect(Collectors.toList());
            return result.get(k - 1);

        }

        private List<String> getArrange(int n) {
            Set<String> result = new HashSet<>();
            if (n == 1) {
                result.add(String.valueOf(n));
            } else {
                List<String> subResult = getArrange(n - 1);

                for (String str : subResult) {
                    for (int i = str.length() - 1; i >= 0; i--) {
                        result.add(str.substring(0, i + 1) + n + str.substring(i + 1));
                    }
                    result.add(n + str);
                }
            }
            return new ArrayList<>(result);
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
