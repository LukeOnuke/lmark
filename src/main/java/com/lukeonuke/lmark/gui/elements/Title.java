package com.lukeonuke.lmark.gui.elements;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class Title extends FlowPane {
    String text;

    public Title(String text) {
        setText(text);
        init();
        this.setOrientation(Orientation.HORIZONTAL);
    }

    public Title(Orientation orientation, String text) {
        super(orientation);
        setText(text);
        init();
    }

    public Title(double v, double v1, String text) {
        super(v, v1);
        setText(text);
        init();
    }

    public Title(Orientation orientation, double v, double v1, String text) {
        super(orientation, v, v1);
        setText(text);
        init();
    }

    public Title(String text, Node... nodes) {
        super(nodes);
        setText(text);
        init();
    }

    public Title(Orientation orientation, String text, Node... nodes) {
        super(orientation, nodes);
        setText(text);
        init();
    }

    public Title(double v, double v1, String text, Node... nodes) {
        super(v, v1, nodes);
        setText(text);
        init();
    }

    public Title(Orientation orientation, double v, double v1, String text, Node... nodes) {
        super(orientation, v, v1, nodes);
        setText(text);
        init();
    }

    public void setText(String text){
        this.text = text;
        Label letter;
        short i = -1;
        for (Character character : text.toCharArray()) {
            letter = new Label(character.toString());
            letter.getStyleClass().addAll("title-text");
            this.getChildren().add(letter);
            i++;
            if(i == 0) letter.getStyleClass().addAll("title-text-notfirst");
        }
    }

    private void init(){
        this.getStyleClass().add("title");
    }

    public String getText() {
        return text;
    }
}
