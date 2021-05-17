package leetcode.editor.cn;

//给定一个非负索引 k，其中 k ≤ 33，返回杨辉三角的第 k 行。 
//
// 
//
// 在杨辉三角中，每个数是它左上方和右上方的数的和。 
//
// 示例: 
//
// 输入: 3
//输出: [1,3,3,1]
// 
//
// 进阶： 
//
// 你可以优化你的算法到 O(k) 空间复杂度吗？ 
// Related Topics 数组 
// 👍 271 👎 0

import java.util.ArrayList;
import java.util.List;

class PascalsTriangleIi {
    public static void main(String[] args) {
        Solution solution = new PascalsTriangleIi().new Solution();

    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public List<Integer> getRow(int rowIndex) {
            List<Integer> level = new ArrayList<>();
            if (rowIndex == 1) {
                level.add(1);
                return level;
            }
            List<Integer> subResult = getRow(rowIndex - 1);
            for (int i = 0; i < rowIndex; i++) {
                if (i == 0) {
                    level.add(subResult.get(i));
                } else if (i == rowIndex - 1) {
                    level.add(subResult.get(subResult.size() - 1));
                } else {
                    level.add(subResult.get(i - 1) + subResult.get(i));
                }
            }
            return level;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
