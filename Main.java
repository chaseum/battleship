import javax.swing.*;
import java.awt.*;

public class Main {
	public static void main(String... args) {
		new Main();
	}

	public Main() {
		JFrame frame = new JFrame("Neo-Retro Battleship");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new CardLayout());
		frame.add(new Title(frame), "TITLE");
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// new ClientWaiting(frame));

	}
}
