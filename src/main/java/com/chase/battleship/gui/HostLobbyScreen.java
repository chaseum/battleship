package com.chase.battleship.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Waiting room for online hosts. Displays the lobby code and transitions to setup
 * once a client connects.
 */
public class HostLobbyScreen extends BaseScreen {

    private final VBox root;
    private final Label codeLabel;
    private final Label statusLabel;

    private GuiGameSession session;
    private volatile boolean waiting;

    public HostLobbyScreen(ScreenManager manager) {
        super(manager);

        root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #001b29;");

        Label title = new Label("Host Lobby");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px;");

        Label info = new Label("Share this code with your friend. Waiting for them to join...");
        info.setStyle("-fx-text-fill: #cccccc;");
        info.setWrapText(true);
        info.setMaxWidth(420);

        codeLabel = new Label("Generating code...");
        codeLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 28px; -fx-font-weight: bold;");

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #cccccc;");

        Button backBtn = new Button("Cancel");
        backBtn.setOnAction(e -> {
            stopWaiting();
            if (session != null) {
                session.close();
            }
            manager.clearCurrentSession();
            manager.goBack();
        });

        root.getChildren().addAll(title, info, codeLabel, statusLabel, backBtn);
    }

    @Override
    public void onShow() {
        stopWaiting();
        codeLabel.setText("Generating code...");
        statusLabel.setText("Connecting to rendezvous...");

        Thread setupThread = new Thread(() -> {
            try {
                GuiGameSession existingSession = manager.getCurrentSession();
                GuiGameSession preparedSession = existingSession != null
                        ? existingSession
                        : new GuiGameSession(manager.getPlannedMode(), manager.getPendingJoinCode());

                if (existingSession == null) {
                    manager.setCurrentSession(preparedSession);
                }

                Platform.runLater(() -> {
                    session = preparedSession;
                    codeLabel.setText("Lobby Code: " + session.getLobbyCode());
                    statusLabel.setText("Waiting for a player to join...");
                    startWaitingForClient();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    session = null;
                    codeLabel.setText("Lobby Code: --");
                    statusLabel.setText("Failed to host: " + ex.getMessage());
                });
            }
        }, "host-setup");

        setupThread.setDaemon(true);
        setupThread.start();
    }

    private void startWaitingForClient() {
        if (session == null) {
            return;
        }
        stopWaiting();
        waiting = true;

        Thread t = new Thread(() -> {
            try {
                session.waitForPeerReady();
                if (!waiting) {
                    return;
                }
                Platform.runLater(() -> {
                    if (!waiting) return;
                    statusLabel.setText("Player joined! Moving to setup...");
                    manager.show(ScreenId.SETUP);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Connection failed: " + ex.getMessage()));
            }
        }, "host-lobby-wait");
        t.setDaemon(true);
        t.start();
    }

    private void stopWaiting() {
        waiting = false;
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
