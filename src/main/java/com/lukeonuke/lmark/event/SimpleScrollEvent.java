package com.lukeonuke.lmark.event;

import javafx.event.Event;
import javafx.event.EventType;

public class SimpleScrollEvent extends Event {
    public static final EventType<SimpleScrollEvent> SIMPLE_SCROLL_EVENT_TYPE = new EventType(EventType.ROOT, "SIMPLE_SCROLL_EVENT");
    private double scrollPercentage;
    public SimpleScrollEvent(double scrollPercentage) {
        super(SIMPLE_SCROLL_EVENT_TYPE);
        this.scrollPercentage = scrollPercentage;
    }

    public double getScrollPercentage() {
        return scrollPercentage;
    }
}
