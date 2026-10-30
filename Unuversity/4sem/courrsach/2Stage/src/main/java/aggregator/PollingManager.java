package aggregator;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class PollingManager {

    private final AggregatorService service;
    private final RecordStorage storage;
    private final Path file;
    private final RecordStorage.Format format;
    private final long intervalSeconds;
    private final Semaphore permits;
    private final ExecutorService pool;
    private final List<Future<?>> futures = new CopyOnWriteArrayList<>();
    private volatile boolean running;

    public PollingManager(AggregatorService service, RecordStorage storage, Path file,
                          RecordStorage.Format format, int maxTasks, long intervalSeconds) {
        this.service = service;
        this.storage = storage;
        this.file = file;
        this.format = format;
        this.intervalSeconds = intervalSeconds;
        this.permits = new Semaphore(maxTasks);
        this.pool = Executors.newCachedThreadPool();
    }

    public synchronized void start(List<String> apiNames) {
        if (running) return;
        running = true;
        for (String name : apiNames) {
            futures.add(pool.submit(() -> loop(name)));
        }
    }

    private void loop(String apiName) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                permits.acquire();
                try {
                    List<DataRecord> records = service.collectOne(apiName);
                    if (!records.isEmpty()) {
                        storage.save(records, file, format, true);
                    }
                } finally {
                    permits.release();
                }
                Thread.sleep(intervalSeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.println("Ошибка опроса " + apiName + ": " + e.getMessage());
            }
        }
    }

    public synchronized void stop() {
        running = false;
        for (Future<?> future : futures) future.cancel(true);
        futures.clear();
    }

    public synchronized void shutdown() {
        stop();
        pool.shutdownNow();
        try {
            pool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }
}