package aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class HttpApiSource implements ApiSource {

    protected static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public JsonNode fetch() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(getUrl()))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " from " + getName());
        }
        return extract(MAPPER.readTree(response.body()));
    }

    protected JsonNode extract(JsonNode root) {
        return root;
    }
}