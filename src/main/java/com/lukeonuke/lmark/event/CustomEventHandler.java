package com.lukeonuke.lmark.event;

import javafx.event.EventHandler;

public abstract class CustomEventHandler implements EventHandler<CustomEvent> {
    public abstract void onEvent(double scrollPercentage);
}
