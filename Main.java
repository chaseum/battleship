import javax.swing.*;
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
		frame.add(new Title(frame), "TITLE");
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		// new ClientWaiting(frame));
	}
}
