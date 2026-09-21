package aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordStorageTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RecordStorage storage = new RecordStorage();

    private DataRecord record(long id, String source, String json) throws Exception {
        return new DataRecord(id, source, "2026-02-04T11:56:23+00:00", mapper.readTree(json));
    }

    @Test
    void savesJsonArray(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.json");
        storage.save(List.of(record(1, "a", "{\"x\":1}")), file, RecordStorage.Format.JSON, false);

        List<DataRecord> records = storage.readJson(file);
        assertEquals(1, records.size());
        assertEquals("a", records.get(0).getSource());
        assertEquals(1, records.get(0).getData().get("x").asInt());
    }

    @Test
    void appendsToJson(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.json");
        storage.save(List.of(record(1, "a", "{\"x\":1}")), file, RecordStorage.Format.JSON, false);
        storage.save(List.of(record(2, "b", "{\"y\":2}")), file, RecordStorage.Format.JSON, true);
        assertEquals(2, storage.readJson(file).size());
    }

    @Test
    void overwritesJsonWithoutAppend(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.json");
        storage.save(List.of(record(1, "a", "{\"x\":1}")), file, RecordStorage.Format.JSON, false);
        storage.save(List.of(record(2, "b", "{\"y\":2}")), file, RecordStorage.Format.JSON, false);

        List<DataRecord> records = storage.readJson(file);
        assertEquals(1, records.size());
        assertEquals("b", records.get(0).getSource());
    }

    @Test
    void savesCsvWithFlattenedHeaders(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.csv");
        storage.save(List.of(record(1, "a", "{\"x\":1,\"nested\":{\"y\":2}}")),
                file, RecordStorage.Format.CSV, false);

        List<String> lines = Files.readAllLines(file);
        assertTrue(lines.get(0).startsWith("id,source,timestamp"));
        assertTrue(lines.get(0).contains("data.x"));
        assertTrue(lines.get(0).contains("data.nested.y"));
        assertTrue(lines.get(1).startsWith("1,a,"));
    }

    @Test
    void csvEscapesCommaInValue(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.csv");
        storage.save(List.of(record(1, "a", "{\"x\":\"hello, world\"}")),
                file, RecordStorage.Format.CSV, false);

        List<Map<String, String>> rows = RecordStorage.readCsvRows(file);
        assertEquals("hello, world", rows.get(0).get("data.x"));
    }

    @Test
    void csvAppendUnionsHeaders(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.csv");
        storage.save(List.of(record(1, "a", "{\"x\":1}")), file, RecordStorage.Format.CSV, false);
        storage.save(List.of(record(2, "b", "{\"y\":2}")), file, RecordStorage.Format.CSV, true);

        List<Map<String, String>> rows = RecordStorage.readCsvRows(file);
        assertEquals(2, rows.size());
        assertEquals("1", rows.get(0).get("data.x"));
        assertEquals("2", rows.get(1).get("data.y"));
    }

    @Test
    void parseCsvLineHandlesQuotes() {
        assertEquals(List.of("a", "b,c", "d\"e"), RecordStorage.parseCsvLine("a,\"b,c\",\"d\"\"e\""));
    }

    @Test
    void escapeWrapsSpecialCharacters() {
        assertEquals("\"a,b\"", RecordStorage.escape("a,b"));
        assertEquals("plain", RecordStorage.escape("plain"));
        assertEquals("", RecordStorage.escape(null));
    }

    @Test
    void flattenArrayAsJsonText() throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        RecordStorage.flatten(mapper.readTree("{\"arr\":[1,2]}"), "data", out);
        assertEquals("[1,2]", out.get("data.arr"));
    }

    @Test
    void readJsonOfMissingFileReturnsEmpty(@TempDir Path dir) throws Exception {
        assertTrue(storage.readJson(dir.resolve("none.json")).isEmpty());
    }
}