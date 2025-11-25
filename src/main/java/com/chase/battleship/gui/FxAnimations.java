package com.chase.battleship.gui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Tiny helper methods for lightweight text animations.
 */
public final class FxAnimations {

    private static final String MARQUEE_KEY = "fx-marquee";
    private static final String TYPEWRITER_KEY = "fx-typewriter";

    private FxAnimations() {
    }

    public static void marqueeIfNeeded(Label label, double visibleWidth) {
        label.applyCss();
        label.layout();
        label.setMinWidth(visibleWidth);
        label.setPrefWidth(visibleWidth);
        label.setMaxWidth(visibleWidth);

        Text measure = new Text(label.getText());
        measure.setFont(label.getFont());
        double textWidth = measure.getBoundsInLocal().getWidth();
        if (textWidth <= visibleWidth) {
            stopMarquee(label);
            label.setClip(null);
            label.setTranslateX(0);
            return;
        }
        startMarquee(label, visibleWidth, textWidth);
    }

    public static void stopMarquee(Label label) {
        Object anim = label.getProperties().remove(MARQUEE_KEY);
        if (anim instanceof Animation animation) {
            animation.stop();
        }
        label.setTranslateX(0);
        label.setClip(null);
    }

    private static void startMarquee(Label label, double visibleWidth, double textWidth) {
        stopMarquee(label);

        Rectangle clip = new Rectangle(visibleWidth, label.getHeight() + 8);
        label.setClip(clip);

        TranslateTransition tt = new TranslateTransition(Duration.seconds(Math.max(5, textWidth / 30)), label);
        tt.setFromX(visibleWidth);
        tt.setToX(-textWidth);
        tt.setInterpolator(Interpolator.LINEAR);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.play();

        label.getProperties().put(MARQUEE_KEY, tt);
    }

    /**
     * Simple typewriter text animation for a Label.
     */
    public static void typewriter(Label label, String text, Duration duration) {
        if (label == null) {
            return;
        }
        stopTypewriter(label);

        String target = text == null ? "" : text;
        if (target.isEmpty()) {
            label.setText("");
            return;
        }

        double perCharMs = duration.toMillis() / Math.max(target.length(), 1);
        Timeline timeline = new Timeline();
        for (int i = 0; i < target.length(); i++) {
            final int index = i;
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(perCharMs * (i + 1)),
                    event -> label.setText(target.substring(0, index + 1)))
            );
        }
        timeline.setOnFinished(e -> label.setText(target));
        timeline.playFromStart();

        label.getProperties().put(TYPEWRITER_KEY, timeline);
    }

    public static void stopTypewriter(Label label) {
        Object anim = label.getProperties().remove(TYPEWRITER_KEY);
        if (anim instanceof Animation animation) {
            animation.stop();
        }
    }
}
