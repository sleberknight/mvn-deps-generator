package mvn.depgenerator;

import java.util.List;
import java.util.Map;

public final class MvnDependencies {

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> docs(Map<String, Object> map) {
        Map<String, Object> responseMap = (Map<String, Object>) map.get("response");
        return (List<Map<String, Object>>) responseMap.get("docs");
    }

}
