package net.java.dev.mocksgs;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;

public class AbstractMockTaskHandle implements Comparable<AbstractMockTaskHandle> {

    private final Task task;
    private final ManagedReference<Task> taskRef;
    protected final long start;
    /**
     * Tick at which the task was scheduled.
     */
    private final long scheduleTime;

    /**
     * Keeps task by reference if it's a {@link ManagedObject}, to comply with the semantics of
     * the regular task manager.
     * 
     * @see TaskManager
     */
    public AbstractMockTaskHandle(final Task task, final long start, final long scheduleTime) {
        if (task instanceof ManagedObject) {
            this.taskRef = AppContext.getDataManager().createReference(task);
            this.task = null;
        } else {
            this.taskRef = null;
            this.task = task;
        }
        this.start = start;
        this.scheduleTime = scheduleTime;
    }

    public int compareTo(AbstractMockTaskHandle otherHandle) {
        return scheduleTime < otherHandle.scheduleTime ? -1 : (scheduleTime > otherHandle.scheduleTime ? 1 : 0);
    }

    public long getScheduleTime() {
        return scheduleTime;
    }

    public long getStart() {
        return start;
    }

    public Task getTask() {
        return task != null ? task : taskRef.get();
    }
}
