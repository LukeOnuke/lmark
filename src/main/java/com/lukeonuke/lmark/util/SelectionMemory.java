package com.lukeonuke.lmark.util;

import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;

/**
 * Selection memory. Saves caret (cursor) position and selection, then writes them back to the same text area.
 * */
public class SelectionMemory {
    private int caretPosition;
    private IndexRange indexRange;
    private double scrollTop;

    public SelectionMemory(TextArea textArea) {
        caretPosition = textArea.getCaretPosition();
        indexRange = textArea.getSelection();
        scrollTop = textArea.getScrollTop();
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    public IndexRange getIndexRange() {
        return indexRange;
    }

    public void applyOffset(int selectionStart, int selectionEnd, int caretPosition){
        indexRange = new IndexRange(indexRange.getStart() + selectionStart, indexRange.getEnd() + selectionEnd);
        caretPosition += caretPosition;
    }

    public void applyOffset(int offset){
        applyOffset(offset, offset, offset);
    }

    public void write(TextArea textArea){
        textArea.positionCaret(caretPosition);
        textArea.selectRange(indexRange.getStart(), indexRange.getEnd());
        textArea.setScrollTop(scrollTop);
    }
}
