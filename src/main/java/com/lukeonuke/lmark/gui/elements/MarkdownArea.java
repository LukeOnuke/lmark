package com.lukeonuke.lmark.gui.elements;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;

public class MarkdownArea extends CodeArea {
    public void setText(String text){

    }

    public void computeHighlighting(){
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

    }
}
