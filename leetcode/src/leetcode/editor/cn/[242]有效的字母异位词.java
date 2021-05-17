package leetcode.editor.cn;

//给定两个字符串 s 和 t ，编写一个函数来判断 t 是否是 s 的字母异位词。 
//
// 示例 1: 
//
// 输入: s = "anagram", t = "nagaram"
//输出: true
// 
//
// 示例 2: 
//
// 输入: s = "rat", t = "car"
//输出: false 
//
// 说明: 
//你可以假设字符串只包含小写字母。 
//
// 进阶: 
//如果输入字符串包含 unicode 字符怎么办？你能否调整你的解法来应对这种情况？ 
// Related Topics 排序 哈希表 
// 👍 357 👎 0

import java.util.HashMap;
import java.util.Map;

class ValidAnagram {
    public static void main(String[] args) {
        Solution solution = new ValidAnagram().new Solution();

    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public boolean isAnagram(String s, String t) {
            Map<Character, Integer> sMap = new HashMap<>();
            for (int i = 0; i < s.length(); i++) {
                sMap.merge(s.charAt(i), 1, Integer::sum);
            }
            Map<Character, Integer> tMap = new HashMap<>();
            for (int i = 0; i < t.length(); i++) {
                tMap.merge(t.charAt(i), 1, Integer::sum);
            }
            return sMap.equals(tMap);
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
