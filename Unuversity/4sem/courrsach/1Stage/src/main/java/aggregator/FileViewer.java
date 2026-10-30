package aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileViewer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PrintStream out;

    public FileViewer() {
        this(System.out);
    }

    public FileViewer(PrintStream out) {
        this.out = out;
    }

    public void print(Path file, RecordStorage.Format format, String sourceFilter) throws IOException {
        if (!Files.exists(file)) {
            out.println("Файл не найден: " + file);
            return;
        }
        if (format == RecordStorage.Format.JSON) {
            printJson(file, sourceFilter);
        } else {
            printCsv(file, sourceFilter);
        }
    }

    private void printJson(Path file, String sourceFilter) throws IOException {
        JsonNode root = mapper.readTree(file.toFile());
        if (!root.isArray()) {
            out.println(root.toPrettyString());
            return;
        }
        for (JsonNode node : root) {
            if (sourceFilter != null && !sourceFilter.equals(node.path("source").asText())) {
                continue;
            }
            out.println(node.toPrettyString());
        }
    }

    private void printCsv(Path file, String sourceFilter) throws IOException {
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) return;
        List<String> headers = RecordStorage.parseCsvLine(lines.get(0));
        int sourceIndex = headers.indexOf("source");
        out.println(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isEmpty()) continue;
            if (sourceFilter != null) {
                List<String> cells = RecordStorage.parseCsvLine(line);
                if (sourceIndex < 0 || sourceIndex >= cells.size()
                        || !sourceFilter.equals(cells.get(sourceIndex))) {
                    continue;
                }
            }
            out.println(line);
        }
    }
}