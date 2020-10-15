package common;

/**
 * Created by Jeff_xu on 27/02/2019.
 *
 * @author Jeff_xu
 */
public interface Action2<T1, T2> {
    void invoke(T1 t1, T2 t2);
}
