package com.dsklyut.si.osgi.eventadmin;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.ErrorMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dsklyut
 * Date: Feb 12, 2010
 * Time: 1:14:48 PM
 */
public class DefaultMessageEventMapper implements MessageEventMapper {

    private final String defaultTopic;

    public DefaultMessageEventMapper(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    /**
     * Expects to find Topic name under {@link com.dsklyut.si.osgi.eventadmin.EventAdminHeaders#TOPIC} key
     *
     * @param message
     * @return
     */
    @Override
    public Event toEvent(Message<?> message) {
        return this.toEvent(message.getHeaders().get(EventAdminHeaders.TOPIC, String.class), message);
    }

    /**
     * Takes all message headers as base for {@link org.osgi.service.event.Event} properties.
     * Sets up:
     * <ol>
     * <li>{@link org.osgi.service.event.EventConstants#TIMESTAMP} = {@link org.springframework.integration.core.MessageHeaders#getTimestamp()}</li>
     * <li> In case of {@link org.springframework.integration.message.ErrorMessage} sets up
     * {@link org.osgi.service.event.EventConstants#EXCEPTION},
     * {@link org.osgi.service.event.EventConstants#EXCEPTION_CLASS},
     * {@link org.osgi.service.event.EventConstants#EXCEPTION_MESSAGE}
     * </li>
     * <li>adds source messages at {@link org.osgi.service.event.EventConstants#EVENT}</li>
     * </ol>
     *
     * @param topic
     * @param message
     * @return
     */
    private Event toEvent(String topic, Message<?> message) {

        if (!StringUtils.hasText(topic)) {
            topic = defaultTopic;
        }

        Assert.hasText(topic, "Topic is required");

        // take all of the headers to start with
        Map<String, Object> headers = new HashMap<String, Object>(message.getHeaders());

        // set-up osgi headers
        headers.put(EventConstants.TIMESTAMP, message.getHeaders().getTimestamp());

        // special treatment of ErrorMessage
        if (message instanceof ErrorMessage) {
            final ErrorMessage errorMessage = (ErrorMessage) message;
            headers.put(EventConstants.EXCEPTION, errorMessage.getPayload());
            headers.put(EventConstants.EXCEPTION_CLASS, errorMessage.getPayload().getClass());
            headers.put(EventConstants.EXCEPTION_MESSAGE, errorMessage.getPayload().getMessage());
        }

        // add original message as EVENT
        headers.put(EventConstants.EVENT, message);


        return new Event(topic, headers);
    }
}
