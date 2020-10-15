package leetcode.editor.cn;

//编写一个函数来查找字符串数组中的最长公共前缀。 
//
// 如果不存在公共前缀，返回空字符串 ""。 
//
// 示例 1: 
//
// 输入: ["flower","flow","flight"]
//输出: "fl"
// 
//
// 示例 2: 
//
// 输入: ["dog","racecar","car"]
//输出: ""
//解释: 输入不存在公共前缀。
// 
//
// 说明: 
//
// 所有输入只包含小写字母 a-z 。 
// Related Topics 字符串 
// 👍 1250 👎 0

//leetcode submit region begin(Prohibit modification and deletion)
class Solution2 {
    public static String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();

        int minLength = Integer.MAX_VALUE;
        for (String str : strs) {
            minLength = Math.min(str.length(), minLength);
        }
        for (int i = 0; i < minLength; i++) {
            char charAt = strs[0].charAt(i);
            for (String str : strs) {
                if (str.charAt(i) != charAt) {
                    return result.toString();
                }
            }
            result.append(charAt);
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String[] strs = new String[2];
        strs[0] = "abc";
        strs[1] = "abde";
        System.out.println(longestCommonPrefix(strs));
    }
}
//leetcode submit region end(Prohibit modification and deletion)
