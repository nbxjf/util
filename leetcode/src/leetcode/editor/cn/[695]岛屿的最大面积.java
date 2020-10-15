package leetcode.editor.cn;

//给定一个包含了一些 0 和 1 的非空二维数组 grid 。 
//
// 一个 岛屿 是由一些相邻的 1 (代表土地) 构成的组合，这里的「相邻」要求两个 1 必须在水平或者竖直方向上相邻。你可以假设 grid 的四个边缘都被 
//0（代表水）包围着。 
//
// 找到给定的二维数组中最大的岛屿面积。(如果没有岛屿，则返回面积为 0 。) 
//
// 
//
// 示例 1: 
//
// [[0,0,1,0,0,0,0,1,0,0,0,0,0],
// [0,0,0,0,0,0,0,1,1,1,0,0,0],
// [0,1,1,0,1,0,0,0,0,0,0,0,0],
// [0,1,0,0,1,1,0,0,1,0,1,0,0],
// [0,1,0,0,1,1,0,0,1,1,1,0,0],
// [0,0,0,0,0,0,0,0,0,0,1,0,0],
// [0,0,0,0,0,0,0,1,1,1,0,0,0],
// [0,0,0,0,0,0,0,1,1,0,0,0,0]]
// 
//
// 对于上面这个给定矩阵应返回 6。注意答案不应该是 11 ，因为岛屿只能包含水平或垂直的四个方向的 1 。 
//
// 示例 2: 
//
// [[0,0,0,0,0,0,0,0]] 
//
// 对于上面这个给定的矩阵, 返回 0。 
//
// 
//
// 注意: 给定的矩阵grid 的长度和宽度都不超过 50。 
// Related Topics 深度优先搜索 数组 
// 👍 352 👎 0

class MaxAreaOfIsland {
    public static void main(String[] args) {
        Solution solution = new MaxAreaOfIsland().new Solution();
        int[][] grid = new int[][] {
            {0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0},
            {0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0}
        };
        System.out.println(solution.maxAreaOfIsland(grid));
    }

    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public int maxAreaOfIsland(int[][] grid) {
            if (grid == null || grid.length <= 0) {
                return 0;
            }
            int[][] searched = new int[grid.length][grid[0].length];
            int max = 0;
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    int traverse = deepTraverse(grid, searched, i, j);
                    max = Math.max(max, traverse);
                }
            }
            return max;
        }

        public int deepTraverse(int[][] grid, int[][] searched, int i, int j) {
            // 超出数组的长度
            int result = 0;
            if (i < 0 || j < 0 || i >= grid.length || j >= grid[i].length) {
                return result;
            }
            // 已经超找过无需再次查找
            if (searched[i][j] == 1) {
                return result;
            }
            // 标记当前已查找过
            searched[i][j] = 1;
            if (grid[i][j] == 1) {
                result += 1;
                result += deepTraverse(grid, searched, i - 1, j);
                result += deepTraverse(grid, searched, i, j + 1);
                result += deepTraverse(grid, searched, i + 1, j);
                result += deepTraverse(grid, searched, i, j - 1);
            }
            return result;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}
