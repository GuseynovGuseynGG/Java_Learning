package work;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class Work {
    public enum Status {
        UNKNOWN, STOPPING, RUNNING, FATAL_ERROR
    }

    private volatile Status currentStatus;
    private volatile boolean running;
    private Thread programThread;
    private final int index;
    private final Queue<Status> statusQueue = new LinkedList<>();
    private final Object lock = new Object();
    private volatile boolean firstStart = true;
    private StateChanger changer;

    public Work(int index) {
        this.index = index;
        this.currentStatus = Status.UNKNOWN;
        this.running = false;
        System.out.println("Program " + index + " created, status: " + currentStatus);
    }

    public synchronized Status getStatus() {
        return currentStatus;
    }

    public void setStatus(Status newStatus) {
        synchronized (lock) {
            if (currentStatus == Status.FATAL_ERROR && newStatus != Status.FATAL_ERROR) {
                return;
            }
            
            System.out.println("State: " + currentStatus + " -> " + newStatus);
            currentStatus = newStatus;
            statusQueue.add(newStatus);
            lock.notifyAll();
        }
    }

    public Status waitForStatusChange() throws InterruptedException {
        synchronized (lock) {
            while (statusQueue.isEmpty() && running) {
                lock.wait();
            }
            if (!statusQueue.isEmpty()) {
                return statusQueue.poll();
            }
            return null;
        }
    }

    public void start() {
        if (currentStatus == Status.FATAL_ERROR) {
            System.out.println("Cannot start program " + index + " - fatal error occurred");
            return;
        }
        
        if (running) {
            System.out.println("Program " + index + " is already running");
            return;
        }

        running = true;
        programThread = new Thread(() -> {
            System.out.println("Program " + index + " started");
            if (firstStart) {
                firstStart = false;
                changer = new StateChanger(this, index);
                changer.start();
            }
            setStatus(Status.RUNNING);
            
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (!running) {
                System.out.println("Program " + index + " terminated");
            }
        }, "program-" + index);
        
        programThread.start();
    }

    public void stop() {
        if (!running) {
            return;
        }
        setStatus(Status.STOPPING);
        running = false;
        if (programThread != null && programThread.isAlive()) {
            programThread.interrupt();
        }
    }

    public void kill() {
        setStatus(Status.FATAL_ERROR);
        running = false;
        if (programThread != null && programThread.isAlive()) {
            programThread.interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }
    
    public boolean isFirstStart() {
        return firstStart;
    }
}
