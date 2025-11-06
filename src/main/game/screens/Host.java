package src.main.game.screens;

import javax.swing.*;

import src.main.game.logic.Helper;
import src.main.game.logic.ScreenState;
import src.main.game.server.Rendezvous;
import src.main.game.server.RendezvousConfig;

import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Host extends JPanel implements ActionListener {

	private ScreenState state;
	private JFrame frame;
	private JLabel code;
	private String gameCode;
	private JLabel codeExplan;
	private JLabel wait;
	private Socket client;
	private BufferedReader br;
	private PrintWriter pw;
	private int port;
	private ServerSocket server;
	private Timer timer;
	private JButton back;
	private Thread acceptThread;

	public Host(JFrame frame) {
		this.frame = frame;
		this.setBackground(Helper.bgColor);
		loginScreen();
		back = new JButton("Back");
		back.setFocusable(false);
		back.addActionListener(event -> goTitle());
		add(back);
		if (getParent() == null) {
			frame.getContentPane().add(this, "HOST");
		}
		connecting();
	}

	private void goTitle() {
		try {
			if (client != null && !client.isClosed())
				client.close();
		} catch (IOException ignored) {
		}
		try {
			if (server != null && !server.isClosed())
				server.close();
		} catch (IOException ignored) {
		}
		if (acceptThread != null && acceptThread.isAlive())
			acceptThread.interrupt();
		Helper.showCard(frame, "TITLE");
	}

	private void setupStreams(Socket s) throws IOException {
		s.setTcpNoDelay(true);
		s.setSoTimeout(0);
		pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8),
				true);
		br = new BufferedReader(new InputStreamReader(s.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
	}

	public void connecting() {
		try {
			server = new ServerSocket(0); // free port
			port = server.getLocalPort();
			// Generate a 6-letter code (A-Z) using Helper
			gameCode = Helper.makeCode(6);
			registerCode(gameCode, port);
			code.setText("Game code: " + gameCode);
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Failed to open server socket", "Error", JOptionPane.ERROR_MESSAGE);
		}
		System.out.println("socket tried to connect :p");
		repaint();
		acceptThread = new Thread(() -> {
			try {
				client = server.accept();
				setupStreams(client);
				// handshake
				pw.println("hi client :3");
				String reply = br.readLine();
				if (!"hi host :3".equals(reply))
					throw new IOException("bad handshake");
				SwingUtilities.invokeLater(() -> {
					// go to set up or playing screen
					frame.getContentPane().add(new SetUp(frame, client, true), "SET_UP");
					Helper.showCard(frame, "SET_UP");
				});
			} catch (IOException ioe) {
				if (!Thread.currentThread().isInterrupted())
					ioe.printStackTrace();
			}
		});
		acceptThread.start(); // Start the accept thread
	}

	// tell rendezvous our port
	private void registerCode(String code, int port) {
		new Thread(() -> {
			try (Socket s = new Socket(RendezvousConfig.HOST, RendezvousConfig.PORT);
					PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
				out.println("REG " + code + " " + port);
			} catch (IOException ignored) {
			}
		}).start();
	}

	public void loginScreen() {
		timer = new Timer(75, (ActionListener) this);
		timer.start();
		state = ScreenState.CONNECTING;
		// ServerSocket server=new ServerSocket(20000);

		setOpaque(true);
		this.setBackground(Helper.bgColor);

		wait = new JLabel("Waiting for Another Player");
		code = new JLabel("Game Code: " + Helper.ipToCode(Helper.ipAddress));

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		wait.setFont(Helper.titleFont);
		code.setFont(Helper.normalTxtFont);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.CENTER;

		this.add(wait, c);
		c.gridy = 1;
		c.insets = new Insets(Helper.fixHeight(200), 0, 0, 0);
		this.add(code, c);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
	}
}
