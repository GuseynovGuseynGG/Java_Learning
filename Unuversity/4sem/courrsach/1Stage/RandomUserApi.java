package aggregator;

import com.fasterxml.jackson.databind.JsonNode;

public class RandomUserApi extends HttpApiSource {
    @Override public String getName() { return "randomuser"; }
    @Override public String getUrl() {
        return "https://randomuser.me/api/?results=1&inc=name,location,email,login,dob,picture";
    }
    @Override protected JsonNode extract(JsonNode root) {
        return root.path("results").path(0);
    }
}