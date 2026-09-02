package main;

import supervisor.Supervisor;
import work.Work;

public class Main {
    public static void main(String[] args) {
        Work worker = new Work(1);
        Supervisor supervisor = new Supervisor(worker);

        System.out.println("Supervisor starting program...");
        worker.start();
        supervisor.start();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Main: Stopping program...");
        worker.kill();
        supervisor.interrupt();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Main: Program finished");
    }
}
