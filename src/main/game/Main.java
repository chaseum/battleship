package src.main.game;

import javax.swing.*;

import src.main.game.logic.Helper;
import src.main.game.screens.Title;

import java.awt.*;

public class Main {
	public static void main(String... args) {
		new Main();
	}

	public Main() {
		JFrame frame = new JFrame("Neo-Retro Battleship");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (Helper.confirmExit(frame))
					System.exit(0);
			}
		});
		frame.setLayout(new CardLayout());
		frame.getContentPane().add(new Title(frame), "TITLE");
		((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "TITLE");
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
