
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class Title extends JPanel implements ActionListener, KeyListener {

	JButton join;
	JButton host;
	JButton exit;
	JFrame frame;
	Timer timer;
	int time;
	boolean joinSelected;

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == timer) {
			time++;
			repaint();
			// System.out.println(time);
			if (e.getSource() == join) {
				join.setBackground(Helper.interpolate(Helper.buttonBg, Helper.highlighted,
						(double) time % Helper.highlightSteps / Helper.highlightSteps));
				// System.out.println(Helper.interpolate(Helper.buttonBg, Helper.highlighted,
				// (double)time%Helper.highlightSteps/Helper.highlightSteps));
			} else if (e.getSource() == host) {
				host.setBackground(Helper.interpolate(Helper.buttonBg, Helper.highlighted,
						(double) time % Helper.highlightSteps / Helper.highlightSteps));

			}

		}
	}

	public Title(JFrame frame) {
		this.frame = frame;
		timer = new Timer(75, this);
		System.out.println(frame.size());
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JLabel label = new JLabel("NEO-RETRO BATTLESHIP");
		join = new JButton("Join Existing Game");
		host = new JButton("Host New Game");
		exit = new JButton("Exit");
		exit.setPreferredSize(new Dimension(Helper.fixWidth(450), Helper.fixHeight(150)));
		join.setPreferredSize(new Dimension(Helper.fixWidth(750), Helper.fixHeight(150)));
		host.setPreferredSize(new Dimension(Helper.fixWidth(750), Helper.fixHeight(150)));
		label.setFont(Helper.titleFont);
		label.setForeground(new Color(235, 235, 235));
		join.setForeground(new Color(235, 235, 235));
		host.setForeground(new Color(235, 235, 235));
		exit.setForeground(new Color(235, 235, 235));

		join.setFont(Helper.buttonFont);
		host.setFont(Helper.buttonFont);
		exit.setFont(Helper.buttonFont);

		join.setBackground(Helper.buttonBg);
		host.setBackground(Helper.buttonBg);
		exit.setBackground(Helper.buttonBg);
		((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "TITLE");

		join.addActionListener(e -> {
			// stops anims + removes key listeners
			if (timer != null) {
				timer.stop();
			}
			frame.getContentPane().remove(this);
			frame.getContentPane().add(new ClientWaiting(frame), "CLIENT_WAIT");
			((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "CLIENT_WAIT"); // shows join
			// screen
			frame.getContentPane().revalidate();
			frame.getContentPane().repaint();
		});
		host.addActionListener(e -> {
			if (timer != null) {
				timer.stop();
			}
			frame.getContentPane().remove(this);
			frame.getContentPane().add(new Host(frame), "HOST");
			((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "HOST"); // shows join
			// screen
			frame.getContentPane().revalidate();
			frame.getContentPane().repaint();
		});
		exit.addActionListener(e -> {
			if (Helper.confirmExit(this))
				System.exit(0);
		});

		timer = new Timer(16, evt -> {
			time++;
			repaint();
		});

		join.setFocusable(false);
		host.setFocusable(false);
		exit.setFocusable(false);

		this.setBackground(Helper.bgColor);

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		this.add(label, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;

		c.insets = new Insets(Helper.fixHeight(600), Helper.fixWidth(120), Helper.fixHeight(0), Helper.fixHeight(60));
		this.add(join, c);

		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(Helper.fixHeight(600), Helper.fixWidth(60), Helper.fixHeight(0), Helper.fixHeight(120));
		this.add(host, c);

		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(Helper.fixHeight(20), Helper.fixWidth(60), Helper.fixHeight(0), Helper.fixHeight(100));
		this.add(exit);
		exit.setBounds(frame.getWidth() - 200, frame.getHeight() - 120, 160, 50);

		frame.add(this);
		frame.addKeyListener(this);
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		// Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		// cursorImg, new Point(0, 0), "blank cursor");
		// Set the blank cursor to the JFrame.
		// frame.getContentPane().setCursor(blankCursor);
		join.setFocusable(true);
		host.setFocusable(true);
		exit.setFocusable(true);
		addKeyListener(this);
		requestFocusInWindow();

		joinSelected = true;
		frame.pack();
		timer.start();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		System.out.println(frame.size());
		frame.setVisible(true);

	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		System.out.println(e);

		System.out.println(KeyEvent.getKeyText(e.getKeyCode()));
		String key = KeyEvent.getKeyText(e.getKeyCode());
		if (key.equals("Right") || key.equals("Left")) {
			if (joinSelected) {
				join.setBackground(Helper.buttonBg);
			} else {
				host.setBackground(Helper.buttonBg);

			}
			joinSelected = !joinSelected;
			time = 0;
		}
		if (key.equals("Enter")) {
			frame.remove(this);
			frame.removeKeyListener(this);
			// this.removeAll();
			timer.stop();
			if (joinSelected) {
				new ClientWaiting(frame);
			} else {
				System.out.println("joinselected " + joinSelected);
				new Host(frame);
			}
		}

	}

	public void keyReleased(KeyEvent e) {
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(null, 400, 400, null);
	}

}
