package leetcode.editor.cn;

//给定一个只包含数字的字符串，复原它并返回所有可能的 IP 地址格式。 
//
// 有效的 IP 地址 正好由四个整数（每个整数位于 0 到 255 之间组成，且不能含有前导 0），整数之间用 '.' 分隔。 
//
// 例如："0.1.2.201" 和 "192.168.1.1" 是 有效的 IP 地址，但是 "0.011.255.245"、"192.168.1.312"
// 和 "192.168@1.1" 是 无效的 IP 地址。 
//
// 
//
// 示例 1： 
//
// 输入：s = "25525511135"
//输出：["255.255.11.135","255.255.111.35"]
// 
//
// 示例 2： 
//
// 输入：s = "0000"
//输出：["0.0.0.0"]
// 
//
// 示例 3： 
//
// 输入：s = "1111"
//输出：["1.1.1.1"]
// 
//
// 示例 4： 
//
// 输入：s = "010010"
//输出：["0.10.0.10","0.100.1.0"]
// 
//
// 示例 5： 
//
// 输入：s = "101023"
//输出：["1.0.10.23","1.0.102.3","10.1.0.23","10.10.2.3","101.0.2.3"]
// 
//
// 
//
// 提示： 
//
// 
// 0 <= s.length <= 3000 
// s 仅由数字组成 
// 
// Related Topics 字符串 回溯算法 
// 👍 415 👎 0

import java.util.ArrayList;
import java.util.List;

class RestoreIpAddresses {
    public static void main(String[] args) {
        Solution solution = new RestoreIpAddresses().new Solution();
        System.out.println(solution.restoreIpAddresses("101023"));
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public List<String> restoreIpAddresses(String s) {
            List<String> result = new ArrayList<>();
            if (s.length() > 12) {
                return result;
            }
            result =  parseIpAddresses(s, 1);
            return result;
        }

        private List<String> parseIpAddresses(String s, int index) {
            List<String> result = new ArrayList<>();

            if (index >= 4) {
                if (isIpNumber(s)) {
                    result.add(s);
                }
                return result;
            }

            for (int i = 0; i < Math.min(3, s.length()); i++) {
                String start = s.substring(0, i + 1);
                if (isIpNumber(start)) {
                    List<String> strings = parseIpAddresses(s.substring(i + 1), index + 1);
                    if (!strings.isEmpty()) {
                        for (String string : strings) {
                            result.add(start + "." + string);
                        }
                    }
                }
            }
            return result;
        }

        private boolean isIpNumber(String num) {
            if (num.startsWith("0") && num.length() > 1) {
                return false;
            }
            try {
                if (Integer.parseInt(num) <= 255) {
                    return true;
                }
            } catch (Exception ignored) {

            }
            return false;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
