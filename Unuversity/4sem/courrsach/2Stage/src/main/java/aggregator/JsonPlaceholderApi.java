package aggregator;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPlaceholderApi extends HttpApiSource {
    @Override public String getName() { return "jsonplaceholder"; }
    @Override public String getUrl() { return "https://jsonplaceholder.typicode.com/users?id=1"; }
    @Override protected JsonNode extract(JsonNode root) {
        return root.isArray() && root.size() > 0 ? root.get(0) : root;
    }
}