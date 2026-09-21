package aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileViewerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RecordStorage storage = new RecordStorage();

    @Test
    void printsJsonFilteredBySource(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.json");
        storage.save(List.of(
                new DataRecord(1, "a", "t1", mapper.readTree("{\"x\":1}")),
                new DataRecord(2, "b", "t2", mapper.readTree("{\"y\":2}"))
        ), file, RecordStorage.Format.JSON, false);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new FileViewer(new PrintStream(buffer)).print(file, RecordStorage.Format.JSON, "a");

        String text = buffer.toString();
        assertTrue(text.contains("\"source\" : \"a\""));
        assertFalse(text.contains("\"source\" : \"b\""));
    }

    @Test
    void printsCsvFilteredBySource(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.csv");
        storage.save(List.of(
                new DataRecord(1, "a", "t1", mapper.readTree("{\"x\":1}")),
                new DataRecord(2, "b", "t2", mapper.readTree("{\"y\":2}"))
        ), file, RecordStorage.Format.CSV, false);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new FileViewer(new PrintStream(buffer)).print(file, RecordStorage.Format.CSV, "b");

        String text = buffer.toString();
        assertTrue(text.contains("2,b,"));
        assertFalse(text.contains("1,a,"));
    }
}