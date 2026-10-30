package supervisor;

import java.util.Random;

final class AbstractProgram extends Thread {
    public enum State {
        UNKNOWN,
        STOPPING,
        RUNNING,
        FATAL_ERROR
    }

    private State state = State.UNKNOWN;
    private boolean stateChanged = false;

    public AbstractProgram() {
    }

    public synchronized void setState(State newState) {
        this.state = newState;
        this.stateChanged = true;
        notify();
    }

    public synchronized State getStateTY() {
        while (!stateChanged) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return state;
            }
        }
        stateChanged = false;
        return state;
    }

    @Override
    public void run() {
        Thread daemon = new Thread(() -> {
            Random random = new Random();
            State[] runtimeStates = {State.STOPPING, State.RUNNING, State.FATAL_ERROR};

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);
                    State newState = runtimeStates[random.nextInt(runtimeStates.length)];
                    setState(newState);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        daemon.setDaemon(true);
        daemon.start();

    }
}
