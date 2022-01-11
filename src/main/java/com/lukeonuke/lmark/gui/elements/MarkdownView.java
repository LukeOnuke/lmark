package com.lukeonuke.lmark.gui.elements;

import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.event.LinkStartHoverEvent;
import com.lukeonuke.lmark.event.LinkStopHoverEvent;
import com.lukeonuke.lmark.event.SimpleScrollEvent;
import com.lukeonuke.lmark.util.FileUtils;
import com.lukeonuke.lmark.util.FxUtils;
import com.lukeonuke.lmark.util.OSIntegration;
import com.lukeonuke.lmark.util.ThemeManager;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MarkdownView {
    private final Logger logger = LoggerFactory.getLogger(MarkdownView.class);
    private final WebView webView = new WebView();
    private JSBridge jsBridge;
    private static final HtmlRenderer renderer;
    private static final Parser parser;
    private double scrollY;
    private String contents;
    private final static MutableDataSet options = new MutableDataSet();
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private static org.jsoup.nodes.Document.OutputSettings outputSettings = new org.jsoup.nodes.Document.OutputSettings();

    static {
        outputSettings.prettyPrint(false);
        options.set(Parser.EXTENSIONS, getExtensions());
        /*options.set(HtmlRenderer.SUPPRESS_HTML, true);
        options.set(HtmlRenderer.ESCAPE_HTML, true);*/
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public MarkdownView() {
        webView.getStyleClass().add("markdown");
        webView.getEngine().getHistory().getEntries().clear();
        webView.contextMenuEnabledProperty().setValue(false);
        webView.getEngine().setUserDataDirectory(FileUtils.getRelativeFile(ApplicationConstants.MARKDOWN_CACHE_PATH));

        webView.getEngine().setOnError(errorEvent -> {
            logger.error(errorEvent.getMessage());
        });

        webView.getEngine().setOnAlert(stringWebEvent -> {
            logger.info(stringWebEvent.getData());
        });

        /*
         * Register js bridge and add clicks to open in browser
         * ====================================================
         * When a link is clicked open it in a new window, although the WebEngine#getDocument() needs to wait to when
         * the load-worker loads the webpage. Plus, this enables it to work every time something is loaded.
         * */
        webView.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Document document = webView.getEngine().getDocument();

                //Js bridge
                jsBridge = new JSBridge(); //Bit stupid making a variable but sometimes the jvm just dies without it
                ((JSObject) webView.getEngine().executeScript("window")).setMember("mdedit", jsBridge);

                webView.getEngine().executeScript("document.addEventListener('scroll', function(event) {" +
                        "mdedit.scroll(document.body.scrollTop ,document.body.scrollHeight - document.body.clientHeight);" +
                        "});");

                scroll(scrollY);

                //Clickable links
                NodeList nodeList = document.getElementsByTagName("a");
                Node node;
                EventTarget eventTarget;
                for (int i = 0; i < nodeList.getLength(); i++) {
                    node = nodeList.item(i);
                    eventTarget = (EventTarget) node;

                    eventTarget.addEventListener("mouseenter", evt -> {
                        webView.fireEvent(new LinkStartHoverEvent((HTMLAnchorElement) evt.getCurrentTarget()));
                    }, false);
                    eventTarget.addEventListener("mouseleave", evt -> {
                        webView.fireEvent(new LinkStopHoverEvent((HTMLAnchorElement) evt.getCurrentTarget()));
                    }, false);
                    eventTarget.addEventListener("click", evt -> {
                        HTMLAnchorElement anchorElement = (HTMLAnchorElement) evt.getCurrentTarget();
                        String href = anchorElement.getHref();
                        if (href.startsWith("#")) {
                            webView.getEngine()
                                    .executeScript("var ele = document.getElementById(`" + href.replace("#", "") + "`);" +
                                            "if(ele != null){ele.scrollIntoView(true);}");
                            return;
                        }
                        //handle opening URL outside JavaFX WebView
                        OSIntegration.openWebpage(anchorElement.getHref());

                        evt.preventDefault();
                    }, false);
                }
            }
        });
    }

    public static List<Extension> getExtensions(){
        List<Extension> extensions = new ArrayList<>();
        extensions.add(TablesExtension.create());
        extensions.add(TaskListExtension.create());
        extensions.add(AnchorLinkExtension.create());
        extensions.add(StrikethroughExtension.create());
        return extensions;
    }

    private void setContents(String contents) {
        this.contents = contents;
        refresh();
    }

    public void refresh() {
        webView.getEngine().loadContent(renderWithStyleSheet(themeManager.getWebCSS(this)));
    }

    private String renderWithStyleSheet(String css) {
        return renderWithStyleSheet(css, "");
    }

    private String renderWithStyleSheet(String css, String additions) {

        try {
            css = FileUtils.getResourceAsString(css);
        } catch (IOException e) {
            try {
                css = FileUtils.readSpecifiedFile(new File(css));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        css += additions;

        return "<head>\r\n<style>body{padding: 10px;}\r\n" + css
                + "</style>\r\n</head>\r\n<body class='markdown-body'>"
                + contents + "</body>";
    }

    public void setMDContents(String contents) {
        setRenderedContent(renderer.render(parser.parse(preFilter(contents))));
    }

    public void setRenderedContent(String html){
        FxUtils.lazyRunOnPlatform(() -> setContents(filter(html)));
    }

    public WebView getNode() {
        return webView;
    }

    public void scrollTo(double y) {
        scrollY = y;
        scroll(scrollY);
    }

    private void scroll(double y) {
        webView.getEngine().executeScript("window.scrollTo(0, " +
                "((document.body.scrollHeight - document.body.clientHeight)" + "* " + y + "));");
    }

    private String filter(String string) {
        org.jsoup.nodes.Document document = Jsoup.parse(string.replace("<strong>", "<b>")
                .replace("</strong>", "</b>"));
        return document.html();
    }

    /**
     * Filter out nasty stuff the most secure way i could think of. Uses NCR <i>Numeric Character Reference<i/> to
     * replace < into <code>&lt;<code/>
     * <code><code/>
     * */
    private String preFilter(String string) {
        return Jsoup.clean(string, "", Safelist.basicWithImages(), outputSettings);
    }

    public String getContents() {
        return (String) webView.getEngine().executeScript("document.documentElement.outerHTML");
    }

    public String getPDFReadyDocument() {
        org.jsoup.nodes.Document document = Jsoup.parse(renderWithStyleSheet(ApplicationConstants.WEB_MARKDOWN_CSS,
                "@page{size: A4 portrait;} img{max-width: 90vw;}"));
        document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        return document.html();
    }


    /**
     * Implements a bridge between the javascript on the web view and the java:tm: on ere
     * https://stackoverflow.com/questions/35985601/calling-a-java-method-from-a-javafx-webview
     */
    public class JSBridge {
        public void scroll(int current, int max) {
            webView.fireEvent(new SimpleScrollEvent(((float) (current) / max)));
        }

        public void log(String s) {
            logger.info(s);
        }
    }
}
