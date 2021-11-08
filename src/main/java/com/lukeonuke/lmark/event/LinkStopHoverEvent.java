package com.lukeonuke.lmark.event;

import javafx.event.EventType;
import org.w3c.dom.html.HTMLAnchorElement;

public class LinkStopHoverEvent extends LinkEvent {
    public static final EventType<LinkStopHoverEvent> LINK_STOP_HOVER_EVENT_TYPE = new EventType<>(LinkEvent.EVENT_TYPE, "LINK_STOP_HOVER_EVENT");
    public LinkStopHoverEvent(HTMLAnchorElement anchorElement){
        super(LINK_STOP_HOVER_EVENT_TYPE, anchorElement);
    }
}