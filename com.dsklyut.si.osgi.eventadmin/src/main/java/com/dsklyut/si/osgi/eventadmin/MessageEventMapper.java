package com.dsklyut.si.osgi.eventadmin;

import org.osgi.service.event.Event;
import org.springframework.integration.core.Message;

/**
 * User: dsklyut
 * Date: Feb 12, 2010
 * Time: 1:13:59 PM
 */
public interface MessageEventMapper {

    Event toEvent(Message<?> message);
}
