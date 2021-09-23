package com.lukeonuke.lmark.gui.elements;

import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.event.SimpleScrollEvent;
import com.lukeonuke.lmark.gui.util.FileUtils;
import com.lukeonuke.lmark.gui.util.OSIntegration;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.io.IOException;

public class Markdown {
    private final Logger logger = LoggerFactory.getLogger(Markdown.class);
    private final WebView webView = new WebView();
    private JSBridge jsBridge;
    private static HtmlRenderer renderer;
    private static Parser parser;
    private double scrollY;

    static{
        DataHolder options = PegdownOptionsAdapter.flexmarkOptions(
                Extensions.ALL
        );
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public Markdown() {
        webView.getEngine().getHistory().getEntries().clear();
        webView.contextMenuEnabledProperty().setValue(false);

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
                JSObject win = (JSObject) document;
                jsBridge = new JSBridge(); //Bit stupid making a variable but sometimes the jvm just dies without it
                ((JSObject) webView.getEngine().executeScript("window")).setMember("mdedit", jsBridge);

                webView.getEngine().executeScript("document.addEventListener('scroll', function(event) {" +
                        "mdedit.scroll(window.scrollY ,Math.max( document.body.scrollHeight, document.body.offsetHeight," +
                        " document.documentElement.clientHeight," +
                        " document.documentElement.scrollHeight," +
                        " document.documentElement.offsetHeight ))" +
                        "});");

                scroll(scrollY);

                //Clickable links
                NodeList nodeList = document.getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", new EventListener() {
                        @Override
                        public void handleEvent(Event evt) {
                            HTMLAnchorElement anchorElement = (HTMLAnchorElement) evt.getCurrentTarget();
                            String href = anchorElement.getHref();
                            if (href.startsWith("#")) return;
                            //handle opening URL outside JavaFX WebView
                            OSIntegration.openWebpage(anchorElement.getHref());

                            evt.preventDefault();
                        }
                    }, false);
                }
            }
        });
    }

    public void setContents(String contents) {
        try {
            webView.getEngine().loadContent("<head><style>body{padding: 10px;}" + FileUtils.getResourceAsString(ApplicationConstants.WEB_MARKDOWN_CSS) + "</style></head><body class='markdown-body'>"
                    + contents + "</body>", "text/html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMDContents(String contents) {

        setContents(filter(renderer.render(parser.parse(contents))));

    }

    public WebView getNode() {
        return webView;
    }

    public void scrollTo(double y) {
        scrollY = y;
        scroll(scrollY);
    }

    private void scroll(double y){
        webView.getEngine().executeScript("window.scrollTo(0, " +
                "(Math.max( document.body.scrollHeight, document.body.offsetHeight," +
                " document.documentElement.clientHeight," +
                " document.documentElement.scrollHeight," +
                " document.documentElement.offsetHeight )" + "* " + y + "));");
    }

    private String filter(String string) {
        return string.replace("<strong>", "<b>").replace("</strong>", "</b>");
    }

    /**
     * Implements a bridge between the javascript on the web view and the java:tm: on ere
     * https://stackoverflow.com/questions/35985601/calling-a-java-method-from-a-javafx-webview
     */
    public class JSBridge {
        public void scroll(int current, int max) {
            //System.out.println("Scrolled to " + current + " out of " + max + " in percent " + ((float)(current + 200)/max) * 100);
            webView.fireEvent(new SimpleScrollEvent(((float) (current) / max)));
        }
    }
}
