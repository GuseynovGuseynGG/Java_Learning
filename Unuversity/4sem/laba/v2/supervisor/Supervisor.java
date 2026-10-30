package supervisor;

final class Supervisor extends Thread {
    private AbstractProgram program;

    public Supervisor() {
        program = new AbstractProgram();
    }

    public void startApp() {
        System.out.println("[Supervisor] Запуск программы");
        program.start();
    }

    public void stopApp() {
        System.out.println("[Supervisor] Остановка программы");
        program.interrupt();
    }

    @Override
    public void run() {
        startApp();

        while (!isInterrupted()) {
            synchronized (program) {
                AbstractProgram.State current = program.getStateTY();
                System.out.println("[Supervisor] Текущее состояние: " + current);

                switch (current) {
                    case UNKNOWN:
                        System.out.println("[Supervisor] Программа готова к первому запуску");
                        break;

                    case STOPPING:
                        System.out.println("[Supervisor] Обнаружен STOPPING, перезапуск...");
                        stopApp();
                        try {
                            program.join(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                        startApp();
                        break;

                    case FATAL_ERROR:
                        System.out.println("[Supervisor] Обнаружен FATAL_ERROR, завершение работы.");
                        stopApp();
                        return;

                    case RUNNING:
                        break;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        Supervisor sup = new Supervisor();
        sup.start();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sup.interrupt();
    }
}
