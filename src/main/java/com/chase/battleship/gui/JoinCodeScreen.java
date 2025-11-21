package com.chase.battleship.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * GUI placeholder for joining a hosted game.
 * Currently only shows a compact text field and an informational message.
 */
public class JoinCodeScreen extends BaseScreen {

    private final VBox root;
    private final Label errorLabel;
    private final Button joinBtn;
    private final TextField codeField;
    private final RadioButton classicBtn;
    private final RadioButton neoBtn;

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

        classicBtn = new RadioButton("Classic");
        classicBtn.setStyle("-fx-text-fill: #f0f0f0;");
        classicBtn.setToggleGroup(modeGroup);
        classicBtn.setSelected(true);

        neoBtn = new RadioButton("Neo-Retro");
        neoBtn.setStyle("-fx-text-fill: #f0f0f0;");
        neoBtn.setToggleGroup(modeGroup);

        HBox modeRow = new HBox(12, modeLabel, classicBtn, neoBtn);
        modeRow.setAlignment(Pos.CENTER);

        codeField = new TextField();
        codeField.setPromptText("Enter join code");
        codeField.setMaxWidth(250); // <-- much smaller than full width

        joinBtn = new Button("Join Game");
        Button backBtn = new Button("Back");

        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff6666;");

        joinBtn.setOnAction(e -> {
            errorLabel.setText("");
            String code = codeField.getText().trim();
            GuiGameSession.Mode mode = classicBtn.isSelected()
                    ? GuiGameSession.Mode.CLASSIC_ONLINE_CLIENT
                    : GuiGameSession.Mode.NEORETRO_ONLINE_CLIENT;

            joinBtn.setDisable(true);
            manager.setPlannedMode(mode);
            manager.setPendingJoinCode(code);
            manager.clearCurrentSession();

            Thread joinThread = new Thread(() -> {
                try {
                    GuiGameSession session = new GuiGameSession(mode, code);
                    session.waitForPeerReady();
                    Platform.runLater(() -> {
                        manager.setCurrentSession(session);
                        manager.show(ScreenId.SETUP);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        errorLabel.setText("Failed to join: " + ex.getMessage());
                        joinBtn.setDisable(false);
                    });
                }
            }, "join-connector");

            joinThread.setDaemon(true);
            joinThread.start();
        });

        backBtn.setOnAction(e -> {
            if (manager.getCurrentSession() != null) {
                manager.getCurrentSession().close();
            }
            manager.clearCurrentSession();
            manager.goBack();
        });

        root.getChildren().addAll(title, info, modeRow, codeField, joinBtn, backBtn, errorLabel);
    }

    @Override
    public Region getRoot() {
        return root;
    }

    @Override
    public void onShow() {
        joinBtn.setDisable(false);
        errorLabel.setText("");
        codeField.clear();
        if (manager.getCurrentSession() != null) {
            manager.getCurrentSession().close();
        }
        manager.clearCurrentSession();
    }
}
