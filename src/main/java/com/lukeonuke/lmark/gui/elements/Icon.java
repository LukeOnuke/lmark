package com.lukeonuke.lmark.gui.elements;

import com.lukeonuke.lmark.ApplicationConstants;
import javafx.scene.text.Text;

public class Icon extends Text {
    private Icon() {
    }

    public Icon(String s) {
        super(s);
        setFont(ApplicationConstants.FONTS_AWESOME);
        getStyleClass().add("text");
    }

    private Icon(double v, double v1, String s) {
        super(v, v1, s);
    }
}
