public class Supervisor extends Thread {
    private final Work worker;
    private Thread programThread;

    public Supervisor(Work worker) {
        this.worker = worker;
        setName("supervisor");
    }

    public void startProgram() {
        if (programThread != null && programThread.isAlive()) {
            return;
        }
        programThread = new Thread(worker, "program");
        programThread.start();
    }

    public void stopProgram() {
        worker.setStatus(Work.Status.STOPPING);
        if (programThread != null) {
            programThread.interrupt();
        }
    }

    public void killProgram() {
        worker.setStatus(Work.Status.FATAL_ERROR);
        if (programThread != null) {
            programThread.interrupt();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Work.Status state = worker.waitForStatusChange();
                System.out.println("Supervisor detected status: " + state);
                if (state == Work.Status.STOPPING) {
                    System.out.println("Restarting program...");
                    if (programThread != null && programThread.isAlive()) {
                        programThread.join();
                    }
                    startProgram();
                } else if (state == Work.Status.FATAL_ERROR) {
                    System.out.println("Fatal error detected, killing program...");
                    killProgram();
                    return;
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}