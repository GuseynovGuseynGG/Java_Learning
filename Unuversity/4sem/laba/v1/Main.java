public class Main {
    public static void main(String[] args) {
        Work worker = new Work();
        Supervisor supervisor = new Supervisor(worker);

        System.out.println("Supervisor starting program...");
        supervisor.startProgram();
        supervisor.start();

        try {
            supervisor.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Main: Program finished");
    }
}