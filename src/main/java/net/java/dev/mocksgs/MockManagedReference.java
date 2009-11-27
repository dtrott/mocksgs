package net.java.dev.mocksgs;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.math.BigInteger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.TransactionNotActiveException;

/**
 * This is a simple mockup of a {@link ManagedReference}. It stores the
 * id of the associated ManagedObject.  It always requests
 * an object by polling the backing {@link MockDataManager} with this id.
 */
public class MockManagedReference<T> implements ManagedReference<T>,
                                                Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The id of the associated {@code ManagedObject}
     */
    private final BigInteger id;
    /**
     * A boolean indicating whether or not this {@code ManagedReference}
     * is still in an active state.  If it is not active, attempts to 
     * retrieve the associated {@code ManagedObject} will throw an
     * exception.
     */
    private transient boolean active;

    public MockManagedReference(BigInteger id) {
        this.id = id;
        this.active = true;
    }

    @Override
    public T get() {
        return internalGet();
    }

    @Override
    public T getForUpdate() {
        return internalGet();
    }

    @SuppressWarnings("unchecked")
    private T internalGet() {
        if (!active) {
            throw new TransactionNotActiveException(
                    "Transaction not active");
        }
        DataManager dm = AppContext.getDataManager();
        if (!(dm instanceof MockDataManager)) {
            throw new IllegalStateException(
                    "MockManagedReference cannot be used without " +
                    " a backing MockDataManager");
        }
        return (T) ((MockDataManager) dm).getObjectWithId(id);
    }

    @Override
    public BigInteger getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof MockManagedReference) {
            return ((MockManagedReference) object).id.equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        long oid = id.longValue();
        return (int) (oid ^ (oid >>> 32)) + 6883;
    }

    /**
     * Deactivates this {@code ManagedReference}.  Attempts to retrieve
     * the associated {@code ManagedObject} while in a deactivated state
     * will throw an exception.
     */
    void deactivate() {
        this.active = false;
    }

    /**
     * Reads in a serialized object.  This will initialize the state of
     * the object to its state from the input stream and will also set
     * the value of its transient active field to {@code true}.
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        active = true;
    }
}

