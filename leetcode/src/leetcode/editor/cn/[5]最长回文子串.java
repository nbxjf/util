package leetcode.editor.cn;

//给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为 1000。 
//
// 示例 1： 
//
// 输入: "babad"
//输出: "bab"
//注意: "aba" 也是一个有效答案。
// 
//
// 示例 2： 
//
// 输入: "cbbd"
//输出: "bb"
// 
// Related Topics 字符串 动态规划 
// 👍 2879 👎 0

class LongestPalindromicSubstring {
    public static void main(String[] args) {
        Solution solution = new LongestPalindromicSubstring().new Solution();
        String ans = solution.longestPalindrome("cbbd");
        System.out.println(ans);
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {

        public String longestPalindrome(String s) {
            int n = s.length();
            boolean[][] dp = new boolean[n][n];
            String ans = "";
            for (int gap = 0; gap < n; ++gap) {
                for (int left = 0; left + gap < n; ++left) {
                    int right = left + gap;
                    if (gap == 0) {
                        dp[left][right] = true;
                    } else if (gap == 1) {
                        dp[left][right] = (s.charAt(left) == s.charAt(right));
                    } else {
                        dp[left][right] = (s.charAt(left) == s.charAt(right) && dp[left + 1][right - 1]);
                    }
                    if (dp[left][right] && gap + 1 > ans.length()) {
                        ans = s.substring(left, left + gap + 1);
                    }
                }
            }
            return ans;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
