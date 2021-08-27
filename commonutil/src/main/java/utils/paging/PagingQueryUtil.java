package utils.paging;

import java.io.Serializable;
import java.util.List;

import utils.func.Func1;

/**
 * Created by Jeff_xu on 26/02/2019.
 *
 * @author Jeff_xu
 */
public class PagingQueryUtil {

    /**
     * 分页查询
     *
     * @param pageParameter 前端的分页参数
     * @param func          func
     * @param <T>           T
     * @return result
     */
    public static <T extends PagingQueryFilterable & Serializable> PagingQueryResp<T> pagingQuery(
        PageParameter pageParameter,
        Func1<List<T>, PageParameter> func) {
        return pagingQuery(5, pageParameter, func);
    }

    /**
     * 分页查询
     *
     * @param bufferSize    后端预取数据大小
     * @param pageParameter 前端的分页参数
     * @param func          func
     * @param <T>           T
     * @return result
     */
    public static <T extends PagingQueryFilterable & Serializable> PagingQueryResp<T> pagingQuery(int bufferSize,
                                                                                                  PageParameter pageParameter,
                                                                                                  Func1<List<T>, PageParameter> func) {
        int expectSize = pageParameter.limit;
        pageParameter.limit = pageParameter.limit + bufferSize;
        PagingQueryResp<T> pagingQueryResp = new PagingQueryResp<T>(pageParameter.offset);
        return pagingQuery(expectSize, pageParameter, func, pagingQueryResp);
    }

    /**
     * 分页查询
     *
     * @param expectSize       当前缺少的数据量
     * @param newPageParameter 当前查询分页参数
     * @param func             func
     * @param pagingQueryResp  返回数据集
     * @param <T>              T
     * @return result
     */
    private static <T extends PagingQueryFilterable & Serializable> PagingQueryResp<T> pagingQuery(int expectSize,
                                                                                                   PageParameter newPageParameter,
                                                                                                   Func1<List<T>, PageParameter> func,
                                                                                                   PagingQueryResp<T> pagingQueryResp) {
        List<T> queryResult = func.invoke(newPageParameter);
        for (T t : queryResult) {
            if (expectSize > 0) {
                if (!t.filtered()) {
                    pagingQueryResp.result.add(t);
                    expectSize--;
                }
                pagingQueryResp.cursor++;
            } else {
                break;
            }
        }
        // expectSize > 0 ，还需要请求下一页来填充当前的缺失数据
        if (queryResult.size() >= newPageParameter.limit && expectSize > 0) {
            newPageParameter.offset = newPageParameter.offset + newPageParameter.limit;
            pagingQuery(expectSize, newPageParameter, func, pagingQueryResp);
        }
        return pagingQueryResp;
    }
}
