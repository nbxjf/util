package common;

/**
 * Created by Jeff_xu on 27/02/2019.
 *
 * @author Jeff_xu
 */
public interface Action3<T1, T2, T3> {
    void invoke(T1 t1, T2 t2, T3 t3);
}
