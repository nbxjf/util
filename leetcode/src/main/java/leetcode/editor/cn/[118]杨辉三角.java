package leetcode.editor.cn;

//给定一个非负整数 numRows，生成杨辉三角的前 numRows 行。 
//
// 
//
// 在杨辉三角中，每个数是它左上方和右上方的数的和。 
//
// 示例: 
//
// 输入: 5
//输出:
//[
//     [1],
//    [1,1],
//   [1,2,1],
//  [1,3,3,1],
// [1,4,6,4,1]
//] 
// Related Topics 数组 
// 👍 462 👎 0

import java.util.ArrayList;
import java.util.List;

class PascalsTriangle {
    public static void main(String[] args) {
        Solution solution = new PascalsTriangle().new Solution();

    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public List<List<Integer>> generate(int numRows) {
            List<List<Integer>> result = new ArrayList<>();
            List<Integer> level = new ArrayList<>();
            if (numRows == 1) {
                level.add(1);
                result.add(level);
                return result;
            }
            List<List<Integer>> subResult = generate(numRows - 1);
            result.addAll(subResult);
            List<Integer> ints = subResult.get(subResult.size() - 1);
            for (int i = 0; i < numRows; i++) {
                if (i == 0) {
                    level.add(ints.get(i));
                } else if (i == numRows - 1) {
                    level.add(ints.get(ints.size() - 1));
                } else {
                    level.add(ints.get(i - 1) + ints.get(i));
                }
            }
            result.add(level);
            return result;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
