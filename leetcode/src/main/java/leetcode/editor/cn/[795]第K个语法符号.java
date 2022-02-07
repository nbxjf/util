package leetcode.editor.cn;

//在第一行我们写上一个 0。接下来的每一行，将前一行中的0替换为01，1替换为10。 
//
// 给定行数 N 和序数 K，返回第 N 行中第 K个字符。（K从1开始） 
//
// 
//例子: 
//
// 输入: N = 1, K = 1
//输出: 0
//
//输入: N = 2, K = 1
//输出: 0
//
//输入: N = 2, K = 2
//输出: 1
//
//输入: N = 4, K = 5
//输出: 1
//
//解释:
//第一行: 0
//第二行: 01
//第三行: 0110
//第四行: 01101001
// 
//
// 
//注意： 
//
// 
// N 的范围 [1, 30]. 
// K 的范围 [1, 2^(N-1)]. 
// 
// Related Topics 递归 
// 👍 117 👎 0

class KThSymbolInGrammar {
    public static void main(String[] args) {
        Solution solution = new KThSymbolInGrammar().new Solution();
        int i = solution.kthGrammar(3, 3);
        System.out.printf("i");
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public int kthGrammar(int N, int K) {
            String res = getG(N);
            return res.charAt(K - 1) - '0';
        }

        private String getG(int n) {
            if (n == 1) {
                return "0";
            } else {
                String g = getG(n - 1);
                g = g.replace("0", "2").replace("1", "3");
                g = g.replace("2", "01").replace("3", "10");

                return g;
            }
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
