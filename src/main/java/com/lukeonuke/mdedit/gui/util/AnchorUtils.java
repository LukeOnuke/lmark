package com.lukeonuke.mdedit.gui.util;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class AnchorUtils {
    public static void anchorAllSides(Node node, double distance){
        AnchorPane.setTopAnchor(node, distance);
        AnchorPane.setLeftAnchor(node, distance);
        AnchorPane.setRightAnchor(node, distance);
        AnchorPane.setBottomAnchor(node, distance);
    }
}
