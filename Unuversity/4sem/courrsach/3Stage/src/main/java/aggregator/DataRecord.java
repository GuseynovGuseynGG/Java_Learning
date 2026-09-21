package aggregator;

import com.fasterxml.jackson.databind.JsonNode;

public class DataRecord {

    private long id;
    private String source;
    private String timestamp;
    private JsonNode data;

    public DataRecord() {
    }

    public DataRecord(long id, String source, String timestamp, JsonNode data) {
        this.id = id;
        this.source = source;
        this.timestamp = timestamp;
        this.data = data;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public JsonNode getData() { return data; }
    public void setData(JsonNode data) { this.data = data; }
}