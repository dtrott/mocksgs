package net.java.dev.mocksgs;

import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;

public class MockPeriodicTaskHandle extends AbstractMockTaskHandle implements PeriodicTaskHandle {

    private final long repeat;
    /**
     * <code>true</code> if {@link #cancel()} has been called.
     */
    private boolean cancelled = false;

    public MockPeriodicTaskHandle(final Task task, final long start, final long repeat, final long scheduleTime) {
        super(task, start, scheduleTime);

        this.repeat = repeat;
    }

    public void cancel() {
        cancelled = true;
    }

    public long getRepeat() {
        return repeat;
    }

    /**
     * @return <code>true</code> if {@link #cancel()} has been called.
     */
    public boolean isCancelled() {
        return cancelled;
    }
}
