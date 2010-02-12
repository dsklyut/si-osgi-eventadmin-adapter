package com.dsklyut.si.osgi.eventadmin;

import org.springframework.integration.core.MessageHeaders;

/**
 * User: dsklyut
 * Date: Feb 12, 2010
 * Time: 1:28:21 PM
 */
public abstract class EventAdminHeaders {

    private static final String PREFIX = MessageHeaders.PREFIX + "osgi.eventadmin_";
    public static final String TOPIC = PREFIX + "topic";

}
