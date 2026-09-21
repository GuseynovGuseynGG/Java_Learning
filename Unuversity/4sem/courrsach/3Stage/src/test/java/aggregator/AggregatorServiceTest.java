package aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregatorServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private ApiRegistry registryWith(ApiSource source) {
        ApiRegistry registry = new ApiRegistry();
        registry.register(source);
        return registry;
    }

    @Test
    void collectOneBuildsRecord() throws Exception {
        ApiSource source = mock(ApiSource.class);
        when(source.getName()).thenReturn("test");
        when(source.fetch()).thenReturn(mapper.readTree("{\"a\":1,\"nested\":{\"b\":2}}"));

        AggregatorService service = new AggregatorService(registryWith(source));
        List<DataRecord> records = service.collectOne("test");

        assertEquals(1, records.size());
        assertEquals("test", records.get(0).getSource());
        assertEquals(1, records.get(0).getData().get("a").asInt());
        assertNotNull(records.get(0).getTimestamp());
    }

    @Test
    void collectOneUnknownApiReturnsEmpty() {
        AggregatorService service = new AggregatorService(new ApiRegistry());
        assertTrue(service.collectOne("missing").isEmpty());
    }

    @Test
    void collectOneHandlesNetworkFailure() throws Exception {
        ApiSource source = mock(ApiSource.class);
        when(source.getName()).thenReturn("bad");
        when(source.fetch()).thenThrow(new RuntimeException("boom"));

        AggregatorService service = new AggregatorService(registryWith(source));
        assertTrue(service.collectOne("bad").isEmpty());
    }

    @Test
    void collectAggregatesSeveralApisWithIncrementingIds() throws Exception {
        ApiSource first = mock(ApiSource.class);
        when(first.getName()).thenReturn("first");
        when(first.fetch()).thenReturn(mapper.readTree("{\"x\":1}"));

        ApiSource second = mock(ApiSource.class);
        when(second.getName()).thenReturn("second");
        when(second.fetch()).thenReturn(mapper.readTree("{\"y\":2}"));

        ApiRegistry registry = new ApiRegistry();
        registry.register(first);
        registry.register(second);

        AggregatorService service = new AggregatorService(registry);
        List<DataRecord> records = service.collect(List.of("first", "second"));

        assertEquals(2, records.size());
        assertEquals(1, records.get(0).getId());
        assertEquals(2, records.get(1).getId());
        assertEquals("first", records.get(0).getSource());
        assertEquals("second", records.get(1).getSource());
    }
}