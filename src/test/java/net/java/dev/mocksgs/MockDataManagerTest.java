package net.java.dev.mocksgs;

import com.sun.sgs.internal.InternalContext;
import com.sun.sgs.internal.ManagerLocator;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedObjectRemoval;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ObjectNotFoundException;
import java.io.Serializable;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import org.easymock.EasyMock;

/**
 * Tests the {@code MockDataManager} class
 */
public class MockDataManagerTest {
    
    private MockDataManager manager;
    
    @Before
    public void setupManager() {
        manager = new MockDataManager();
        ManagerLocator locator = EasyMock.createMock(ManagerLocator.class);
        EasyMock.expect(locator.getDataManager()).andReturn(manager);
        EasyMock.replay(locator);
        InternalContext.setManagerLocator(locator);
    }
    
    @After
    public void tearDown() {
        manager = null;
        InternalContext.setManagerLocator(null);
    }
    
    @Test(expected=NullPointerException.class)
    public void testCreateReferenceNullObject() {
        manager.createReference(null);
    }
    
    @Test(expected=IllegalArgumentException.class) 
    public void testCreateReferenceNotManagedObject() {
        manager.createReference(new Serializable () {});
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateReferenceNotSerializable() {
        manager.createReference(new ManagedObject() {});
    }
    
    @Test
    public void testCreateReferenceValidObject() {
        TestObject testObject = new TestObject();
        ManagedReference<TestObject> test = manager.createReference(testObject);
        
        Assert.assertEquals(testObject, test.get());
    }
    
    @Test(expected=ObjectNotFoundException.class)
    public void testCreateReferenceRemoved() {
        TestObject o = new TestObject();
        manager.createReference(o);
        manager.removeObject(o);
        manager.createReference(o);
    }
    
    @Test(expected=NullPointerException.class)
    public void testGetBindingNullName() {
        manager.getBinding(null);
    }
    
    @Test(expected=NameNotBoundException.class)
    public void testGetBindingUnboundName() {
        manager.getBinding("unbound");
    }
    
    @Test
    public void testGetBindingBoundName() {
        TestObject testObject = new TestObject();
        manager.setBinding("bound", testObject);
        
        ManagedObject o = manager.getBinding("bound");
        Assert.assertEquals(testObject, o);
    }
    
    @Test(expected=ObjectNotFoundException.class)
    public void testGetBindingMissingObject() {
        TestObject testObject = new TestObject();
        manager.setBinding("bound", testObject);
        manager.removeObject(testObject);
        
        manager.getBinding("bound");
    }
    
    @Test(expected=NullPointerException.class)
    public void testMarkForUpdateNullObject() {
        manager.createReference(null);
    }
    
    @Test(expected=IllegalArgumentException.class) 
    public void testMarkForUpdateNotManagedObject() {
        manager.createReference(new Serializable () {});
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMarkForUpdateNotSerializable() {
        manager.createReference(new ManagedObject() {});
    }
    
    @Test(expected=ObjectNotFoundException.class)
    public void testMarkForUpdateNotFound() {
        TestObject o = new TestObject();
        manager.createReference(o);
        manager.removeObject(o);
        
        manager.markForUpdate(o);
    }
    
    @Test
    public void testMarkForUpdateNeverAdded() {
        manager.markForUpdate(new TestObject());
    }
    
    @Test
    public void testMarkForUpdateValid() {
        TestObject testObject = new TestObject();
        manager.createReference(testObject);
        manager.markForUpdate(testObject);
    }
    
    @Test
    public void testNextBoundNameNull() {
        TestObject o1 = new TestObject();
        TestObject o2 = new TestObject();
        TestObject o3 = new TestObject();
        
        manager.setBinding("name1", o1);
        manager.setBinding("name2", o2);
        manager.setBinding("name3", o3);
        
        String n = manager.nextBoundName(null);
        Assert.assertEquals("name1", n);
    }
    
    @Test
    public void testNextBoundNameWalk() {
        TestObject o1 = new TestObject();
        TestObject o2 = new TestObject();
        TestObject o3 = new TestObject();
        
        manager.setBinding("name1", o1);
        manager.setBinding("name2", o2);
        manager.setBinding("name3", o3);
        
        String n1 = manager.nextBoundName(null);
        String n2 = manager.nextBoundName(n1);
        String n3 = manager.nextBoundName(n2);
        String n4 = manager.nextBoundName(n3);
        Assert.assertEquals("name1", n1);
        Assert.assertEquals("name2", n2);
        Assert.assertEquals("name3", n3);
        Assert.assertNull(n4);
    }
    
    @Test
    public void testNextBoundNameMiddle() {
        TestObject o1 = new TestObject();
        TestObject o5 = new TestObject();
        TestObject o9 = new TestObject();
        
        manager.setBinding("name1", o1);
        manager.setBinding("name5", o5);
        manager.setBinding("name9", o9);
        
        String n5 = manager.nextBoundName("name3");
        Assert.assertEquals("name5", n5);
    }
    
    @Test(expected=NullPointerException.class)
    public void removeBindingNull() {
        manager.removeBinding(null);
    }
    
    @Test(expected=NameNotBoundException.class)
    public void removeBindingNotBound() {
        manager.removeBinding("unbound");
    }
    
    @Test
    public void removeBindingValid() {
        TestObject o = new TestObject();
        manager.setBinding("name", o);
        ManagedObject o1 = manager.getBinding("name");
        Assert.assertEquals(o1, o);
        
        manager.removeBinding("name");
        try {
            manager.getBinding("name");
            Assert.fail("Binding should have been removed");
        } catch(NameNotBoundException n) {
            
        }
    }
    
    @Test(expected=NullPointerException.class)
    public void testRemoveObjectNull() {
        manager.removeObject(null);
    }
    
    @Test(expected=IllegalArgumentException.class) 
    public void testRemoveObjectNotManagedObject() {
        manager.removeObject(new Serializable () {});
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testRemoveObjectNotSerializable() {
        manager.removeObject(new ManagedObject() {});
    }
    
    @Test
    public void testRemoveObjectNeverAdded() {
        manager.removeObject(new TestObject());
    }
    
    @Test(expected=ObjectNotFoundException.class)
    public void testRemoveObjectNotFound() {
        TestObject o = new TestObject();
        manager.createReference(o);
        
        manager.removeObject(o);
        manager.removeObject(o);
    }
    
    @Test
    public void testRemoveObjectValid() {
        TestObject o = new TestObject();
        manager.createReference(o);
        
        manager.removeObject(o);
    }
    
    @Test
    public void testRemoveObjectManagedObjectRemoval() {
        TestObjectRemoval o = new TestObjectRemoval();
        manager.createReference(o);
        manager.removeObject(o);
        
        Assert.assertTrue(o.called());
    }
    
    @Test(expected=NullPointerException.class)
    public void testSetBindingNullObject() {
        manager.setBinding("name", null);
    }
    
    @Test(expected=NullPointerException.class)
    public void testSetBindingNullName() {
        manager.setBinding(null, new TestObject());
    }
    
    @Test(expected=IllegalArgumentException.class) 
    public void testSetBindingNotManagedObject() {
        manager.setBinding("name", new Serializable () {});
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetBindingNotSerializable() {
        manager.setBinding("name", new ManagedObject() {});
    }
    
    @Test(expected=ObjectNotFoundException.class)
    public void testSetBindingRemoved() {
        TestObject o = new TestObject();
        manager.createReference(o);
        manager.removeObject(o);
        manager.setBinding("name", o);
    }
    
    @Test
    public void testSetBinding() {
        TestObject o = new TestObject();
        manager.setBinding("name", o);
        
        ManagedObject o1 = manager.getBinding("name");
        Assert.assertEquals(o, o1);
    }
    
    private class TestObject implements Serializable, ManagedObject {
        
    }
    
    private class TestObjectRemoval implements Serializable,
                                             ManagedObject,
                                             ManagedObjectRemoval {
        private boolean called = false;
        
        @Override
        public void removingObject() {
            called = true;
        }
        
        public boolean called() {
            return called;
        }
        
    }

}
