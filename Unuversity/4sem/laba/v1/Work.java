public class Work implements Runnable {
    public enum Status {
        UNKNOWN, STOPPING, RUNNING, FATAL_ERROR
    }

    private Status currentStatus = Status.UNKNOWN;
    private boolean statusConsumed = true;

    public synchronized Status getStatus() {
        return currentStatus;
    }

    public synchronized void setStatus(Status newStatus) {
        if (currentStatus == Status.FATAL_ERROR) {
            return;
        }
        System.out.println("State: " + currentStatus + " -> " + newStatus);
        currentStatus = newStatus;
        statusConsumed = false;
        notifyAll();
        while (!statusConsumed) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public synchronized Status waitForStatusChange() throws InterruptedException {
        while (statusConsumed) {
            wait();
        }
        statusConsumed = true;
        notifyAll();
        return currentStatus;
    }

    @Override
    public void run() {
        synchronized (this) {
            currentStatus = Status.RUNNING;
            System.out.println("Program started, status: " + currentStatus);
            statusConsumed = false;
            notifyAll();
            while (!statusConsumed) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        StateChanger changer = new StateChanger(this);
        changer.setDaemon(true);
        changer.start();

        synchronized (this) {
            while (currentStatus != Status.STOPPING && currentStatus != Status.FATAL_ERROR) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
        System.out.println("Program terminated, status: " + currentStatus);
    }
}