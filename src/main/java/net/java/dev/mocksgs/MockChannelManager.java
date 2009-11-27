package net.java.dev.mocksgs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.NameExistsException;
import com.sun.sgs.app.NameNotBoundException;

public class MockChannelManager implements ChannelManager {
	
    private final Map<String, MockChannel> channels = new HashMap<String, MockChannel>();

    // Default channel factory just returns channels
    private MockChannelFactory channelFactory = new MockChannelFactory() {

        public MockChannel createChannel(final String channelName, final ChannelListener channelListener, final Delivery delivery) {
            return new MockChannel(channelName, channelListener, delivery);
        }
    };


    public void setMockChannelFactor(final MockChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }


    public MockChannel createChannel(final String channelName, final ChannelListener channelListener, final Delivery delivery) {

        if (channelListener != null && !(channelListener instanceof Serializable)) {
            throw new IllegalArgumentException("Channel Listener wasn't serializable: " + channelListener.getClass().getCanonicalName());
        }

        if (channels.containsKey(channelName)) {
            throw new NameExistsException(channelName);
        }

        MockChannel channel = channelFactory.createChannel(channelName, channelListener, delivery);

        channels.put(channelName, channel);

        return channel;
    }

    public MockChannel getChannel(final String name) {
        MockChannel channel = channels.get(name);

        if (channel == null) {
            throw new NameNotBoundException(name);
        }

        return channel;
    }

    public Map<String, MockChannel> getChannels() {
        return channels;
    }


}

