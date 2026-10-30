package aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PollingManagerTest {

    private static class CountingApi implements ApiSource {
        private final AtomicInteger calls = new AtomicInteger();
        private final CountDownLatch latch;

        CountingApi(CountDownLatch latch) { this.latch = latch; }
        @Override public String getName() { return "counter"; }
        @Override public String getUrl() { return "http://stub"; }
        @Override public JsonNode fetch() {
            calls.incrementAndGet();
            latch.countDown();
            return null;
        }
    }

    @Test
    void pollsRepeatedlyAndSavesToFile(@TempDir Path dir) throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        CountingApi api = new CountingApi(latch);

        ApiRegistry registry = new ApiRegistry();
        registry.register(api);
        AggregatorService service = new AggregatorService(registry);

        Path file = dir.resolve("out.json");
        PollingManager manager = new PollingManager(
                service, new RecordStorage(), file, RecordStorage.Format.JSON, 2, 0);

        manager.start(List.of("counter"));
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        manager.shutdown();

        assertFalse(manager.isRunning());
        assertTrue(api.calls.get() >= 2);
    }

    @Test
    void stopStopsPolling(@TempDir Path dir) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CountingApi api = new CountingApi(latch);

        ApiRegistry registry = new ApiRegistry();
        registry.register(api);
        AggregatorService service = new AggregatorService(registry);

        PollingManager manager = new PollingManager(
                service, new RecordStorage(), dir.resolve("out.json"),
                RecordStorage.Format.JSON, 1, 0);

        manager.start(List.of("counter"));
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        manager.stop();
        assertFalse(manager.isRunning());
        manager.shutdown();
    }
}