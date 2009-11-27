package net.java.dev.mocksgs;

import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.Delivery;

/**
 * Register a MockChannelFactory with the MockChannelManager in order to
 * inject your own MockChannel objects.
 */
public interface MockChannelFactory {

    public MockChannel createChannel(final String channelName, final ChannelListener channelListener, final Delivery delivery);
}
