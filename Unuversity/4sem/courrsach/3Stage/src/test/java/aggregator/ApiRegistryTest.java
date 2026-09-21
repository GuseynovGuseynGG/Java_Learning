package aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiRegistryTest {

    private static class StubApi implements ApiSource {
        private final String name;
        StubApi(String name) { this.name = name; }
        @Override public String getName() { return name; }
        @Override public String getUrl() { return "http://stub"; }
        @Override public JsonNode fetch() { return null; }
    }

    @Test
    void containsDefaultApis() {
        ApiRegistry registry = new ApiRegistry();
        assertTrue(registry.names().contains("jsonplaceholder"));
        assertTrue(registry.names().contains("randomuser"));
        assertTrue(registry.names().contains("restcountries"));
    }

    @Test
    void registerAndGet() {
        ApiRegistry registry = new ApiRegistry();
        registry.register(new StubApi("stub"));
        assertNotNull(registry.get("stub"));
        assertEquals(1, registry.all().stream().filter(s -> s.getName().equals("stub")).count());
    }

    @Test
    void getUnknownReturnsNull() {
        assertNull(new ApiRegistry().get("missing"));
    }
}