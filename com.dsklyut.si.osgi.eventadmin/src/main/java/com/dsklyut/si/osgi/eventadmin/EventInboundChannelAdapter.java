package com.dsklyut.si.osgi.eventadmin;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * User: dsklyut
 * Date: Feb 11, 2010
 * Time: 8:27:51 PM
 */
public class EventInboundChannelAdapter extends MessageProducerSupport implements EventHandler {

    /**
     * Matcher to use for topic matching.
     */
    private PathMatcher topicMatcher = new AntPathMatcher();

    /**
     * Topics to listen to.
     */
    private final Set<String> topics = new CopyOnWriteArraySet<String>();


    /**
     * Matcher for topics.
     * <p/>
     * Default: is {@link org.springframework.util.AntPathMatcher}
     * <p/>
     * <strong>Note:</strong> This option is in addition to matching done by the OSGi EventAdmin.
     * See OSGi compendium spec section 113.4 for details.
     *
     * @param topicMatcher
     */
    public void setTopicMatcher(PathMatcher topicMatcher) {
        this.topicMatcher = topicMatcher;
    }

    /**
     * Topics to listen to.
     * <p/>
     * <strong>Note:</strong> This property is in addition to service-properties that can be set-up during osgi service publication
     * See OSGi compendium spec section 113.4 for details.
     *
     * @param topics
     * @see org.osgi.service.event.EventHandler
     */
    public void setTopics(Set<String> topics) {
        // not asserting on topics - because topics of interest can be provided as service properties also.
        synchronized (this.topics) {
            this.topics.clear();
            this.topics.addAll(topics);
        }
    }

    /**
     * Workflow:
     * <ol>
     * <li>If topic list is empty - send message</li>
     * <li>If topicMatcher is null - do direct match to the topic set - send message to on all matches</li>
     * <li>Otherwise - use matcher and send message on all matches</li>
     * </ol>
     *
     * @param event
     */
    @Override
    public void handleEvent(Event event) {
        if (CollectionUtils.isEmpty(topics)) {
            this.sendAsMessage(event);
            return;
        }

        final String targetTopic = event.getTopic();
        if (topicMatcher == null) {
            if (this.topics.contains(targetTopic)) {
                this.sendAsMessage(event);
            }
        } else {
            for (String topic : topics) {
                if (topicMatcher.match(topic, targetTopic)) {
                    this.sendAsMessage(event);
                }
            }
        }
    }

    private void sendAsMessage(Event event) {
        this.sendMessage(MessageBuilder.withPayload(event).copyHeaders(copyEventPropertiesToMap(event)).build());
    }

    private Map<String, Object> copyEventPropertiesToMap(Event event) {
        Map<String, Object> result = new HashMap<String, Object>();
        String[] keys = event.getPropertyNames();
        for (String key : keys) {
            result.put(key, event.getProperty(key));
        }
        return result;
    }


    // -- TODO: maybe auto register/de-register as an osgi service?  Will need a bundleContext ref and maybe more...

    @Override
    protected void doStart() {
        // nothing for now
    }

    @Override
    protected void doStop() {
        // nothing for now
    }
}
