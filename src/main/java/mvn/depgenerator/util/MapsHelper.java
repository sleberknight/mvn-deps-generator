package mvn.depgenerator.util;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public final class MapsHelper {

    public static String string(Map<String, Object> map, String key) {
        return (String) map.get(key);
    }

    public static Long longValue(Map<String, Object> map, String key) {
        return (Long) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<String> immutableStringList(Map<String, Object> map, String key) {
        List<String> strings = (List<String>) map.get(key);
        return ImmutableList.copyOf(strings);
    }
}
