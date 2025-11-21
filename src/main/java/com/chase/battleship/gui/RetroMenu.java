package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple retro-style text menu that supports mouse hover and arrow/enter navigation.
 */
public class RetroMenu extends VBox {

    public record Option(String label, Runnable action) {}

    private final List<Option> options = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private int selectedIndex = 0;

    public RetroMenu(List<Option> menuOptions) {
        super(10);
        setAlignment(Pos.CENTER_LEFT);
        setPickOnBounds(false);
        getStyleClass().add("retro-menu");
        this.options.addAll(menuOptions);
        buildLabels();
        refreshLabels();
    }

    private void buildLabels() {
        labels.clear();
        getChildren().clear();
        for (int i = 0; i < options.size(); i++) {
            final int idx = i;
            Label l = new Label();
            l.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 22px; -fx-font-family: 'Consolas';");
            l.setOnMouseEntered(e -> setSelectedIndex(idx));
            l.setOnMouseClicked(e -> activate(idx));
            labels.add(l);
            getChildren().add(l);
        }
    }

    private void setSelectedIndex(int idx) {
        idx = Math.max(0, Math.min(idx, options.size() - 1));
        selectedIndex = idx;
        refreshLabels();
    }

    private void refreshLabels() {
        for (int i = 0; i < labels.size(); i++) {
            Label l = labels.get(i);
            Option opt = options.get(i);
            boolean selected = (i == selectedIndex);
            l.setText((selected ? "> " : "  ") + opt.label());
            l.setOpacity(selected ? 1.0 : 0.75);
        }
    }

    private void activate(int idx) {
        if (idx < 0 || idx >= options.size()) return;
        options.get(idx).action().run();
    }

    /**
     * Handles arrow/WASD + enter navigation for this menu.
     * @return true if the key was consumed by the menu
     */
    public boolean handleKey(KeyCode code) {
        switch (code) {
            case UP, W -> {
                setSelectedIndex(selectedIndex - 1);
                return true;
            }
            case DOWN, S -> {
                setSelectedIndex(selectedIndex + 1);
                return true;
            }
            case ENTER, SPACE -> {
                activate(selectedIndex);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public void focusFirst() {
        setSelectedIndex(0);
    }
}
