package leetcode.editor.cn;

//给定一个字符串数组，将字母异位词组合在一起。字母异位词指字母相同，但排列不同的字符串。 
//
// 示例: 
//
// 输入: ["eat", "tea", "tan", "ate", "nat", "bat"]
//输出:
//[
//  ["ate","eat","tea"],
//  ["nat","tan"],
//  ["bat"]
//] 
//
// 说明： 
//
// 
// 所有输入均为小写字母。 
// 不考虑答案输出的顺序。 
// 
// Related Topics 哈希表 字符串 
// 👍 677 👎 0

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GroupAnagrams {
    public static void main(String[] args) {
        Solution solution = new GroupAnagrams().new Solution();
        List<List<String>> lists = solution.groupAnagrams(new String[] {"eat", "tea", "tan", "ate", "nat", "bat"});
        System.out.println(lists);
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public List<List<String>> groupAnagrams(String[] strs) {
            if (strs == null || strs.length == 0) {
                return new ArrayList<>();
            }

            Map<String, List<String>> result = new HashMap<>();
            for (String str : strs) {
                List<Character> ch = new ArrayList<>();
                for (int i = 0; i < str.length(); i++) {
                    ch.add(str.charAt(i));
                }
                ch.sort(Comparator.naturalOrder());
                StringBuilder key = new StringBuilder("");
                for (Character character : ch) {
                    key.append(character);
                }
                result.merge(key.toString(), Arrays.asList(str), (old, newVal) -> {
                    List<String> n = new ArrayList<>();
                    n.addAll(newVal);
                    n.addAll(old);
                    return n;
                });
            }
            return new ArrayList<>(result.values());
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
