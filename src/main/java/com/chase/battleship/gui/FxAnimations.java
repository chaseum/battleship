package com.chase.battleship.gui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
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
}
