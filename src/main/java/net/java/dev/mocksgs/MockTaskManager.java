package net.java.dev.mocksgs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.ExceptionRetryStatus;
import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;

/**
 * Mock implementation of the {@link TaskManager}.
 * 
 * This implementation does not execute tasks by default. 
 * Call {@link #executeNextTaskTick()} to 
 * advance time to the next scheduled task and execute it, 
 * or call {@link #executeCurrentTick()} to execute
 * tasks scheduled at or before the current tick. 
 * 
 * Time is internal, and advances only as tasks are executed, 
 * or by explicitly calling
 * {@link #setMockTimeMillis(long)}.
 * 
 * This way, you can let time run as fast (or as slow) as desired in your tests.
 *  
 * @author j0rg3n
 */
public class MockTaskManager implements TaskManager {

    private final static Logger logger = 
            Logger.getLogger(MockTaskManager.class.getName());
    /**
     * Priority queue containing scheduled tasks, in order of execution.
     */
    private PriorityQueue<AbstractMockTaskHandle> scheduledTaskHandles = 
            new PriorityQueue<AbstractMockTaskHandle>();
    /**
     * The current wall-clock time to the task manager.
     */
    private long mockTime = 0L;

    @Override
    public PeriodicTaskHandle schedulePeriodicTask(final Task task, 
                                                   final long delay, 
                                                   final long period) {
        checkArgument(task);
        checkPositive("Delay", delay);
        checkPositive("Period", period);

        MockPeriodicTaskHandle handle =
                new MockPeriodicTaskHandle(task, delay, period, 
                                           getMockTimeMillis() + delay);

        scheduledTaskHandles.add(handle);
        return handle;
    }

    @Override
    public void scheduleTask(final Task task, final long delay) {
        checkArgument(task);
        checkPositive("Delay", delay);

        scheduledTaskHandles.add(
                new MockScheduledTaskHandle(task, delay, 
                                            getMockTimeMillis() + delay));
    }

    @Override
    public void scheduleTask(final Task task) {
        scheduleTask(task, 0);
    }

    public List<PeriodicTaskHandle> getPeriodicTaskHandles() {
        List<PeriodicTaskHandle> list = new LinkedList<PeriodicTaskHandle>();
        for (AbstractMockTaskHandle handle : scheduledTaskHandles) {
            if (handle instanceof PeriodicTaskHandle) {
                list.add((PeriodicTaskHandle) handle);
            }
        }
        return list;
    }

    public List<MockScheduledTaskHandle> getScheduledTaskHandles() {
        List<MockScheduledTaskHandle> list = new LinkedList<MockScheduledTaskHandle>();
        for (AbstractMockTaskHandle handle : scheduledTaskHandles) {
            if (handle instanceof MockScheduledTaskHandle) {
                list.add((MockScheduledTaskHandle) handle);
            }
        }
        return list;
    }

    public void reset() {
        scheduledTaskHandles.clear();
    }

    public int getTotalTaskCount() {
        return scheduledTaskHandles.size();
    }

    public boolean isTaskQueueEmpty() {
        return scheduledTaskHandles.isEmpty();
    }

    /**
     * @return Time of next scheduled task, or -1L if the task queue is empty.
     */
    public long getNextTaskScheduleTime() {

        return !scheduledTaskHandles.isEmpty() ? scheduledTaskHandles.peek().getScheduleTime() : -1L;
    }

    /**
     * Advances time until the first tick containing tasks, then executes the first of those tasks.
     * 
     * Note: If the first task is scheduled in the past, the time will *not* move backwards.
     * 
     * Note: This means that time stands still until there are tasks scheduled in the future.
     */
    public void executeNextTaskTick() {
        long nextTaskScheduleTime = getNextTaskScheduleTime();
        if (nextTaskScheduleTime != -1L) {
            // Compensate for tasks scheduled in the past (for whatever reason)
            mockTime = Math.max(nextTaskScheduleTime, mockTime);
            executeCurrentTick();
        }
    }

    /**
     * Execute a single task scheduled at (or before!) the current tick.
     * 
     * The current tick may be retrieved or set by using {@link #setMockTimeMillis(long)} and
     * {@link #getMockTimeMillis()}.
     */
    public void executeCurrentTick() {
        AbstractMockTaskHandle nextTask = scheduledTaskHandles.peek();
        if (nextTask != null && nextTask.getScheduleTime() <= mockTime) {
            scheduledTaskHandles.poll();
            try {
                // Execute task
                nextTask.getTask().run();

                // Reschedule periodic tasks
                if (nextTask instanceof MockPeriodicTaskHandle) {
                    long repeat = ((MockPeriodicTaskHandle) nextTask).getRepeat();
                    scheduledTaskHandles.add(new MockPeriodicTaskHandle(nextTask.getTask(),
                                                                        nextTask.getStart(), repeat, mockTime + repeat));
                }
            } catch (Exception e) {
                if (e instanceof ExceptionRetryStatus) {
                    if (((ExceptionRetryStatus) e).shouldRetry()) {
                        logger.log(Level.SEVERE, "Task requests retry, rescheduling it.", e);

                        // Reschedule task immediately, regardless of task period.
                        // FIXME Schedule for next "tick slice".
                        scheduledTaskHandles.add(nextTask);
                    } else {
                        logger.log(Level.SEVERE, "Task does not want to be retried, not rescheduling.", e);
                    }
                } else {
                    logger.log(Level.SEVERE, "Task failed with a non-retryable exception, not rescheduling.", e);
                }
            }
        }
    }

    /**
     * Gets the internal tick. 
     * 
     * Warning: May be the system time, or fast-forward fake time, depending on the
     * task manager configuration.
     */
    public long getMockTimeMillis() {
        return mockTime;
    }

    /**
     * Sets the internal fast-forward fake time. 
     * 
     * Warning: Making time go backwards while tasks are scheduled may have undefined results.
     */
    public void setMockTimeMillis(long time) {
        this.mockTime = time;
    }
    
    
    /**
     * Verify that the task implements Serializable
     * @param task
     */
    private void checkArgument(Task task) {
        if (task == null) {
            throw new NullPointerException("The task must not be null");
        }
        if (!(task instanceof Serializable)) {
            throw new IllegalArgumentException(
                    "Task is not serializable: " + task);
        }
    }
    
    /**
     * Verify that the number is greater than or equal to 0
     * @param name the name of the variable
     * @param num the value of the variable
     */
    private void checkPositive(String name, long num) {
        if(num < 0) {
            throw new IllegalArgumentException(name +
                                               " is less than zero: " + num);
        }
    }
}


