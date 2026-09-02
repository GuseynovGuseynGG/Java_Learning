package work;

import java.util.concurrent.ThreadLocalRandom;

public class StateChanger extends Thread {
    private Work work;

    public StateChanger(Work work, int index) {
        this.work = work;
        setDaemon(true);
        setName("changer-" + index);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && work.isRunning()) {
            try {
                Thread.sleep(100);
                Work.Status newStatus;
                if (work.isFirstStart()) {
                    newStatus = Work.Status.RUNNING;
                } else {
                    Work.Status[] statuses = {Work.Status.STOPPING, Work.Status.RUNNING, Work.Status.FATAL_ERROR};
                    newStatus = statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
                }
                work.setStatus(newStatus);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
