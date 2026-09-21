package aggregator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Cli {

    private final ApiRegistry registry;
    private final AggregatorService service;
    private final RecordStorage storage;
    private final FileViewer viewer;
    private final Scanner scanner;

    private List<String> selected = new ArrayList<>();
    private RecordStorage.Format format = RecordStorage.Format.JSON;
    private Path file = Path.of("out.json");
    private boolean append = false;
    private PollingManager polling;

    public Cli(ApiRegistry registry, AggregatorService service,
               RecordStorage storage, FileViewer viewer, Scanner scanner) {
        this.registry = registry;
        this.service = service;
        this.storage = storage;
        this.viewer = viewer;
        this.scanner = scanner;
    }

    public void run(String[] args) throws Exception {
        if (args.length > 0 && "--auto".equals(args[0])) {
            runAuto(args);
        } else {
            runInteractive();
        }
    }

    private void runAuto(String[] args) throws Exception {
        AutoArgs autoArgs = AutoArgs.parse(args);
        if (autoArgs.apis.isEmpty()) {
            System.out.println("Не указан список API (--apis)");
            return;
        }
        if (autoArgs.poll) {
            polling = new PollingManager(service, storage, autoArgs.file, autoArgs.format,
                    autoArgs.maxTasks, autoArgs.interval);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> polling.shutdown()));
            polling.start(autoArgs.apis);
            System.out.println("Опрос запущен. Ctrl+C для остановки.");
            new CountDownLatch(1).await();
        } else {
            List<DataRecord> records = service.collect(autoArgs.apis);
            storage.save(records, autoArgs.file, autoArgs.format, autoArgs.append);
            System.out.println("Сохранено записей: " + records.size() + " в " + autoArgs.file);
        }
    }

    private void runInteractive() throws Exception {
        while (true) {
            printMenu();
            String command = scanner.nextLine().trim();
            switch (command) {
                case "1" -> selected = selectApis();
                case "2" -> format = selectFormat();
                case "3" -> configureFile();
                case "4" -> fetchOnce();
                case "5" -> viewFile();
                case "6" -> startPolling();
                case "7" -> stopPolling();
                case "0" -> {
                    if (polling != null) polling.shutdown();
                    return;
                }
                default -> System.out.println("Неизвестная команда");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Выбранные API: " + (selected.isEmpty() ? "нет" : selected));
        System.out.println("Формат: " + format + ", файл: " + file + ", режим: " + (append ? "дозапись" : "новый"));
        System.out.println("Опрос: " + (polling != null && polling.isRunning() ? "идёт" : "остановлен"));
        System.out.println("1) Выбрать API");
        System.out.println("2) Выбрать формат");
        System.out.println("3) Выбрать файл и режим записи");
        System.out.println("4) Опросить один раз и сохранить");
        System.out.println("5) Показать содержимое файла");
        System.out.println("6) Запустить периодический опрос");
        System.out.println("7) Остановить опрос");
        System.out.println("0) Выход");
        System.out.print("> ");
    }

    private List<String> selectApis() {
        List<String> names = registry.names();
        for (int i = 0; i < names.size(); i++) {
            System.out.println((i + 1) + ") " + names.get(i));
        }
        System.out.print("Введите номера через запятую: ");
        String line = scanner.nextLine().trim();
        List<String> result = new ArrayList<>();
        for (String part : line.split(",")) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < names.size()) result.add(names.get(index));
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    private RecordStorage.Format selectFormat() {
        System.out.print("Формат (1 - JSON, 2 - CSV): ");
        String value = scanner.nextLine().trim();
        return "2".equals(value) ? RecordStorage.Format.CSV : RecordStorage.Format.JSON;
    }

    private void configureFile() {
        System.out.print("Путь к файлу: ");
        file = Path.of(scanner.nextLine().trim());
        System.out.print("Режим (1 - создать новый, 2 - дозаписать): ");
        append = "2".equals(scanner.nextLine().trim());
    }

    private void fetchOnce() throws Exception {
        if (selected.isEmpty()) {
            System.out.println("API не выбраны");
            return;
        }
        List<DataRecord> records = service.collect(selected);
        storage.save(records, file, format, append);
        append = true;
        System.out.println("Сохранено записей: " + records.size());
    }

    private void viewFile() throws Exception {
        System.out.print("Фильтр по API (Enter - все): ");
        String value = scanner.nextLine().trim();
        viewer.print(file, format, value.isEmpty() ? null : value);
    }

    private void startPolling() {
        if (selected.isEmpty()) {
            System.out.println("API не выбраны");
            return;
        }
        if (polling != null && polling.isRunning()) {
            System.out.println("Опрос уже идёт");
            return;
        }
        System.out.print("Максимум одновременных задач n: ");
        int maxTasks = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Интервал опроса t (сек): ");
        long interval = Long.parseLong(scanner.nextLine().trim());
        append = true;
        polling = new PollingManager(service, storage, file, format, maxTasks, interval);
        polling.start(selected);
        System.out.println("Опрос запущен");
    }

    private void stopPolling() {
        if (polling == null || !polling.isRunning()) {
            System.out.println("Опрос не запущен");
            return;
        }
        polling.shutdown();
        polling = null;
        System.out.println("Опрос остановлен");
    }
}