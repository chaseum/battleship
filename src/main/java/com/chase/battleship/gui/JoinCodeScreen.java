package com.chase.battleship.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * GUI placeholder for joining a hosted game.
 * Currently only shows a compact text field and an informational message.
 */
public class JoinCodeScreen extends BaseScreen {

    private final VBox root;

    public JoinCodeScreen(ScreenManager manager) {
        super(manager);

        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #001b29;");

        Label title = new Label("Join Game");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px;");

        Label info = new Label("Network join is not yet wired to the rendezvous server.");
        info.setStyle("-fx-text-fill: #cccccc;");

        TextField codeField = new TextField();
        codeField.setPromptText("Enter join code");
        codeField.setMaxWidth(250); // <-- much smaller than full width

        Button joinBtn = new Button("Join (WIP)");
        Button backBtn = new Button("Back");

        joinBtn.setOnAction(e -> {
            // For now, just show an in-game message; do not start a session.
            info.setText("Joining by code is work-in-progress. Use CLI for online play.");
        });

        backBtn.setOnAction(e -> manager.goBack());

        root.getChildren().addAll(title, info, codeField, joinBtn, backBtn);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
