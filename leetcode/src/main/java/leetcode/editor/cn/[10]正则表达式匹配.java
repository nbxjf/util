package leetcode.editor.cn;

//给你一个字符串 s 和一个字符规律 p，请你来实现一个支持 '.' 和 '*' 的正则表达式匹配。 
//
// 
// '.' 匹配任意单个字符 
// '*' 匹配零个或多个前面的那一个元素 
// 
//
// 所谓匹配，是要涵盖 整个 字符串 s的，而不是部分字符串。 
// 
//
// 示例 1： 
//
// 
//输入：s = "aa", p = "a"
//输出：false
//解释："a" 无法匹配 "aa" 整个字符串。
// 
//
// 示例 2: 
//
// 
//输入：s = "aa", p = "a*"
//输出：true
//解释：因为 '*' 代表可以匹配零个或多个前面的那一个元素, 在这里前面的元素就是 'a'。因此，字符串 "aa" 可被视为 'a' 重复了一次。
// 
//
// 示例 3： 
//
// 
//输入：s = "ab", p = ".*"
//输出：true
//解释：".*" 表示可匹配零个或多个（'*'）任意字符（'.'）。
// 
//
// 
//
// 提示： 
//
// 
// 1 <= s.length <= 20 
// 1 <= p.length <= 30 
// s 只包含从 a-z 的小写字母。 
// p 只包含从 a-z 的小写字母，以及字符 . 和 *。 
// 保证每次出现字符 * 时，前面都匹配到有效的字符 
// 
// Related Topics 递归 字符串 动态规划 👍 2715 👎 0


class RegularExpressionMatching {
    public static void main(String[] args) {
        Solution solution = new RegularExpressionMatching().new Solution();
//        System.out.println(solution.isMatch("mississippi", "mis*is*p*."));
//        System.out.println("mississippi".matches("mis*is*p*."));
        System.out.println(solution.isMatch("aa", "a*"));
//        System.out.println(solution.isMatch("aa", "a."));
//        System.out.println(solution.isMatch("ab", ".*"));
//        System.out.println(solution.isMatch("ab", "a**b"));
//        System.out.println(solution.isMatch("aab", "c*a*b"));
//        System.out.println(solution.isMatch("ab", "**"));
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public boolean isMatch(String s, String p) {
            char[] cs = s.toCharArray();
            char[] cp = p.toCharArray();

            // dp[i][j]:表示s的前i个字符，p的前j个字符是否能够匹配
            boolean[][] dp = new boolean[cs.length + 1][cp.length + 1];

            // 初期值
            // s为空，p为空，能匹配上
            dp[0][0] = true;
            // p为空，s不为空，必为false(boolean数组默认值为false，无需处理)

            // s为空，p不为空，由于*可以匹配0个字符，所以有可能为true
            for (int j = 1; j <= cp.length; j++) {
                if (cp[j - 1] == '*') {
                    dp[0][j] = dp[0][j - 2]; // 等于将最后两个字符干掉，看去除掉x*的结果
                }
            }

            // 填格子
            for (int i = 1; i <= cs.length; i++) {
                for (int j = 1; j <= cp.length; j++) {
                    // 文本串和模式串末位字符能匹配上
                    if (cs[i - 1] == cp[j - 1] || cp[j - 1] == '.') {
                        dp[i][j] = dp[i - 1][j - 1];
                    } else if (cp[j - 1] == '*') { // 模式串末位是*
                        // 模式串*的前一个字符能够跟文本串的末位匹配上
                        if (cs[i - 1] == cp[j - 2] || cp[j - 2] == '.') {
                            dp[i][j] = dp[i][j - 2]      // *匹配0次的情况
                                    || dp[i - 1][j];     // *匹配1次或多次的情况
                        } else { // 模式串*的前一个字符不能够跟文本串的末位匹配
                            dp[i][j] = dp[i][j - 2];     // *只能匹配0次
                        }
                    }
                }
            }
            return dp[cs.length][cp.length];
        }
    }
//leetcode submit region end(Prohibit modification and deletion)


}