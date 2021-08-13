package com.lukeonuke.mdedit.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class SimpleScrollEvent extends CustomEvent {
    public static final EventType<CustomEvent> SIMPLE_SCROLL_EVENT_TYPE = new EventType(CUSTOM_EVENT_TYPE, "CustomEvent1");
    private double scrollPercentage;
    public SimpleScrollEvent(double scrollPercentage) {
        super(SIMPLE_SCROLL_EVENT_TYPE);
        this.scrollPercentage = scrollPercentage;
    }

    public double getScrollPercentage() {
        return scrollPercentage;
    }

    @Override
    public void invokeHandler(CustomEventHandler handler) {
        handler.onEvent(scrollPercentage);
    }
}
