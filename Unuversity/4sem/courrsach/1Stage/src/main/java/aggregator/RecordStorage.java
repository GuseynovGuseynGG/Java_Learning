package aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class RecordStorage {

    public enum Format { JSON, CSV }

    private static final List<String> FIXED = List.of("id", "source", "timestamp");

    private final ObjectMapper mapper = new ObjectMapper();
    private final Object lock = new Object();

    public void save(List<DataRecord> records, Path file, Format format, boolean append) throws IOException {
        synchronized (lock) {
            if (format == Format.JSON) {
                saveJson(records, file, append);
            } else {
                saveCsv(records, file, append);
            }
        }
    }

    public List<DataRecord> readJson(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        JsonNode root = mapper.readTree(file.toFile());
        List<DataRecord> result = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                result.add(mapper.treeToValue(node, DataRecord.class));
            }
        }
        return result;
    }

    private void saveJson(List<DataRecord> records, Path file, boolean append) throws IOException {
        List<DataRecord> all = append ? readJson(file) : new ArrayList<>();
        all.addAll(records);
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), all);
    }

    private void saveCsv(List<DataRecord> records, Path file, boolean append) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        if (append && Files.exists(file)) {
            rows.addAll(readCsvRows(file));
        }
        for (DataRecord record : records) {
            rows.add(toRow(record));
        }
        LinkedHashSet<String> headers = new LinkedHashSet<>(FIXED);
        for (Map<String, String> row : rows) {
            headers.addAll(row.keySet());
        }
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            List<String> headerCells = new ArrayList<>();
            for (String h : headers) {
                headerCells.add(escape(h));
            }
            writer.write(String.join(",", headerCells));
            writer.newLine();
            for (Map<String, String> row : rows) {
                List<String> cells = new ArrayList<>();
                for (String header : headers) {
                    cells.add(escape(row.get(header)));
                }
                writer.write(String.join(",", cells));
                writer.newLine();
            }
        }
    }

    public static Map<String, String> toRow(DataRecord record) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(record.getId()));
        row.put("source", record.getSource());
        row.put("timestamp", record.getTimestamp());
        flatten(record.getData(), "data", row);
        return row;
    }

    public static void flatten(JsonNode node, String prefix, Map<String, String> out) {
        if (node == null || node.isNull()) return;
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flatten(entry.getValue(), key, out);
            }
        } else if (node.isArray()) {
            out.put(prefix, node.toString());
        } else {
            out.put(prefix, node.asText());
        }
    }

    public static List<Map<String, String>> readCsvRows(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<Map<String, String>> rows = new ArrayList<>();
        if (lines.isEmpty()) return rows;
        List<String> headers = parseCsvLine(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isEmpty()) continue;
            List<String> cells = parseCsvLine(line);
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                row.put(headers.get(j), j < cells.size() ? cells.get(j) : "");
            }
            rows.add(row);
        }
        return rows;
    }

    public static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    public static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}