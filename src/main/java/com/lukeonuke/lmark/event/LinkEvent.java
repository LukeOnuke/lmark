package com.lukeonuke.lmark.event;

import javafx.event.Event;
import javafx.event.EventType;
import org.w3c.dom.html.HTMLAnchorElement;

public abstract class LinkEvent extends Event {
    public static EventType<LinkEvent> EVENT_TYPE = new EventType<>(EventType.ROOT, "LINK_EVENT");
    private final HTMLAnchorElement anchorElement;
    public LinkEvent(EventType eventType, HTMLAnchorElement anchorElement){
        super(eventType);
        this.anchorElement = anchorElement;
    }

    public HTMLAnchorElement getAnchorElement() {
        return anchorElement;
    }
}
