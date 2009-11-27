package net.java.dev.mocksgs;

import com.sun.sgs.app.Task;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.internal.InternalContext;

/**
 * MockSGS provides a testing framework that implements the SGS specification
 * for use within test cases.  It registers itself within the standard SGS app server
 * and provides it's own AppContext.  You must call MockSGS.init() before your 
 * tests run in order for it to hook into SGS.
 * 
 * 
 * The common usage pattern is to create a unit test for each task in our system.
 * You should setup any precursor data within MockSGS via the DataManager, then
 * run your task.  When your task finishes, MockSGS will validate any data you have
 * in the data manager for serialization problems.
 * 
 * Afterward you can inspect the contents of the TaskManager, DataManager, and
 * ChannelManager to make sure they contain the expected objects.
 * 
 * If you generate a valid test for all of your tasks you will be well on your way to 
 * a stable running system before you ever fire up your first SGS instance.
 * 
 * Future plans include a MockClient api so that you can instrument and test
 * your entire application within the confines of a fixed set of unit tests within a single
 * vm.  This can drastically reduce debugging time.
 */
public class MockSGS {
    
    public static void init() {
        InternalContext.setManagerLocator(new MockManagerLocator());
    }

    public static void reset() {
        InternalContext.setManagerLocator(null);
    }

    public static void run(final Task task) throws Exception {
        task.run();

        // go through the data manager and make sure all objects can serialize
        ((MockDataManager)AppContext.getDataManager()).serializeDataStore();
    }
}
