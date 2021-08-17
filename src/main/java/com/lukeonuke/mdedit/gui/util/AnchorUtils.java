package com.lukeonuke.mdedit.gui.util;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class AnchorUtils {
    public static void anchorAllSides(Node node, double distance) {
        anchor(node, distance, distance, distance, distance);
    }

    public static void anchorTopLeft(Node node, double top, double left){
        anchor(node, top, -1D, left, -1D);
    }

    public static void anchor(Node node, double top, double bottom, double left, double right) {
        if (top != -1D ) AnchorPane.setTopAnchor(node, top);
        if (bottom != -1D ) AnchorPane.setBottomAnchor(node, bottom);
        if (left != -1D ) AnchorPane.setLeftAnchor(node, left);
        if (right != -1D ) AnchorPane.setRightAnchor(node, right);
    }
}
