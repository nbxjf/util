package utils.paging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeff_xu on 26/02/2019.
 *
 * 分页查询统一返回实体
 *
 * @param <T>
 * @author Jeff_xu
 */
public class PagingQueryResp<T extends Serializable> implements Serializable {
    /**
     * 列表数据
     */
    public List<T> result = new ArrayList<T>();
    /**
     * 当前游标位置
     */
    public int cursor = 0;

    public PagingQueryResp(int cursor) {
        this.cursor = cursor;
    }

    public PagingQueryResp() {
    }

}
