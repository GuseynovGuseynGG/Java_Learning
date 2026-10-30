package aggregator;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AggregatorService {

    private final ApiRegistry registry;
    private final AtomicLong idCounter = new AtomicLong();

    public AggregatorService(ApiRegistry registry) {
        this.registry = registry;
    }

    public List<DataRecord> collect(List<String> apiNames) {
        List<DataRecord> result = new ArrayList<>();
        for (String name : apiNames) {
            result.addAll(collectOne(name));
        }
        return result;
    }

    public List<DataRecord> collectOne(String apiName) {
        ApiSource source = registry.get(apiName);
        if (source == null) {
            return List.of();
        }
        try {
            JsonNode data = source.fetch();
            DataRecord record = new DataRecord(
                    idCounter.incrementAndGet(),
                    source.getName(),
                    OffsetDateTime.now().toString(),
                    data
            );
            return List.of(record);
        } catch (Exception e) {
            System.err.println("Ошибка получения данных из " + apiName + ": " + e.getMessage());
            return List.of();
        }
    }
}