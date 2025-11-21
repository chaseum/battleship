package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DisconnectedScreen extends BaseScreen {

    private final VBox root = new VBox(15);

    public DisconnectedScreen(ScreenManager screenManager) {
        super(screenManager);

        Label label = new Label("Player disconnected");
        Button title = new Button("Back to Title");
        Button quit = new Button("Quit");

        title.setOnAction(e -> {
            if (screenManager.getCurrentSession() != null) {
                screenManager.getCurrentSession().close();
            }
            screenManager.clearCurrentSession();
            screenManager.show(ScreenId.TITLE);
        });
        quit.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(label, title, quit);
        root.setAlignment(Pos.CENTER);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
