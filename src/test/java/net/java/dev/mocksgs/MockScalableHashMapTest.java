package net.java.dev.mocksgs;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.util.ScalableHashMap;
import com.sun.sgs.internal.InternalContext;
import com.sun.sgs.internal.ManagerLocator;

/**
 * Tests the {@code MockDataManager} class
 */
public class MockScalableHashMapTest {
    
    @Before
    public void init() {
    	MockSGS.init();
    }
    
    @After
    public void tearDown() {
        MockSGS.reset();
    }
    
    @Test
    public void testCreateScalableHashMap() {
        //manager.createReference(new ScalableHashMap<String, String>());
    	AppContext.getDataManager().createReference(new ScalableHashMap<String, String>());
    }
}
