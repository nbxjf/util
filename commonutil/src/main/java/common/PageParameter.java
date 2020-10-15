package common;

import java.io.Serializable;

/**
 * Created by Jeff_xu on 26/02/2019.
 *
 * @author Jeff_xu
 */
public class PageParameter implements Serializable {
    public int offset;
    public int limit;

    public PageParameter() {
    }
}
