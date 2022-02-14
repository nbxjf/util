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
     * 默认的分页查询buffer大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 5;

    /**
     * 分页查询
     *
     * @param pageParameter 前端的分页参数
     * @param func          func
     * @param timeoutMillis 超时时间，单位毫秒，如果超过了timeout时间，也会中断分页查询并返回已经查询到的数据
     * @param <T>           T
     * @return result
     */
    public static <T extends PagingQueryFilterable & Serializable> PagingQueryResp<T> pagingQuery(PageParameter pageParameter,
                                                                                                  Func1<List<T>, PageParameter> func,
                                                                                                  long timeoutMillis) {
        return pagingQuery(DEFAULT_BUFFER_SIZE, pageParameter, func, timeoutMillis);
    }

    /**
     * 分页查询
     *
     * @param bufferSize    后端预取数据大小
     * @param pageParameter 前端的分页参数
     * @param func          func
     * @param timeoutMillis 超时时间，单位毫秒，如果超过了timeout时间，也会中断分页查询并返回已经查询到的数据
     * @param <T>           T
     * @return result
     */
    public static <T extends PagingQueryFilterable & Serializable> PagingQueryResp<T> pagingQuery(int bufferSize,
                                                                                                  PageParameter pageParameter,
                                                                                                  Func1<List<T>, PageParameter> func,
                                                                                                  long timeoutMillis) {
        int expectSize = pageParameter.limit;
        pageParameter.limit = pageParameter.limit + bufferSize;
        PagingQueryResp<T> pagingQueryResp = new PagingQueryResp<T>(pageParameter.offset);
        return pagingQuery(expectSize, pageParameter, func, pagingQueryResp, System.currentTimeMillis(), timeoutMillis);
    }

    /**
     * 分页查询
     *
     * @param expectSize       当前缺少的数据量
     * @param newPageParameter 当前查询分页参数
     * @param func             func
     * @param pagingQueryResp  返回数据集
     * @param startTimeMillis  开始执行时间
     * @param timeoutMillis    超时时间，单位毫秒，如果超过了timeout时间，也会中断分页查询并返回已经查询到的数据
     * @param <T>              T
     * @return result
     */
    private static <T extends PagingQueryFilterable & Serializable> PagingQueryResp<T> pagingQuery(int expectSize,
                                                                                                   PageParameter newPageParameter,
                                                                                                   Func1<List<T>, PageParameter> func,
                                                                                                   PagingQueryResp<T> pagingQueryResp,
                                                                                                   long startTimeMillis,
                                                                                                   long timeoutMillis) {
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
        // 如果当前执行时间已经超过了设置的超时时间，跳出递归返回当前的查询结果值，防止死循环
        if (System.currentTimeMillis() - startTimeMillis >= timeoutMillis) {
            return pagingQueryResp;
        }
        // expectSize > 0 ，还需要请求下一页来填充当前的缺失数据
        if (queryResult.size() >= newPageParameter.limit && expectSize > 0) {
            newPageParameter.offset = newPageParameter.offset + newPageParameter.limit;
            pagingQuery(expectSize, newPageParameter, func, pagingQueryResp, startTimeMillis, timeoutMillis);
        }
        return pagingQueryResp;
    }
}
