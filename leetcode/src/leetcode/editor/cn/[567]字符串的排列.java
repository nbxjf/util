package leetcode.editor.cn;

//给定两个字符串 s1 和 s2，写一个函数来判断 s2 是否包含 s1 的排列。 
//
// 换句话说，第一个字符串的排列之一是第二个字符串的子串。 
//
// 示例1: 
//
// 
//输入: s1 = "ab" s2 = "eidbaooo"
//输出: True
//解释: s2 包含 s1 的排列之一 ("ba").
// 
//
// 
//
// 示例2: 
//
// 
//输入: s1= "ab" s2 = "eidboaoo"
//输出: False
// 
//
// 
//
// 注意： 
//
// 
// 输入的字符串只包含小写字母 
// 两个字符串的长度都在 [1, 10,000] 之间 
// 
// Related Topics 双指针 Sliding Window 
// 👍 172 👎 0

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class PermutationInString {
    public static void main(String[] args) {
        Solution solution = new PermutationInString().new Solution();

        System.out.println(solution.checkInclusion("adc", "dcda"));
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        /**
         * 暴力解法
         * 会存在超时问题
         */
        public boolean checkInclusion_1(String s1, String s2) {
            if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty() || s2.length() < s1.length()) {
                return false;
            }
            if (s1.equals(s2) || s2.contains(s1)) {
                return true;
            }
            List<Character> characterList = new ArrayList<>();
            for (int i = 0; i < s1.length(); i++) {
                characterList.add(s1.charAt(i));
            }
            int maxLength = s2.length();

            for (int i = 0; i < maxLength; i++) {
                List<Character> tempList = new ArrayList<>(characterList);
                int size = 0;
                for (int j = i; j < Math.min(maxLength, i + s1.length()); j++) {
                    char charAt = s2.charAt(j);
                    if (!tempList.contains(charAt)) {
                        if (!characterList.contains(charAt)) {
                            i = j;
                        }
                        break;
                    } else {
                        tempList.remove(Character.valueOf(charAt));
                        size++;
                    }
                }
                if (size == s1.length()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 优化解法
         * 使用滑动窗口，每次比较s1长度的区间数据，使用 hashTable 判断字符串是否相等
         */
        public boolean checkInclusion(String s1, String s2) {
            if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty() || s2.length() < s1.length()) {
                return false;
            }
            if (s1.equals(s2) || s2.contains(s1)) {
                return true;
            }
            HashMap<Character, Integer> charSet = new HashMap<>(10000);
            for (int i = 0; i < s1.length(); i++) {
                charSet.merge(s1.charAt(i), 1, Integer::sum);
            }
            HashMap<Character, Integer> s2HashMap = new HashMap<>(10000);
            for (int i = 0; i < s1.length(); i++) {
                s2HashMap.merge(s2.charAt(i), 1, Integer::sum);
            }
            if (charSet.equals(s2HashMap)) {
                return true;
            }
            for (int i = 0; i < s2.length() - s1.length(); i++) {
                s2HashMap.merge(s2.charAt(i), -1, Integer::sum);
                if (s2HashMap.get(s2.charAt(i)) <= 0) {
                    s2HashMap.remove(s2.charAt(i));
                }
                s2HashMap.merge(s2.charAt(i + s1.length()), 1, Integer::sum);
                if (charSet.equals(s2HashMap)) {
                    return true;
                }
            }
            return false;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
