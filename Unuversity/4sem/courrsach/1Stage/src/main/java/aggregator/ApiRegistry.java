package aggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiRegistry {

    private final Map<String, ApiSource> sources = new LinkedHashMap<>();

    public ApiRegistry() {
        register(new JsonPlaceholderApi());
        register(new RandomUserApi());
        register(new RestCountriesApi());
    }

    public void register(ApiSource source) {
        sources.put(source.getName(), source);
    }

    public ApiSource get(String name) {
        return sources.get(name);
    }

    public Collection<ApiSource> all() {
        return sources.values();
    }

    public List<String> names() {
        return new ArrayList<>(sources.keySet());
    }
}