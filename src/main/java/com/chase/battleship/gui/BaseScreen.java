package com.chase.battleship.gui;

import javafx.scene.layout.Region;

public abstract class BaseScreen {

    protected final ScreenManager manager;

    protected BaseScreen(ScreenManager manager) {
        this.manager = manager;
    }

    public abstract Region getRoot();

    public void onShow() {
    }
}