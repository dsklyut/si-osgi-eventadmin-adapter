package com.dsklyut.si.osgi.eventadmin;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;
import org.springframework.integration.core.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * Publishes Messages as events through OSGi EventAdmin
 * User: dsklyut
 * Date: Feb 12, 2010
 * Time: 12:07:26 PM
 */
public class EventPublishingMessageHandler extends AbstractMessageHandler implements InitializingBean {

    public static final int ASYNCHRONOUS_DELIVERY = 1;
    public static final int SYNCHRONOUS_DELIVERY = 2;

    private final Constants constants = new Constants(EventPublishingMessageHandler.class);

    /**
     * Target event admin
     */
    private EventAdmin eventAdmin;

    /**
     * Default is {@link #ASYNCHRONOUS_DELIVERY}
     */
    private int deliveryType = ASYNCHRONOUS_DELIVERY;

    /**
     * Default Topic to publish event to.
     */
    private String defaultTopic = null;

    private MessageEventMapper mapper;

    /**
     * Event admin
     *
     * @param eventAdmin
     */
    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    /**
     * Set delivery type
     *
     * @param deliveryType
     * @see #ASYNCHRONOUS_DELIVERY
     * @see #SYNCHRONOUS_DELIVERY
     */
    public void setDeliveryType(int deliveryType) {
        Assert.state(deliveryType == ASYNCHRONOUS_DELIVERY || deliveryType == SYNCHRONOUS_DELIVERY,
                "Delivery Type must be " + ASYNCHRONOUS_DELIVERY + " ASYNCHRONOUS_DELIVERY or " + SYNCHRONOUS_DELIVERY + " SYNCHRONOUS_DELIVERY");
        this.deliveryType = deliveryType;
    }

    /**
     * Set delivery type as name
     *
     * @param deliveryType
     * @see #setDeliveryType(int)
     */
    public void setDeliveryTypeName(String deliveryType) {
        setDeliveryType(constants.asNumber(deliveryType).intValue());
    }

    /**
     * Default topic to publish to if header value for key {@link com.dsklyut.si.osgi.eventadmin.EventAdminHeaders#TOPIC} is undefined
     *
     * @param defaultTopic
     */

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    /**
     * Mapper to use to map from Message to Event
     *
     * @param mapper
     */
    public void setMapper(MessageEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        Event event = createEventFromMessage(message);

        switch (this.deliveryType) {
            case ASYNCHRONOUS_DELIVERY:
                this.eventAdmin.postEvent(event);
                break;
            case SYNCHRONOUS_DELIVERY:
                this.eventAdmin.sendEvent(event);
                break;
        }
    }

    private Event createEventFromMessage(Message<?> message) {
        return mapper.toEvent(message);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.eventAdmin);
        if (this.mapper == null) {
            this.mapper = new DefaultMessageEventMapper(this.defaultTopic);
        }
    }
}
