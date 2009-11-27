package net.java.dev.mocksgs;

import com.sun.sgs.internal.ManagerLocator;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.TaskManager;
import java.util.HashMap;
import java.util.Map;

public class MockManagerLocator implements ManagerLocator {

    private DataManager dataManager = new MockDataManager();
    private ChannelManager channelManager = new MockChannelManager();
    private TaskManager taskManager = new MockTaskManager();
    private Map<Class, Object> managers = new HashMap<Class, Object>();

    @Override
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public TaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public <T> T getManager(final Class<T> clazz) {
        for (Class key : managers.keySet()) {
            if (clazz.isAssignableFrom(key)) {
                return (T) managers.get(key);
            }
        }
        throw new IllegalArgumentException("MockManager not registered : " +
                                           clazz.getCanonicalName());
    }

    public void addMockManager(final Object manager) {
        managers.put(manager.getClass(), manager);
    }
    public void setChannelManager(final ChannelManager channelManager) {
        this.channelManager = channelManager;
    }
    public void setDataManager(final DataManager dataManager) {
        this.dataManager = dataManager;
    }
    public void setTaskManager(final TaskManager taskManager) {
        this.taskManager = taskManager;
    }
}
