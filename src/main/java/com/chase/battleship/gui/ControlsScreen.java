package com.chase.battleship.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Simple controls reference showing keys and basic actions.
 */
public class ControlsScreen extends BaseScreen {

    private static final class ControlSlide {
        final String key;
        final String title;
        final String desc;

        ControlSlide(String key, String title, String desc) {
            this.key = key;
            this.title = title;
            this.desc = desc;
        }
    }

    private final BorderPane root;
    private final Label keyLabel;
    private final Label titleLabel;
    private final Label descLabel;
    private final java.util.List<ControlSlide> slides;
    private int index = 0;

    public ControlsScreen(ScreenManager manager) {
        super(manager);

        slides = java.util.List.of(
                new ControlSlide("Arrows / Enter", "Navigate", "Move through menus and confirm choices."),
                new ControlSlide("Mouse", "Place & Fire", "Click to place ships or fire shots."),
                new ControlSlide("R", "Rotate", "Rotate ships during setup."),
                new ControlSlide("1 / 2 / 3", "Abilities (Neo-Retro)", "Sonar / Multishot / EMP."),
                new ControlSlide("Esc", "Settings / Back", "Open settings or go back.")
        );

        Label header = new Label("Controls");
        header.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 26px;");

        keyLabel = new Label();
        keyLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 22px;");
        titleLabel = new Label();
        titleLabel.setStyle("-fx-text-fill: #c0f0ff; -fx-font-size: 16px;");
        descLabel = new Label();
        descLabel.setStyle("-fx-text-fill: #e0f4ff; -fx-font-size: 13px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(520);

        VBox slideBox = new VBox(8, keyLabel, titleLabel, descLabel);
        slideBox.setAlignment(Pos.CENTER);

        Button left = new Button("<");
        Button right = new Button(">");
        left.setOnAction(e -> prev());
        right.setOnAction(e -> next());

        HBox arrows = new HBox(12, left, slideBox, right);
        arrows.setAlignment(Pos.CENTER);
        arrows.setPadding(new Insets(10));

        Region demo = new Region();
        demo.setPrefSize(360, 220);
        demo.setMinSize(320, 180);
        demo.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(140,220,255,0.4); -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label hint = new Label("Left/Right or A/D to browse. Esc to go back.");
        hint.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 11px;");

        VBox center = new VBox(12, header, arrows, demo, hint);
        center.setAlignment(Pos.CENTER);

        root = new BorderPane(center);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(#0b2a42, #03121f);");

        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                manager.goBack();
                return;
            }
            switch (e.getCode()) {
                case LEFT, A -> prev();
                case RIGHT, D -> next();
                default -> { }
            }
        });

        updateSlide();
    }

    @Override
    public Region getRoot() {
        return root;
    }

    @Override
    public void onShow() {
        updateSlide();
        root.requestFocus();
    }

    private void next() {
        index = (index + 1) % slides.size();
        updateSlide();
    }

    private void prev() {
        index = (index - 1 + slides.size()) % slides.size();
        updateSlide();
    }

    private void updateSlide() {
        ControlSlide slide = slides.get(index);
        keyLabel.setText(slide.key);
        titleLabel.setText(slide.title);
        descLabel.setText(slide.desc);
    }
}
