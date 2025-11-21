package com.chase.battleship.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Full-screen overlay with a looping progress bar for scene transitions and waits.
 */
public class LoadingOverlay extends StackPane {

    private final Label messageLabel;
    private final ProgressBar progressBar;

    public LoadingOverlay() {
        setVisible(false);
        setMouseTransparent(true);
        setPickOnBounds(true);
        setStyle("-fx-background-color: rgba(0, 6, 16, 0.75);");

        messageLabel = new Label("Loading...");
        messageLabel.setStyle("-fx-text-fill: #dff6ff; -fx-font-size: 20px; -fx-font-weight: bold;");

        progressBar = new ProgressBar(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressBar.setPrefWidth(240);
        progressBar.setStyle("-fx-accent: #2fe3ff;");

        VBox centerBox = new VBox(8, messageLabel);
        centerBox.setAlignment(Pos.CENTER);

        StackPane bottomRight = new StackPane(progressBar);
        bottomRight.setAlignment(Pos.BOTTOM_RIGHT);
        bottomRight.setPadding(new Insets(0, 8, 8, 0));

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(22));
        layout.setCenter(centerBox);
        layout.setBottom(bottomRight);

        layout.setStyle("-fx-background-color: rgba(0, 3, 10, 0.55); -fx-border-color: #35f0ff; -fx-border-width: 1;");

        getChildren().add(layout);
    }

    public void show(String message) {
        messageLabel.setText(message);
        if (!isVisible()) {
            setOpacity(0);
            setVisible(true);
            setMouseTransparent(false);
            fade(0, 1, 180).play();
        } else {
            setMouseTransparent(false);
        }
    }

    public void hide() {
        if (!isVisible()) return;
        FadeTransition ft = fade(getOpacity(), 0, 160);
        ft.setOnFinished(e -> {
            setVisible(false);
            setMouseTransparent(true);
        });
        ft.play();
    }

    /**
     * Quick, timed flash for regular scene transitions.
     */
    public void flash(String message, Duration minimumDuration) {
        show(message);
        PauseTransition pause = new PauseTransition(minimumDuration);
        pause.setOnFinished(e -> hide());
        pause.play();
    }

    /**
     * Slightly longer helper used when swapping full screens. Keeps the progress
     * bar visible long enough to mask buffering or layout thrash.
     */
    public void transition(String message) {
        show(message);
        PauseTransition pause = new PauseTransition(Duration.millis(520));
        pause.setOnFinished(e -> hide());
        pause.playFromStart();
    }

    private FadeTransition fade(double from, double to, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), this);
        ft.setFromValue(from);
        ft.setToValue(to);
        return ft;
    }
}
