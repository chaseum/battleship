package com.chase.battleship.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
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

        Label info = new Label("Select a mode, then enter the lobby code shared by the host.");
        info.setStyle("-fx-text-fill: #cccccc;");

        Label modeLabel = new Label("Mode:");
        modeLabel.setStyle("-fx-text-fill: #f0f0f0;");

        ToggleGroup modeGroup = new ToggleGroup();

        RadioButton classicBtn = new RadioButton("Classic");
        classicBtn.setStyle("-fx-text-fill: #f0f0f0;");
        classicBtn.setToggleGroup(modeGroup);
        classicBtn.setSelected(true);

        RadioButton neoBtn = new RadioButton("Neo-Retro");
        neoBtn.setStyle("-fx-text-fill: #f0f0f0;");
        neoBtn.setToggleGroup(modeGroup);

        HBox modeRow = new HBox(12, modeLabel, classicBtn, neoBtn);
        modeRow.setAlignment(Pos.CENTER);

        TextField codeField = new TextField();
        codeField.setPromptText("Enter join code");
        codeField.setMaxWidth(250); // <-- much smaller than full width

        Button joinBtn = new Button("Join Game");
        Button backBtn = new Button("Back");

        joinBtn.setOnAction(e -> {
            manager.setPendingJoinCode(codeField.getText().trim());
            GuiGameSession.Mode mode = classicBtn.isSelected()
                    ? GuiGameSession.Mode.CLASSIC_ONLINE_CLIENT
                    : GuiGameSession.Mode.NEORETRO_ONLINE_CLIENT;
            manager.setPlannedMode(mode);
            manager.clearCurrentSession();
            manager.show(ScreenId.SETUP);
        });

        backBtn.setOnAction(e -> manager.goBack());

        root.getChildren().addAll(title, info, modeRow, codeField, joinBtn, backBtn);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
