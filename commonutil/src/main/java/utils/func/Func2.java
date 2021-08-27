package utils.func;

/**
 * Created by Jeff_xu on 27/02/2019.
 *
 * @author Jeff_xu
 */
public interface Func2<R, T1, T2> {
    R invoke(T1 t1, T2 t2);
}
