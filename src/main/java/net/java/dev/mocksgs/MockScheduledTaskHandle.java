package net.java.dev.mocksgs;

import com.sun.sgs.app.Task;

public class MockScheduledTaskHandle extends AbstractMockTaskHandle {

    public MockScheduledTaskHandle(final Task task, final long start, final long scheduleTime) {

        super(task, start, scheduleTime);
    }
}
