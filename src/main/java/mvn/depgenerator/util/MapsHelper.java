package mvn.depgenerator.util;

import static java.util.Objects.isNull;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

@UtilityClass
public final class MapsHelper {

    public static String string(Map<String, Object> map, String key) {
        return (String) map.get(key);
    }

    public static Long longValue(Map<String, Object> map, String key) {
        return (Long) map.get(key);
    }

    public static Integer integerValue(Map<String, Object> map, String key) {
        return (Integer) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static List<String> immutableStringList(Map<String, Object> map, String key) {
        if (isNull(map.get(key))) {
            return List.of();
        }

        List<String> strings = (List<String>) map.get(key);
        return List.copyOf(strings);
    }
}
