package aggregator;

import com.fasterxml.jackson.databind.JsonNode;

public class RestCountriesApi extends HttpApiSource {
    @Override public String getName() { return "restcountries"; }
    @Override public String getUrl() {
        return "https://restcountries.com/v3.1/name/germany?fields=name,capital,population,currencies,languages,flags";
    }
    @Override protected JsonNode extract(JsonNode root) {
        return root.isArray() && root.size() > 0 ? root.get(0) : root;
    }
}