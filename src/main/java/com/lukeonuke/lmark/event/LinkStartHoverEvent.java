package com.lukeonuke.lmark.event;

import javafx.event.EventType;
import org.w3c.dom.html.HTMLAnchorElement;

public class LinkStartHoverEvent extends LinkEvent {
    public static final EventType<LinkStartHoverEvent> LINK_START_HOVER_EVENT_TYPE = new EventType<>(LinkEvent.EVENT_TYPE, "LINK_START_HOVER_EVENT");
    public LinkStartHoverEvent(HTMLAnchorElement anchorElement){
        super(LINK_START_HOVER_EVENT_TYPE, anchorElement);
    }
}
