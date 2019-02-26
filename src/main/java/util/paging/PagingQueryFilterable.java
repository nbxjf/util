package util.paging;

/**
 * Created by Jeff_xu on 26/02/2019.
 *
 * @author Jeff_xu
 */
public interface PagingQueryFilterable {
    /**
     * page query 是否被需要被过滤
     *
     * @return 是否被需要被过滤
     */
    boolean filtered();
}
