package com.lukeonuke.lmark.gui.elements;

import com.lukeonuke.lmark.util.Icons;
import com.lukeonuke.lmark.util.SelectionMemory;
import com.lukeonuke.lmark.util.StyleRegister;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class MarkdownArea extends CodeArea {
    private Logger logger = LoggerFactory.getLogger(MarkdownArea.class);
    private ArrayList<MarkdownView> slaves = new ArrayList<>();

    static MutableDataSet options = new MutableDataSet();
    static Parser parser;
    private final HtmlRenderer renderer = HtmlRenderer.builder(options).build();

    static {
        options.set(Parser.EXTENSIONS, MarkdownView.getExtensions());
        options.set(HtmlRenderer.SUPPRESS_HTML, true);
        options.set(HtmlRenderer.ESCAPE_HTML, true);
        parser = Parser.builder(options).build();
    }

    public MarkdownArea() {
        this.wrapTextProperty().set(true);

        this.textProperty().addListener((observableValue, s, t1) -> {
                    computeHighlighting();
                    if (t1.replace(s, "").equals("\n")) return;
                    if (!t1.endsWith("\n")) this.appendText("\n");
                }
        );

        setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.Z)) undo();
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.Y)) redo();
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem bold = new MenuItem("Bold");
        bold.setOnAction(actionEvent -> formatItalicize(2));
        bold.setGraphic(new Icon(Icons.BOLD));
        contextMenu.getItems().addAll(bold);
        setContextMenu(contextMenu);

        this.getStyleClass().add("markdown-area");
        this.setId("markdown-area");
    }

    public void computeHighlighting() {
        StyleRegister styleRegister = new StyleRegister(this.getLength());
        Document doc = parser.parse(this.getText());
        doc.getDescendants().forEach(node -> {
            styleRegister.setStyleBetween(node.getStartOffset(), node.getEndOffset() - 1, new ArrayList<>(Arrays.asList(node.getNodeName().toLowerCase(Locale.ENGLISH))));
        });
        if (styleRegister.isModified()) this.setStyleSpans(0, styleRegister.getStyleSpans());


        refreshSlaves(doc);
    }

    public double getScrollY() {
        return this.getEstimatedScrollY() / (this.getTotalHeightEstimate() - this.getHeight());
    }

    public void setScrollY(double scroll) {
        if (totalHeightEstimateProperty().getValue() == null) return;
        this.scrollYToPixel(scroll * (this.getTotalHeightEstimate() - this.getHeight()));
    }

    private boolean selectionOutOfRange(IndexRange indexRange, int length, int offsetStart, int offsetEnd) {
        if (indexRange.getStart() + offsetStart > length) return true;
        return indexRange.getEnd() + offsetEnd > length;
    }

    public void formatItalicize(int count) {
        formatSelection(count, '*');
    }

    public void formatStrikethrough() {
        formatSelection(2, '~');
    }

    private void formatSelection(int count, char character) {
        if (this.getSelection() == null) return;
        if (this.getSelection().getStart() == 0 && this.getSelection().getEnd() == 0) return;
        boolean isFormatted = true;
        for (int i = 0; i < count; i++) {
            repairSelect(character);
        }
        for (int i = 0; i < count; i++) {
            isFormatted = isFormatted && selectionIsFormattedWithChar(i, character);
        }
        if (isFormatted) {
            unformatSelection(count - 1);
        } else {
            formatWithChar(character, count - 1);
        }
    }

    private void unformatSelection(int offset) {
        IndexRange selection = this.getSelection();
        StringBuilder text = new StringBuilder(
                this.getText(selection.getStart() - 1 - offset, selection.getEnd() + offset));
        double scroll = this.getScrollY();
        text.delete(0, 1 + offset);
        text.delete(text.length() - offset, text.length() + 1);
        this.replaceText(selection.getStart() - 1 - offset, selection.getEnd() + 1 + offset, text.toString());
        this.selectRange(selection.getStart() - 1 - offset, selection.getEnd() - 1 - offset);
        this.setScrollY(scroll);
    }

    private boolean selectionIsFormattedWithChar(int offset, char character) {
        IndexRange selection = this.getSelection();
        return this.getText().charAt(selection.getStart() - 1 - offset) == character && this.getText().charAt(selection.getEnd() + offset) == character;
    }

    private void formatWithChar(char character, int count) {
        IndexRange selection = this.getSelection();
        StringBuilder text = new StringBuilder(this.getSelectedText());
        double scroll = this.getScrollY();

        String repeatedCharacter = String.valueOf(character).repeat(count + 1);
        text.append(repeatedCharacter);
        text.insert(0, repeatedCharacter);
        this.replaceText(selection, text.toString());
        this.selectRange(selection.getStart() + 1 + count, selection.getEnd() + 1 + count);
        this.setScrollY(scroll);
    }

    private void repairSelect(char character) {
        IndexRange selection = this.getSelection();
        if (selectionOutOfRange(this.getSelection(), this.getLength(), -1, 1)) return;

        if (Objects.equals(this.getText(selection.getStart(), selection.getStart() + 1), String.valueOf(character))) {
            this.selectRange(selection.getStart() + 1, selection.getEnd());
            selection = this.getSelection();
        }
        if (Objects.equals(this.getText(selection.getEnd() - 1, selection.getEnd()), String.valueOf(character))) {
            this.selectRange(selection.getStart(), selection.getEnd() - 1);
        }
    }

    public int getBeginningOfLine() {
        int caretPos = this.getCaretPosition();
        String text = this.getText(0, this.getCaretPosition());

        if (!text.contains("\n")) {
            return 0;
        }

        int index = caretPos;
        while (text.charAt(index - 1) != '\n') {
            if (index < 1) return -1;
            index--;
        }
        return index;
    }

    public int getEndOfLine() {
        String text = this.getText(getBeginningOfLine(), this.getLength());
        if (!text.contains("\n")) return this.getLength();
        return text.indexOf('\n') + getBeginningOfLine();
    }

    public void formatBullet(String bullet) {
        SelectionMemory selectionMemory = new SelectionMemory(this);
        int index = getBeginningOfLine();
        if (index == -1) return;
        this.insertText(index, bullet);
        selectionMemory.applyOffset(bullet.length());
        selectionMemory.write(this);
    }

    public void removeBullet(String bullet) {
        SelectionMemory selectionMemory = new SelectionMemory(this);
        int index = this.getText(getBeginningOfLine(), getEndOfLine()).indexOf(bullet);
        index += getBeginningOfLine();
        this.deleteText(index, index + bullet.length());
        selectionMemory.applyOffset(-1 * bullet.length());
        selectionMemory.write(this);
    }

    public void replaceBullet(String bulletToReplace, String replacementBullet) {
        SelectionMemory selectionMemory = new SelectionMemory(this);
        int beginningOfLine = getBeginningOfLine();
        int endOfLine = getEndOfLine();
        this.replaceText(beginningOfLine, endOfLine,
                this.getText(beginningOfLine, endOfLine).replace(bulletToReplace, replacementBullet));
        selectionMemory.applyOffset(replacementBullet.length() - bulletToReplace.length());
        selectionMemory.write(this);
    }

    public boolean isFormattedBullet(String bullet) {
        return this.getText(getBeginningOfLine(), getEndOfLine()).trim().startsWith(bullet);
    }

    public void dotBulletFormat() {
        final String bullet = "- ";
        if (isFormattedBullet(bullet)) {
            removeBullet(bullet);
        } else {
            formatBullet(bullet);
        }
    }

    public void checkListBulletFormat() {
        final String checked = "- [x]";
        final String unchecked = "- [ ]";

        if (isFormattedBullet(unchecked)) {
            replaceBullet(unchecked, checked);
        } else if (isFormattedBullet(checked)) {
            removeBullet(checked);
        } else {
            formatBullet(unchecked);
        }
    }

    public void titleFormat() {
        final String title = "# ";
        final String title2 = "## ";
        final String title3 = "### ";
        if (isFormattedBullet(title)) {
            replaceBullet(title, title2);
        } else if (isFormattedBullet(title2)) {
            replaceBullet(title2, title3);
        } else if (isFormattedBullet(title3)) {
            removeBullet(title3);
        } else {
            formatBullet(title);
        }
    }

    public void registerSlave(MarkdownView markdownView) {
        slaves.add(markdownView);
    }

    public void removeSlave(MarkdownView markdownView) {
        slaves.remove(markdownView);
    }

    private void refreshSlaves(Document doc) {
        new Thread(() -> {
            final String html = renderer.render(doc);
            slaves.forEach(markdownView -> {
                markdownView.setRenderedContent(html);
            });
        }, "refresh-worker").start();
    }
}
