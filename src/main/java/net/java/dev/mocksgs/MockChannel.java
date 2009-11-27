package net.java.dev.mocksgs;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.Delivery;

public class MockChannel implements Channel {

    private final String name;
    private final ChannelListener listener;
    private final Delivery delivery;
    private final Map<ClientSession, ChannelListener> clientSessions = new HashMap<ClientSession, ChannelListener>();

    public MockChannel(final String name, final ChannelListener listener, final Delivery delivery) {
        this.name = name;
        this.listener = listener;
        this.delivery = delivery;
    }

    public void close() {
    }

    public Delivery getDeliveryRequirement() {
        return delivery;
    }

    public String getName() {
        return name;
    }

    public Iterator<ClientSession> getSessions() {
        return clientSessions.keySet().iterator();
    }

    public boolean hasSessions() {
        return !clientSessions.isEmpty();
    }

    public Channel join(Set<ClientSession> sessions) {
        for (ClientSession s : sessions) {
            join(s);
        }
        return this;
    }

    public Channel leave(Set<ClientSession> sessions) {
        for (ClientSession s : sessions) {
            leave(s);
        }
        return this;
    }

    public Channel join(final ClientSession clientSession) {
        clientSessions.put(clientSession, listener);
        return this;
    }

    public Channel leave(final ClientSession clientSession) {
        clientSessions.remove(clientSession);
        return this;
    }

    public Channel leaveAll() {
        clientSessions.clear();
        return this;
    }

    public Channel send(final ClientSession arg0, final ByteBuffer arg1) {
        return this;
    }

    // Mock extras
    public Map<ClientSession, ChannelListener> getClientSessions() {
        return clientSessions;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public ChannelListener getListener() {
        return listener;
    }
}
