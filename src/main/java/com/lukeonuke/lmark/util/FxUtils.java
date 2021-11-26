package com.lukeonuke.lmark.util;

import com.lukeonuke.lmark.ApplicationConstants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

/**
 * Utility class for javaFX management.
 *
 * @author lukak
 * @since 1.0.0
 */
public class FxUtils {
    /**
     * Will lazy run stuff that needs to be run on the jfx main thread. Lazy run means that it will add it to the
     * <code>Platform.runLater()<code/> queue only if it is not on the jfx main thread.
     */
    public static void lazyRunOnPlatform(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static Button createToolBarButton(String text, String tooltip, EventHandler<ActionEvent> onAction) {
        Button button = new Button(text);
        button.setFont(ApplicationConstants.FONTS_AWESOME);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(onAction);
        return button;
    }

    public static Alert createAlert(Alert.AlertType alertType, String title, String header, String content, Stage owner) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (owner == null) {
            Scene scene = new Scene(alert.getDialogPane());
            ThemeManager.getInstance().addCss(scene);
            Stage stage = new Stage();
            stage.setScene(scene);
            alert.initOwner(stage.getOwner());
            return alert;
        }

        alert.initOwner(owner.getScene().getWindow());
        return alert;
    }
}
