package leetcode.editor.cn;

/**
 * Created by Jeff_xu on 2021/1/21.
 *
 * @author Jeff_xu
 */
public class Sqrt {

    public static void main(String[] args) {
        Sqrt sqrt = new Sqrt();
        int i = sqrt.mySqrt(2147395600);
        System.out.println(i);
    }

    public int mySqrt(int x) {
        if(x == 0){
            return 0;
        }
        int result = 0;
        for(int i = 46339; i <= x; i ++){
            long temp = (long)i * i;
            if(temp <= x){
                result = i;
            }else{
                break;
            }
        }
        return result;
    }
}
