package aggregator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutoArgs {

    public final List<String> apis;
    public final RecordStorage.Format format;
    public final Path file;
    public final boolean append;
    public final boolean poll;
    public final int maxTasks;
    public final long interval;

    public AutoArgs(List<String> apis, RecordStorage.Format format, Path file, boolean append,
                    boolean poll, int maxTasks, long interval) {
        this.apis = apis;
        this.format = format;
        this.file = file;
        this.append = append;
        this.poll = poll;
        this.maxTasks = maxTasks;
        this.interval = interval;
    }

    public static AutoArgs parse(String[] args) {
        List<String> apis = new ArrayList<>();
        RecordStorage.Format format = RecordStorage.Format.JSON;
        Path file = Path.of("out.json");
        boolean append = false;
        boolean poll = false;
        int maxTasks = 3;
        long interval = 5;

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
                case "--poll":
                    poll = true;
                    break;
                case "--n":
                    if (i + 1 < args.length) maxTasks = Integer.parseInt(args[++i]);
                    break;
                case "--t":
                    if (i + 1 < args.length) interval = Long.parseLong(args[++i]);
                    break;
                default:
                    break;
            }
        }
        return new AutoArgs(apis, format, file, append, poll, maxTasks, interval);
    }
}