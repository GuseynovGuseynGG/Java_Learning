package aggregator;

import com.fasterxml.jackson.databind.JsonNode;

public interface ApiSource {
    String getName();
    String getUrl();
    JsonNode fetch() throws Exception;
}