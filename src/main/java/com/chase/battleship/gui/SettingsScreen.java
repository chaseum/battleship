package com.chase.battleship.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsScreen {

    private double volume = 0.5;
    private double speedFactor = 1.0;

    public void show(Stage owner, Runnable onClose) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Settings");

        Slider volumeSlider = new Slider(0, 1, volume);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(0.5);
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> volume = newV.doubleValue());

        Slider speedSlider = new Slider(0.5, 2.0, speedFactor);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.valueProperty().addListener((obs, ov, nv) -> speedFactor = nv.doubleValue());

        Label volLabel = new Label("Volume");
        Label speedLabel = new Label("Game Speed (affects turn delay)");

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> {
            dialog.close();
            if (onClose != null) onClose.run();
        });

        VBox root = new VBox(12, volLabel, volumeSlider, speedLabel, speedSlider, closeBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 320, 240);
        dialog.setScene(scene);
        dialog.show();
    }

    public double getVolume() {
        return volume;
    }

    public double getSpeedFactor() {
        return speedFactor;
    }
}
