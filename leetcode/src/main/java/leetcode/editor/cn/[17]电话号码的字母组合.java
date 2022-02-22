package leetcode.editor.cn;

//给定一个仅包含数字 2-9 的字符串，返回所有它能表示的字母组合。答案可以按 任意顺序 返回。 
//
// 给出数字到字母的映射如下（与电话按键相同）。注意 1 不对应任何字母。 
//
// 
//
// 
//
// 示例 1： 
//
// 
//输入：digits = "23"
//输出：["ad","ae","af","bd","be","bf","cd","ce","cf"]
// 
//
// 示例 2： 
//
// 
//输入：digits = ""
//输出：[]
// 
//
// 示例 3： 
//
// 
//输入：digits = "2"
//输出：["a","b","c"]
// 
//
// 
//
// 提示： 
//
// 
// 0 <= digits.length <= 4 
// digits[i] 是范围 ['2', '9'] 的一个数字。 
// 
// Related Topics 哈希表 字符串 回溯 👍 1713 👎 0


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LetterCombinationsOfAPhoneNumber {
    public static void main(String[] args) {
        Solution solution = new LetterCombinationsOfAPhoneNumber().new Solution();
        List<String> strings = solution.letterCombinations("2");
        strings.forEach(System.out::println);
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {

        private Map<Character, String> numberMap = new HashMap<Character, String>() {
            {
                put('2', "abc");
                put('3', "def");
                put('4', "ghi");
                put('5', "jkl");
                put('6', "mno");
                put('7', "pqrs");
                put('8', "tuv");
                put('9', "wxyz");
            }
        };

        public List<String> letterCombinations(String digits) {
            if (digits == null || digits.isEmpty()) {
                return new ArrayList<>();
            }
            char[] charArray = digits.toCharArray();
            List<String> result = new ArrayList<>();
            for (char c : charArray) {
                String s = numberMap.get(c);
                List<String> tempResult = new ArrayList<>();
                for (int i = 0; i < s.length(); i++) {
                    if (result.isEmpty()) {
                        tempResult.add(String.valueOf(s.charAt(i)));
                    } else {
                        for (String s1 : result) {
                            tempResult.add(s1 + String.valueOf(s.charAt(i)));
                        }
                    }
                }
                result = tempResult;
            }
            return result;
        }
    }

//leetcode submit region end(Prohibit modification and deletion)

}
