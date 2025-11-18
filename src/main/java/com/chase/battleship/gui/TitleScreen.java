package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TitleScreen extends BaseScreen {

    private final BorderPane root = new BorderPane();

    public TitleScreen(ScreenManager screenManager) {
        super(screenManager);

        Label title = new Label("NEO-RETRO BATTLESHIP");
        title.getStyleClass().add("title-label");

        Button single = new Button("Start 1 Player Game");
        Button multi  = new Button("Start 2 Player / Online");
        Button quit   = new Button("Quit");

        single.setOnAction(e -> screenManager.show(ScreenId.SINGLE_PLAYER_SELECT));
        multi.setOnAction(e -> screenManager.show(ScreenId.MULTI_PLAYER_SELECT));
        quit.setOnAction(e -> root.getScene().getWindow().hide());

        VBox menu = new VBox(12, single, multi, quit);
        menu.setAlignment(Pos.CENTER);

        root.setCenter(menu);
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.TOP_CENTER);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
