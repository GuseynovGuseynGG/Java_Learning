package supervisor;

public class Main {
    public static void main(String[] args) {
        Supervisor supervisor = new Supervisor();
        supervisor.start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Main] Завершение работы супервизора");
    }
}
