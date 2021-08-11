package com.lukeonuke.mdedit.gui.elements;

import com.lukeonuke.mdedit.gui.util.FileUtils;
import com.lukeonuke.mdedit.gui.util.OSIntegration;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.io.IOException;

public class Markdown {
    private final WebView webView = new WebView();
    public Markdown() {
        webView.getEngine().getHistory().getEntries().clear();
        webView.contextMenuEnabledProperty().setValue(false);

        webView.getEngine().executeScript("window.onscroll = function(event) {" +
                "" +
                "}");

        /*When a link is clicked open it in a new window, although the WebEngine#getDocument() needs to wait to when
        * the load-worker loads the webpage. Plus, this enables it to work every time something is loaded.
        * */
        webView.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++)
                {
                    Node node= nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", new EventListener()
                    {
                        @Override
                        public void handleEvent(Event evt)
                        {
                            HTMLAnchorElement anchorElement = (HTMLAnchorElement) evt.getCurrentTarget();
                            String href = anchorElement.getHref();
                            if(href.startsWith("#")) return;
                            //handle opening URL outside JavaFX WebView
                            OSIntegration.openWebpage(anchorElement.getHref());

                            evt.preventDefault();
                        }
                    }, false);
                }
            }
        });
    }

    public void setContents(String contents){
        try {
            webView.getEngine().loadContent("<head><style>body{padding: 10px;}" + FileUtils.getResourceAsString("/gui/web/markdown-light.css") + "</style></head><body class='markdown-body'>"
                    + contents + "</body>", "text/html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMDContents(String contents){
        DataHolder options = PegdownOptionsAdapter.flexmarkOptions(
                Extensions.ALL
        );
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        setContents(renderer.render(parser.parse(contents)));
    }

    public WebView getNode(){
        return webView;
    }

    public void scrollTo(int x, int y){
        webView.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
    }
}
