
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.Timer;
import java.awt.*;

public class ClientWaiting extends JPanel implements ActionListener {

    private JTextField codeField;

    private JFrame frame;
    private Socket server;
    private Timer timer;
    private GridBagConstraints c;
    private JLabel instructions;

    public ClientWaiting(JFrame frame) {
        this.frame = frame;
        loginScreen();

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            fixField();
        }
        if (e.getSource() == codeField) {
            if (codeField.getText().matches("[A-Za-z]{6}")) {
                String ip = Helper.codeToIp(codeField.getText());
                System.out.println(ip);

                try {
                    server = new Socket(ip, 20000);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                this.removeAll();
                frame.remove(this);
                timer.stop();
                codeField.setVisible(false);
                frame.getContentPane().remove(this);
                frame.getContentPane().add(new SetUp(frame, server, true), "SET_UP");
                ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "CLIENT_WAIT"); // shows setup
                // screen
                frame.getContentPane().revalidate();
                frame.getContentPane().repaint();
                //frame.removeAll();

            } else {
                codeField.setText("");
            }
        }

    }

    public void fixField() {
        if (codeField.getText().length() > 6) {
            codeField.setText(codeField.getText().substring(0, 6));

        }
        int textWidth = codeField.getFontMetrics(Helper.buttonFont).stringWidth(codeField.getText());
        int fieldWidth = codeField.getWidth() - 2 * Helper.fieldPadding;
        if (textWidth > fieldWidth || textWidth > 100 && textWidth < fieldWidth) {
            this.setVisible(false);
            this.remove(codeField);
            codeField = buildCodeField();
            codeField.setPreferredSize(new Dimension(textWidth + 2 * Helper.fieldPadding, 100));

            c.gridwidth = 2;
            this.add(codeField, c);

            codeField.requestFocus();
            this.setVisible(true);

            codeField.requestFocus();

        }
    }

    public JTextField buildCodeField() {
        codeField = new JTextField(codeField != null ? codeField.getText() : "pppppp");

        codeField.setFont(Helper.buttonFont);
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.addActionListener(this);
        codeField.setBackground(Helper.buttonBg);
        codeField.setPreferredSize(new Dimension(100, 100));
        return codeField;

    }

    public void loginScreen() {
        System.out.println("logging in");
        JLabel label = new JLabel("Input Game Code");
        instructions = new JLabel("Press Enter to Submit");
        this.setBackground(Helper.bgColor);

        timer = new Timer(1, this);
        this.setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        //c.insets=new Insets(500,120,0,120);

        this.add(label, c);
        c.gridy = 1;
        c.insets = new Insets(Helper.fixHeight(200), 0, 0, 0);

        this.add(instructions, c);
        c.gridy = 2;
        label.setFont(Helper.titleFont);
        instructions.setFont(Helper.normalTxtFont);
        codeField = buildCodeField();
        c.insets = new Insets(Helper.fixHeight(200), 0, 0, 0);
        this.add(codeField, c);
        frame.add(this);
        codeField.requestFocus();
        frame.setVisible(true);
        timer.start();
    }
}
