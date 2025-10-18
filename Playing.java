
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.Timer;

public class Playing extends JPanel implements ActionListener, KeyListener {
	Object[][] board;
	Object[][] oppBoard;
	Set<Ship> ships;
	boolean isClient;
	boolean isYourTurn;
	Socket socket;
	String name;
	JFrame frame;

	BufferedReader br;
	PrintWriter pw;

	JLabel pointLabel;
	JLabel nameLabel;
	JLabel oppLabel;
	Timer timer;
	int targetX;
	int targetY;

	public Playing(Object[][] board, boolean isClient, Socket socket, String name, JFrame frame) {
		this.board = board;
		oppBoard = new Object[10][10];
		this.isClient = isClient;
		this.socket = socket;
		this.name = name;
		this.frame = frame;
		makeScreen();
		drawShips();
		startPlaying();
	}

	public void connecting() {
		try {
			InputStreamReader isr = new InputStreamReader(socket.getInputStream());
			br = new BufferedReader(isr);
			pw = new PrintWriter(socket.getOutputStream(), true);

			pw.println(name);
			nameLabel = new JLabel(name);
			oppLabel = new JLabel(br.readLine());
			br.readLine();
		} catch (Exception e) {

		}
	}

	public void makeScreen() {
		this.setLayout(new GridBagLayout());
		nameLabel = new JLabel(name);
		oppLabel = new JLabel("opp");
		pointLabel = new JLabel("$0");
		timer = new Timer(75, this);
		ships = new HashSet<Ship>();

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = c.NORTHWEST;
		c.weightx = .1;
		c.weighty = .1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0, Helper.fixWidth2(84), 0, 0);
		nameLabel.setFont(Helper.normalTxtFont);
		this.add(nameLabel, c);

		c.anchor = c.NORTH;
		c.weightx = .1;
		c.weighty = .1;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0, Helper.fixWidth2(-175), 0, 0);
		pointLabel.setFont(Helper.normalTxtFont);
		this.add(pointLabel, c);

		c.anchor = c.NORTHEAST;
		c.weightx = .1;
		c.weighty = .1;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 0, Helper.fixWidth2(84));
		oppLabel.setFont(Helper.normalTxtFont);
		this.add(oppLabel, c);

		frame.add(this);
		frame.addKeyListener(this);
		frame.requestFocus();
		timer.start();
		frame.setVisible(true);
	}

	public void drawShips() {
		for (Object[] arr : board) {
			for (Object obj : arr) {
				if (obj != null) {
					ships.add((Ship) obj);
				}
			}
		}
		System.out.println(ships.size());
	}

	public void drawTarget(Graphics g) {
		if (targetX != -1) {
			g.setColor(Color.YELLOW);
			g.fillRect(targetX * Helper.cellWidth + Helper.fixWidth2(852),
					Helper.fixHeight2(60) + targetY * Helper.cellWidth, Helper.cellWidth, Helper.cellWidth);
			g.setColor(Color.BLACK);
		}
	}

	public void startPlaying() {
		isYourTurn = true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			if (!isYourTurn) {
				try {
					String message = br.readLine();
					int x = message.charAt(0) - '0';
					int y = message.charAt(2) - '0';
					Object target = board[x][y];
					if (target == null) {
						pw.println("MISS");
						board[x][y] = new Integer(0);

					} else if (ships.contains(target)) {
						pw.println("HIT");
						board[x][y] = new Integer(-1);
						Ship ship = (Ship) target;
						ship.updateShip(ship.getX() - x + ship.getY() - y);

					}

				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		repaint();
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyPressed(KeyEvent e) {
		String key = KeyEvent.getKeyText(e.getKeyCode());
		if (isYourTurn) {
			if (key.equals("Down")) {

				targetY++;

			}
			if (key.equals("Up")) {

				targetY--;

			}

			if (key.equals("Left")) {
				targetX--;

			}
			if (key.equals("Right")) {
				targetX++;

			}
			if (key.equals("Enter")) {
				pw.println(targetX + "," + targetY);
				try {
					String message = br.readLine();
					if (message.equals("HIT")) {
						oppBoard[targetX][targetY] = 1;
					} else {
						oppBoard[targetX][targetY] = 0;
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}

			}
		}
		targetX = Helper.clamp(0, targetX, 9);
		targetY = Helper.clamp(0, targetY, 9);

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.fillRect(Helper.fixWidth2(84), Helper.fixHeight2(60), Helper.fixWidth2(600), Helper.fixHeight2(600));
		g.fillRect(Helper.fixWidth2(852), Helper.fixHeight2(60), Helper.fixWidth2(600), Helper.fixHeight2(600));
		if (isYourTurn) {
			System.out.println("idk");
			drawTarget(g);
		}
		if (ships != null) {
			for (Ship ship : ships) {
				// System.out.println("ship");
				g.setColor(Color.BLUE);
				g.fillRect(ship.getX() * Helper.cellWidth + Helper.fixWidth2(85),
						Helper.fixHeight2(61) + Helper.cellWidth * ship.getY(),
						-2 + Helper.cellWidth * (ship.isHorizontal() ? ship.getLength() : 1),
						-2 + Helper.cellWidth * (ship.isHorizontal() ? 1 : ship.getLength()));
				g.setColor(Color.BLACK);
			}
		}
	}
}
