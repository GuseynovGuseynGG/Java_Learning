package aggregator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        ApiRegistry registry = new ApiRegistry();
        RecordStorage storage = new RecordStorage();
        AggregatorService service = new AggregatorService(registry);
        FileViewer viewer = new FileViewer();
        Cli cli = new Cli(registry, service, storage, viewer, new Scanner(System.in));
        cli.run(args);
    }
}