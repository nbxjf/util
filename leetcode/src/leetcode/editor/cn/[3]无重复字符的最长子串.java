package leetcode.editor.cn;

//给定一个字符串，请你找出其中不含有重复字符的 最长子串 的长度。 
//
// 示例 1: 
//
// 输入: "abcabcbb"
//输出: 3 
//解释: 因为无重复字符的最长子串是 "abc"，所以其长度为 3。
// 
//
// 示例 2: 
//
// 输入: "bbbbb"
//输出: 1
//解释: 因为无重复字符的最长子串是 "b"，所以其长度为 1。
// 
//
// 示例 3: 
//
// 输入: "pwwkew"
//输出: 3
//解释: 因为无重复字符的最长子串是 "wke"，所以其长度为 3。
//     请注意，你的答案必须是 子串 的长度，"pwke" 是一个子序列，不是子串。
// 
// Related Topics 哈希表 双指针 字符串 Sliding Window 
// 👍 4280 👎 0

import java.util.ArrayList;
import java.util.List;

//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    public static int lengthOfLongestSubstring(String s) {
        int maxLength = 0;
        if (s == null || s.isEmpty()) {
            return maxLength;
        }
        for (int i = 0; i < s.length(); i++) {
            List<Character> allCharacter = new ArrayList<>();
            for (int j = i; j < s.length(); j++) {
                char c = s.charAt(j);
                if (allCharacter.contains(c)) {
                    break;
                } else {
                    allCharacter.add(c);
                }
                maxLength = Math.max(maxLength, allCharacter.size());
            }
        }
        return maxLength;
    }


    public static void main(String[] args) {
        System.out.println(lengthOfLongestSubstring("abcabcbb"));
    }

}
//leetcode submit region end(Prohibit modification and deletion)
