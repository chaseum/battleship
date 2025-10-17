
import javax.swing.*;
import java.net.*;
import java.awt.BorderLayout;
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
    private ServerSocket server;
    private Timer timer;

    public Host(JFrame frame) {
        this.frame = frame;
        loginScreen();
        connecting();
    }

    public void connecting() {
        try {
            ServerSocket server = new ServerSocket(20000);
            System.out.println("AA");
            repaint();
            (new acceptT()).start();
            //once accepted

        } catch (Exception e) {

        }
    }

    public void loginScreen() {
        timer = new Timer(75, this);
        System.out.println("AAAAA");
        state = ScreenState.CONNECTING;
        //ServerSocket server=new ServerSocket(20000);

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
                ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "CLIENT_WAIT"); // shows setup
                // screen
                frame.getContentPane().revalidate();
                frame.getContentPane().repaint();
                //frame.removeAll();
            }
        }
    }

    class acceptT extends Thread {

        public void run() {
            try {
                client = server.accept();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
