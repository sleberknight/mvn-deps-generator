package mvn.depgenerator.util;

import lombok.experimental.UtilityClass;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@UtilityClass
public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Map<String, Object> toMap(String json) {
        return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

}
