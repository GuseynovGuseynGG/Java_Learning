import java.util.concurrent.ThreadLocalRandom;

public class StateChanger extends Thread {
    private final Work work;

    public StateChanger(Work work) {
        this.work = work;
        setDaemon(true);
        setName("changer");
    }

@Override
public void run() {
    while (true) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            return;
        }

            Work.Status current = work.getStatus();
            if (current == Work.Status.FATAL_ERROR || current == Work.Status.STOPPING) {
                return;
            }

            int value = ThreadLocalRandom.current().nextInt(100);
            Work.Status newStatus;
            if (value < 60) {
                newStatus = Work.Status.STOPPING;
            } else if (value < 90) {
                newStatus = Work.Status.RUNNING;
            } else {
                newStatus = Work.Status.FATAL_ERROR;
            }
            work.setStatus(newStatus);
        }
    }