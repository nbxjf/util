package utils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.springframework.util.CollectionUtils;

/**
 * @author Jeff_xu
 * @date 2018/8/29
 */
public class BatchExecutorUtil {
    /**
     * 数据分批次处理
     *
     * @param originParam 原始数据入参
     * @param batchSize   每个批次处理的数据最大数据量
     * @param action      处理批次的action
     */
    public static void executeWithIntArray(int[] originParam, int batchSize, Function<int[], Boolean> action) {
        if (originParam == null || originParam.length == 0) {
            return;
        }
        executeWithList(Ints.asList(originParam), batchSize, integers -> {
            if (CollectionUtils.isEmpty(integers)) {
                return true;
            } else {
                return action.apply(integers.stream().mapToInt(Integer::intValue).toArray());
            }
        });
    }

    public static void executeWithLongArray(long[] originParam, int batchSize, Function<long[], Boolean> action) {
        if (originParam == null || originParam.length == 0) {
            return;
        }
        executeWithList(Longs.asList(originParam), batchSize, longs -> {
            if (CollectionUtils.isEmpty(longs)) {
                return true;
            } else {
                return action.apply(longs.stream().mapToLong(Long::longValue).toArray());
            }
        });
    }

    public static <T> void executeWithList(List<T> originParam, int batchSize, Function<List<T>, Boolean> action) {
        if (originParam != null) {
            int start = 0;
            int end = batchSize;
            int count = originParam.size() / batchSize;
            int remainder = originParam.size() % batchSize;
            for (int i = 0; i < count; i++) {
                boolean result = action.apply(originParam.stream().skip(start).limit(batchSize)
                    .collect(Collectors.toList()));
                if (!result) { return;}
                start = start + batchSize;
                end = end + batchSize;
            }
            if (remainder != 0) {
                boolean result = action.apply(originParam.stream().skip(start).limit(remainder)
                    .collect(Collectors.toList()));
                if (!result) {
                    return;
                }
            }
        }
    }

}
