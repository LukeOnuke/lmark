module mdedit {
    requires javafx.controls;
    requires javafx.web;
    requires flexmark.all;
    requires flexmark.util.data;
    requires flexmark;
    requires flexmark.profile.pegdown;
    requires flexmark.util.misc;
    requires jdk.xml.dom;
    requires java.desktop;

    exports com.lukeonuke.mdedit;
}
