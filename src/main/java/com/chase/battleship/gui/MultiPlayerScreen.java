package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MultiPlayerScreen extends BaseScreen {

    private final BorderPane root = new BorderPane();

    public MultiPlayerScreen(ScreenManager screenManager) {
        super(screenManager);

        Label label = new Label("Multiplayer");
        Button host = new Button("Host Game");
        Button join = new Button("Join Game");
        Button back = new Button("Back");

        host.setOnAction(e -> screenManager.show(ScreenId.HOST_MODE_SELECT));
        join.setOnAction(e -> screenManager.show(ScreenId.JOIN_CODE));
        back.setOnAction(e -> screenManager.goBack());

        VBox box = new VBox(10, host, join, back);
        box.setAlignment(Pos.CENTER);

        root.setTop(label);
        BorderPane.setAlignment(label, Pos.TOP_CENTER);
        root.setCenter(box);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
