
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.awt.*;

public class ClientWaiting extends JPanel implements ActionListener {

	private JTextField codeField;
	private JFrame frame;
	private Socket server;
	private GridBagConstraints c;
	private JLabel instructions;
	private JButton back;

	public ClientWaiting(JFrame frame) {
		this.frame = frame;
		buildUI();
	}

	private void buildUI() {
		setBackground(Helper.bgColor);
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.CENTER;

		JLabel label = new JLabel("Input Game Code");
		label.setFont(Helper.titleFont);
		add(label, c);

		c.gridy = 1;
		c.insets = new Insets(Helper.fixHeight(200), 0, 0, 0);
		instructions = new JLabel("Press Enter to Submit");
		instructions.setFont(Helper.normalTxtFont);
		add(instructions, c);

		c.gridy = 2;
		codeField = buildCodeField();
		c.insets = new Insets(Helper.fixHeight(200), 0, 0, 0);
		add(codeField, c);

		c.gridy = 3;
		c.insets = new Insets(Helper.fixHeight(60), 0, 0, 0);
		back = new JButton("Back");
		back.setFocusable(true);
		back.addActionListener(e -> goTitle());
		add(back, c);

		// Register this panel in the frame's CardLayout (if not already present), and
		// show it.
		if (getParent() == null) {
			frame.getContentPane().add(this, "CLIENT_WAIT");
		}
		((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "CLIENT_WAIT");
		frame.getContentPane().revalidate();
		frame.getContentPane().repaint();

		codeField.requestFocusInWindow();
	}

	private void goTitle() {
		try {
			if (server != null && !server.isClosed())
				server.close();
		} catch (IOException ignored) {
		}
		Helper.showCard(frame, "TITLE");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == codeField) { // user pressed Enter
			String code = codeField.getText().trim(); // already sanitized by our filter
			if (code.length() == 6) {
				try {
					String ip = Helper.codeToIp(code);
					int port = 5000;// must return an int

					server = new Socket();
					server.connect(new InetSocketAddress(ip, port), 5000); // 5s timeout

					// Move to setup screen
					frame.getContentPane().add(new SetUp(frame, server, true), "SET_UP");
					((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "SET_UP");
					frame.getContentPane().revalidate();
					frame.getContentPane().repaint();
				} catch (NumberFormatException nfe) {
					// If codeToPort produced something non-numeric
					JOptionPane.showMessageDialog(this, "Invalid game code (bad port). Try again.");
					codeField.setText("");
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "Could not connect. Check the code or try again.");
				}
			} else {
				// Shouldn't happen due to filter, but guard anyway
				JOptionPane.showMessageDialog(this, "Game code must be 6 letters (A–Z).");
				codeField.setText("");
			}
		}
	}

	private JTextField buildCodeField() {
		JTextField tf = new JTextField();
		tf.setFont(Helper.buttonFont);
		tf.setHorizontalAlignment(JTextField.CENTER);
		tf.setBackground(Helper.buttonBg);
		tf.setColumns(6); // stable width; no dynamic resizing while typing
		tf.addActionListener(this); // Enter submits

		// Install filter: allow only A–Z, limit to 6, uppercase automatically
		AbstractDocument doc = (AbstractDocument) tf.getDocument();
		doc.setDocumentFilter(new codeFilter(6));
		return tf;
	}

	class codeFilter extends DocumentFilter {
		private final int maxLen;

		codeFilter(int maxLen) {
			this.maxLen = maxLen;
		}

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {
			if (string == null)
				return;
			int room = maxLen - fb.getDocument().getLength();
			if (room <= 0)
				return;
			if (string.length() > room)
				string = string.substring(0, room);
			super.insertString(fb, offset, string, attr);
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {
			String current = fb.getDocument().getText(0, fb.getDocument().getLength());
			String before = current.substring(0, offset);
			String after = current.substring(offset + length);
			String cleaned = (text == null ? "" : text);
			String next = before + cleaned + after;
			if (next.length() > maxLen) {
				next = next.substring(0, maxLen);
			}
			super.replace(fb, 0, fb.getDocument().getLength(), next, attrs);
		}
	}
}