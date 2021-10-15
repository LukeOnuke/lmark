module lmark {
    requires javafx.controls;
    requires javafx.web;
    requires flexmark.all;
    requires flexmark.util.data;
    requires flexmark;
    requires flexmark.profile.pegdown;
    requires flexmark.util.misc;
    requires jdk.xml.dom;
    requires java.desktop;
    requires jdk.jsobject;
    requires java.logging;
    requires org.slf4j;
    requires com.github.albfernandez.juniversalchardet;
    requires com.google.gson;
    requires javafx.graphics;
    requires flexmark.pdf.converter;
    requires openhtmltopdf.core;

    opens com.lukeonuke.lmark.gui.elements to javafx.web;

    exports com.lukeonuke.lmark;
}
