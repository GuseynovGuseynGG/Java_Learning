package aggregator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutoArgs {

    public final List<String> apis;
    public final RecordStorage.Format format;
    public final Path file;
    public final boolean append;

    public AutoArgs(List<String> apis, RecordStorage.Format format, Path file, boolean append) {
        this.apis = apis;
        this.format = format;
        this.file = file;
        this.append = append;
    }

    public static AutoArgs parse(String[] args) {
        List<String> apis = new ArrayList<>();
        RecordStorage.Format format = RecordStorage.Format.JSON;
        Path file = Path.of("out.json");
        boolean append = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--apis":
                    if (i + 1 < args.length) apis = List.of(args[++i].split(","));
                    break;
                case "--format":
                    if (i + 1 < args.length) format = RecordStorage.Format.valueOf(args[++i].toUpperCase());
                    break;
                case "--file":
                    if (i + 1 < args.length) file = Path.of(args[++i]);
                    break;
                case "--append":
                    append = true;
                    break;
                default:
                    break;
            }
        }
        return new AutoArgs(apis, format, file, append);
    }
}