package leetcode.editor.cn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jeff_xu on 2021/1/21.
 * 有n个球，红色和白色，其中3个颜色相同的球排在一起是不合法的，输入一个正整数n，求合法的排列组合的数量
 *
 * @author Jeff_xu
 */
public class LegalCount {

    public int getLegalCount(int n) {
        int total = (int)Math.pow(2, (double)n);
        if (n < 3) {
            return total;
        }
        Set<List<Integer>> legalCountArray = getLegalCountArray(n);
        return legalCountArray.size();
    }

    public Set<List<Integer>> getLegalCountArray(int n) {
        Set<List<Integer>> result = new HashSet<>();
        if (n == 1) {
            result.add(Arrays.asList(1));
            result.add(Arrays.asList(0));
            return result;
        }

        Set<List<Integer>> subResultList = getLegalCountArray(n - 1);

        for (List<Integer> sub : subResultList) {
            result.addAll(trace(sub, 0));
            result.addAll(trace(sub, 1));
        }
        return result;
    }

    public Set<List<Integer>> trace(List<Integer> sub, int num) {
        Set<List<Integer>> result = new HashSet<>();
        for (int i = 0; i <= sub.size(); i++) {
            List<Integer> newSub = new ArrayList<>(sub);
            newSub.add(i, num);
            if (judgeIsLegal(newSub)) {
                result.add(newSub);
            }
        }
        return result;
    }

    private boolean judgeIsLegal(List<Integer> sub) {
        for (int i = 0; i < sub.size() - 2; i++) {
            if (sub.get(i).intValue() == sub.get(i + 1) && sub.get(i + 1).intValue() == sub.get(i + 2)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        LegalCount main = new LegalCount();
        int result = main.getLegalCount(2);
        System.out.println(result);
    }
}
