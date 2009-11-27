package net.java.dev.mocksgs;

import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;

import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedObjectRemoval;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ObjectNotFoundException;

/**
 * Mock's darkstar's DataManager interface so that it behaves as
 * expected without the backing datastore. <p>
 *
 * When writing tests using this Manager, a transaction boundary can
 * be simulated by calling the {@link MockDataManager#serializeDataStore}
 * method.  This will serialize each object in the store, and then
 * deserialize them, getting a fresh, separate copy of each object.
 */
public class MockDataManager implements DataManager {

    /**
     * This is the master counter used to assign new ids to
     * every {@code ManagedReference} that is created.
     */
    private static BigInteger masterId = BigInteger.ZERO;
    /**
     * This is the main representation of the Data Store as a map
     * of ids to {@code ManagedObject}s.
     */
    private Map<BigInteger, ManagedObject> store =
            new HashMap<BigInteger, ManagedObject>();
    /**
     * This maps maintains the name bindings from names to object ids.
     */
    private Map<String, BigInteger> bindings =
            new HashMap<String, BigInteger>();
    /**
     * Maps each ManagedObject to its associated id.
     */
    private Map<ManagedObject, BigInteger> idMap =
            new IdentityHashMap<ManagedObject, BigInteger>();
    /**
     * Tracks ManagedObjects that have been removed from the data store
     */
    private Map<ManagedObject, BigInteger> removedMap =
            new IdentityHashMap<ManagedObject, BigInteger>();
    /**
     * List of references created during this transaction
     */
    private List<MockManagedReference> referenceList =
            new ArrayList<MockManagedReference>();

    @Override
    public <T> ManagedReference<T> createReference(T object) {
        checkArgument(object);
        checkRemoved(object);
        ManagedObject o = (ManagedObject) object;
        BigInteger id = addToDataStore(o);
        MockManagedReference<T> m = new MockManagedReference<T>(id);
        referenceList.add(m);
        return m;
    }

    @Override
    public ManagedObject getBinding(String name) {
        checkNull(name);
        BigInteger id = bindings.get(name);
        if (id == null) {
            throw new NameNotBoundException(
                    "No binding for " + name + " in the data store");
        }

        return getObjectWithId(id);
    }

    @Override
    public ManagedObject getBindingForUpdate(String name) {
        return getBinding(name);
    }

    @Override
    public void markForUpdate(Object object) {
        checkArgument(object);

        if (!idMap.containsKey(object)) {
            checkRemoved(object);
        }
    }

    @Override
    public BigInteger getObjectId(Object object) {
        checkArgument(object);

        if (idMap.containsKey(object)) {
            return idMap.get(object);
        }

        return createReference(object).getId();
    }

    @Override
    public String nextBoundName(String name) {
        List<String> names = new ArrayList<String>(bindings.keySet());
        Collections.sort(names);

        if(name == null) {
            if(names.size() > 0) {
                return names.get(0);
            }
        } else {
            for (Iterator<String> in = names.iterator(); in.hasNext();) {
                String next = in.next();
                if (name.compareTo(next) < 0) {
                    return next;
                }
            }
        }
        return null;
    }

    @Override
    public void removeBinding(String name) {
        checkNull(name);
        if (!bindings.containsKey(name)) {
            throw new NameNotBoundException(
                    "No binding for " + name + " in the data store");
        }
        bindings.remove(name);
    }

    @Override
    public void removeObject(Object object) {
        checkArgument(object);

        if (!idMap.containsKey(object)) {
            checkRemoved(object);
        }

        if (object instanceof ManagedObjectRemoval) {
            ((ManagedObjectRemoval) object).removingObject();
        }
        BigInteger id = idMap.remove(object);
        store.remove(id);
        removedMap.put((ManagedObject)object, id);
    }

    @Override
    public void setBinding(String name, Object object) {
        checkNull(name);
        checkArgument(object);
        checkRemoved(object);
        BigInteger id = addToDataStore((ManagedObject) object);
        bindings.put(name, id);
    }

    /**
     * Retrieves the complete set of {@code ManagedObject}s in the data store.
     *
     * @return complete set of items in the data store
     */
    public Set<ManagedObject> getAllData() {
        return idMap.keySet();
    }

    /**
     * Retrieves a mapping of names to {@code ManagedObject}s for all name
     * bindings in the data store.  Note that the result of this method
     * represents a snapshot of the data, and becomes invalid if the
     * data store is modified in any way.
     *
     * @return map of names bound to objects in the data store
     */
    public Map<String, ManagedObject> getBoundData() {
        Map<String, ManagedObject> data = new HashMap<String, ManagedObject>();
        for(String name : bindings.keySet()) {
            data.put(name, store.get(bindings.get(name)));
        }

        return data;
    }


    /**
     * Retrieves the object with the specified id from the data store
     *
     * @param id the id of the object
     * @return the {@code ManagedObject} that is associated with this id
     * @throws ObjectNotFoundException if no object with the given id
     *         exists in the data store
     */
    public ManagedObject getObjectWithId(BigInteger id) {
        if (!store.containsKey(id)) {
            throw new ObjectNotFoundException(
                    "No object found in the data store with id : " + id);
        }

        return store.get(id);
    }

    /**
     * Returns the total number of objects in the data store
     *
     * @return total number of objects in the data store
     */
    public int size() {
        return store.size();
    }

    /**
     * Serialize and de-serialize each of the objects in the data store
     * to simulate the start of a new transaction.  <p>
     *
     * This method will
     * also have the added side effect of deactivating any
     * {@code ManagedReference} objects that have been created through
     * this data store since the last call to this method.  <p>
     *
     * A deactivated
     * {@code ManagedReference} will always throw a
     * {@code TransactionNotActiveException} if an attempt is made to
     * get its associated {@code ManagedObject}.
     */
    public void serializeDataStore() throws Exception {

        //deactive all current references
        for (MockManagedReference r : referenceList) {
            r.deactivate();
        }
        referenceList.clear();

        //serialize each member of the data store and then read it back
        //and store in the data store
        for (Iterator<ManagedObject> im = idMap.keySet().iterator();
                im.hasNext();) {
            ManagedObject m = im.next();
            BigInteger id = idMap.get(m);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(m);

            byte[] serializedForm = baos.toByteArray();

            ByteArrayInputStream bais =
                    new ByteArrayInputStream(serializedForm);
            ObjectInputStream ois = new ObjectInputStream(bais);

            m = (ManagedObject) ois.readObject();
            store.put(id, m);
        }

        //record each object id in the id map
        idMap.clear();
        for (Iterator<BigInteger> ib = store.keySet().iterator();
                ib.hasNext();) {
            BigInteger id = ib.next();
            ManagedObject object = store.get(id);
            idMap.put(object, id);
        }
    }

    /**
     * Verify that the object implements both ManagedObject
     * and Serializable
     * @param object
     */
    private void checkArgument(Object object) {
        if (object == null) {
            throw new NullPointerException("The object must not be null");
        }
        if (!(object instanceof ManagedObject)) {
            throw new IllegalArgumentException(
                    "Object does not implement ManagedObject: " +
                    System.identityHashCode(object));
        }
        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException(
                    "Object does not implement Serializable: " +
                    System.identityHashCode(object));
        }
    }

    /**
     * Verify that the object has not been previously removed
     * from the data store
     * @param object
     */
    private void checkRemoved(Object object) {
        if(removedMap.containsKey(object)) {
            throw new ObjectNotFoundException(
                    "Object has been previously removed from the " +
                    "data store: " +
                    System.identityHashCode(object));
        }
    }

    /**
     * Verify that the name is not null
     * @param name
     */
    private void checkNull(String name) {
       if(name == null) {
           throw new NullPointerException("The name must not be null");
       }
    }

    /**
     * Adds the object to the backing data store map.  If the object
     * is already in the map, no changes are made and its id is simply
     * returned.  Otherwise, a new id is assigned to this object,
     * the object is stored, and the id is returned.
     *
     * @param object the object to put into the data store
     * @return the id of the object in the data store
     */
    private BigInteger addToDataStore(ManagedObject object) {
        BigInteger id = idMap.get(object);
        if (id == null) {
            id = masterId;
            masterId = masterId.add(BigInteger.ONE);
            store.put(id, object);
            idMap.put(object, id);
        }
        return id;
    }
}

