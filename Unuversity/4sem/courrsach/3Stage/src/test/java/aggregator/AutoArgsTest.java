package aggregator;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoArgsTest {

    @Test
    void defaults() {
        AutoArgs args = AutoArgs.parse(new String[]{"--auto"});
        assertTrue(args.apis.isEmpty());
        assertEquals(RecordStorage.Format.JSON, args.format);
        assertEquals(Path.of("out.json"), args.file);
        assertFalse(args.append);
        assertFalse(args.poll);
        assertEquals(3, args.maxTasks);
        assertEquals(5, args.interval);
    }

    @Test
    void parsesAllOptions() {
        AutoArgs args = AutoArgs.parse(
                "--auto --apis a,b,c --format csv --file data.csv --append --poll --n 4 --t 2".split(" "));
        assertEquals(List.of("a", "b", "c"), args.apis);
        assertEquals(RecordStorage.Format.CSV, args.format);
        assertEquals(Path.of("data.csv"), args.file);
        assertTrue(args.append);
        assertTrue(args.poll);
        assertEquals(4, args.maxTasks);
        assertEquals(2, args.interval);
    }

    @Test
    void ignoresUnknownAndMissingValues() {
        AutoArgs args = AutoArgs.parse(new String[]{"--auto", "--unknown", "--format"});
        assertEquals(RecordStorage.Format.JSON, args.format);
    }
}