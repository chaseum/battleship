
import javax.swing.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Host extends JPanel implements ActionListener {

	private ScreenState state;
	private JFrame frame;
	private JLabel code;
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
		loginScreen();
		back = new JButton("Back");
		back.setFocusable(false);
		back.addActionListener(e -> {
			goTitle();
		});
		add(back);
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
			ServerSocket server = new ServerSocket(0); // free port
			port = server.getLocalPort();
			String ip = InetAddress.getLocalHost().getHostAddress();
			code.setText(ip + ":" + port);
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
		// once accepted

	}

	public void loginScreen() {
		timer = new Timer(75, this);
		System.out.println("login screen started :3");
		state = ScreenState.CONNECTING;
		// ServerSocket server=new ServerSocket(20000);

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
		frame.add(this);
		frame.setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			if (client != null) {
				this.removeAll();
				frame.remove(this);
				timer.stop();
				frame.getContentPane().remove(this);
				frame.getContentPane().add(new SetUp(frame, client, true), "SET_UP");
				((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "CLIENT_WAIT"); // shows
																												// setup
				// screen
				frame.getContentPane().revalidate();
				frame.getContentPane().repaint();
				// frame.removeAll();
			}
		}
	}
}
