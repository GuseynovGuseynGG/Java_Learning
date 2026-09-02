package supervisor;

import work.Work;

public class Supervisor extends Thread {
    private Work worker;

    public Supervisor(Work worker) {
        this.worker = worker;
        setName("supervisor");
    }

    public void listen() {
        try {
            Work.Status state = worker.waitForStatusChange();
            
            if (state == null) {
                return;
            }

            System.out.println("Supervisor detected status: " + state);
            
            if (state == Work.Status.STOPPING) {
                System.out.println("Restarting program...");
                worker.start();
            } else if (state == Work.Status.FATAL_ERROR) {
                System.out.println("Fatal error detected, killing program...");
                worker.kill();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && worker.isRunning()) {
            listen();
        }
        System.out.println("Supervisor stopped monitoring");
    }
}
