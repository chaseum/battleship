package com.chase.battleship.gui;

import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;
import java.util.Stack;

public class ScreenManager {

    private final Stage stage;
    private final Map<ScreenId, BaseScreen> screens = new EnumMap<>(ScreenId.class);
    private final Stack<ScreenId> navStack = new Stack<>();

    // GUI-level game state
    private GuiGameSession.Mode plannedMode = GuiGameSession.Mode.CLASSIC_VS_AI;
    private GuiGameSession currentSession;
    private String pendingJoinCode;

    public ScreenManager(Stage stage) {
        this.stage = stage;
    }

    // -------- session plumbing --------
    public GuiGameSession.Mode getPlannedMode() {
        return plannedMode;
    }

    public void setPlannedMode(GuiGameSession.Mode plannedMode) {
        this.plannedMode = plannedMode;
    }

    public GuiGameSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(GuiGameSession session) {
        this.currentSession = session;
    }

    public String getPendingJoinCode() {
        return pendingJoinCode;
    }

    public void setPendingJoinCode(String pendingJoinCode) {
        this.pendingJoinCode = pendingJoinCode;
    }

    public void clearCurrentSession() {
        this.currentSession = null;
    }

    // -------- screen navigation --------
    public void show(ScreenId id) {
        if (!screens.containsKey(id)) {
            screens.put(id, createScreen(id));
        }

        BaseScreen screen = screens.get(id);
        Region root = screen.getRoot();

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1280, 720);
            scene.setCursor(javafx.scene.Cursor.CROSSHAIR);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        if (!navStack.isEmpty() && navStack.peek() != id) {
            navStack.push(id);
        } else if (navStack.isEmpty()) {
            navStack.push(id);
        }

        screen.onShow();
    }

    public void goBack() {
        if (navStack.size() <= 1) {
            show(ScreenId.TITLE);
            return;
        }
        navStack.pop();
        ScreenId prev = navStack.peek();
        show(prev);
    }

    private BaseScreen createScreen(ScreenId id) {
        return switch (id) {
            case TITLE -> new TitleScreen(this);
            case SINGLE_PLAYER_SELECT -> new SinglePlayerScreen(this);
            case MULTI_PLAYER_SELECT -> new MultiPlayerScreen(this);
            case HOST_MODE_SELECT -> new HostModeScreen(this);
            case JOIN_CODE -> new JoinCodeScreen(this);
            case SETUP -> new SetupScreen(this);
            case PLAYING -> new PlayingScreen(this);
            case WIN -> new WinScreen(this);
            case LOSE -> new LoseScreen(this);
        };
    }
}
