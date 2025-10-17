
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.awt.*;
import java.awt.GridBagConstraints;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

public class SetUp extends JPanel implements ActionListener, KeyListener {

    JFrame frame;
    Socket server;

    JTextField codeField;
    Timer timer;
    GridBagConstraints c;

    JTextArea instructions;

    BufferedReader br;
    PrintWriter pw;
    boolean waitingForName;
    boolean donePlacing;
    boolean horizontal;
    boolean isClient;
    JTextField nameField;
    JLabel name;
    JLabel ships;
    JLabel title;
    int targetX;
    int targetY;
    int time;
    Object[][] grid;
    Queue<Ship> shipsToDraw;
    ArrayList<Ship> shipsPlaced;
    Ship placing;
    int x;
    int y;

    public SetUp(JFrame frame, Socket server, boolean isClient) {
        this.frame = frame;
        this.server = server;
        this.isClient = isClient;
        //connecting();
        setUp();

    }

    public void setUp() {

        x = 0;
        y = 0;
        waitingForName = true;
        grid = new Object[10][10];
        shipsPlaced = new ArrayList<Ship>();

        shipsToDraw = new LinkedList<Ship>();
        shipsToDraw.add(new Ship(2, ""));
        shipsToDraw.add(new Ship(3, ""));
        shipsToDraw.add(new Ship(3, ""));
        shipsToDraw.add(new Ship(4, ""));
        shipsToDraw.add(new Ship(4, ""));
        shipsToDraw.add(new Ship(5, ""));
        placing = shipsToDraw.poll();
        timer = new Timer(75, this);

        waitingForName = true;
        this.setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        name = new JLabel("Name:");
        ships = new JLabel("shipsToDraw");
        title = new JLabel("SET-UP");
        instructions = new JTextArea("Enter Name");
        //instructions.
        nameField = new JTextField("Your Name");
        c.anchor = c.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;

        c.insets = new Insets(Helper.fixHeight2(20), Helper.fixWidth2(170), 0, 0);
        c.gridwidth = 2;
        c.gridx = 2;
        c.gridy = 0;
        title.setFont(Helper.titleFont);
        this.add(title, c);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(Helper.fixHeight2(40), Helper.fixWidth2(40), 0, 0);
        name.setFont(Helper.normalTxtFont);
        this.add(name, c);

        c.insets = new Insets(Helper.fixHeight2(40), Helper.fixWidth2(40), 0, 0);
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 0;

        nameField.setPreferredSize(new Dimension(Helper.fixWidth2(250), Helper.fixHeight2(50)));

        nameField.setFont(Helper.normalTxtFont);
        this.add(nameField, c);

        instructions.setLineWrap(true);

        c.gridwidth = 2;
        c.insets = new Insets(0, Helper.fixWidth2(85), Helper.fixHeight2(51), 0);
        c.gridx = 0;
        c.gridy = 1;

        instructions.setFont(Helper.normalTxtFont);
        instructions.setPreferredSize(new Dimension(Helper.fixWidth2(300), Helper.fixWidth2(600)));
        this.add(instructions, c);

        frame.add(this);
        nameField.addActionListener(this);
        frame.addKeyListener(this);
        frame.requestFocus();
        //nameField.requestFocus();
        frame.setVisible(true);
        targetX = -1;
        targetY = -1;
        timer.start();
        System.out.println("Done Set Up");
    }

    public void connecting() {
        try {
            InputStreamReader isr = new InputStreamReader(server.getInputStream());
            br = new BufferedReader(isr);
            pw = new PrintWriter(server.getOutputStream(), true);

        } catch (Exception e) {

        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            time++;
            if (waitingForName) {
                if (!nameField.isFocusOwner()) {
                    nameField.requestFocus();
                }
                nameField.setBackground(Helper.interpolate(Helper.buttonBg, Helper.highlighted, (double) time % Helper.highlightSteps / Helper.highlightSteps));
                //System.out.println(e);
            } else {
                if (!frame.isFocusOwner()) {
                    frame.requestFocus();
                }
                if (placing != null) {
                    placing.setX(targetX);
                    placing.setY(targetY);
                }

            }
            if (shipsToDraw.isEmpty()) {
                this.removeAll();
                frame.remove(this);
                frame.removeKeyListener(this);
                timer.stop();
                System.out.println("idk");
                frame.getContentPane().remove(this);
                frame.getContentPane().add(new Playing(grid, isClient, server, nameField.getText(), frame), "PLAYING");
                ((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), "PLAYING");
                frame.getContentPane().revalidate();
                frame.getContentPane().repaint();

            }
            repaint();
        }
        if (e.getSource() == nameField) {
            waitingForName = false;
            nameField.setFocusable(false);
            nameField.setBackground(Helper.buttonBg);
            targetX = 0;
            targetY = 0;

        }
        //repaint();
    }

    public void drawTarget(Graphics g) {
        if (targetX != -1) {
            g.drawRect(targetX * Helper.cellWidth + 472, 142 + targetY * Helper.cellWidth, Helper.cellWidth, Helper.cellWidth);
        }
    }

    public void keyTyped(KeyEvent e) {
        //System.out.println(KeyEvent.getKeyText(e.getKeyCode()));
    }

    public void keyPressed(KeyEvent e) {
        String key = KeyEvent.getKeyText(e.getKeyCode());
        System.out.println("X,Y  " + x + "," + y);
        if (!waitingForName) {
            if (placing != null) {
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

                    if (isValidPlacement()) {
                        for (int i = 0; i < placing.length; i++) {
                            grid[targetX + (horizontal ? i : 0)][targetY + (!horizontal ? i : 0)] = placing;
                        }

                        placing.setX(targetX);
                        placing.setY(targetY);

                        placing.setHorizontal(horizontal);
                        targetX = 0;
                        targetY = 0;
                        horizontal = true;

                        System.out.println(shipsPlaced);
                        shipsPlaced.add(placing);
                        System.out.println(shipsPlaced);
                        placing = shipsToDraw.poll();
                        while (!isValidPlacement()) {
                            targetX++;
                            if (targetX == 9 && targetY == 9) {
                                targetX = 0;
                                targetY = 0;
                                horizontal = false;
                            } else if (targetX == 9) {
                                targetX = 0;
                                targetY++;
                            }

                        }
                        System.out.println(shipsPlaced);
                    }

                }
            }
            if (key.equals("Slash")) {
                horizontal = !horizontal;
            }
            System.out.println(key);
            targetX = Math.max(0, Math.min(10 - (horizontal ? placing.getLength() : 0), targetX));
            targetY = Math.max(0, Math.min(10 - (!horizontal ? placing.getLength() : 0), targetY));
        }

    }

    public boolean isValidPlacement() {
        if (placing == null || targetX + (horizontal ? placing.getLength() : 0) > 10 || targetY + (!horizontal ? placing.getLength() : 0) > 10) {
            return false;
        }
        for (int i = 0; i < placing.getLength(); i++) {
            if (grid[targetX + (horizontal ? i : 0)][targetY + (horizontal ? 0 : i)] != null) {
                return false;
            }
        }
        return true;
    }

    public void keyReleased(KeyEvent e) {
        //System.out.println(KeyEvent.getKeyText(e.getKeyCode()));
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        g.fillRect(Helper.fixWidth2(472), Helper.fixHeight2(142), Helper.fixWidth2(600), Helper.fixHeight2(600));
        g.setColor(Color.BLUE);
        g.fillRect(Helper.fixWidth2(85), Helper.fixHeight2(142), Helper.fixWidth2(600), Helper.fixHeight2(600));//instrucys
        g.fillRect(Helper.fixWidth2(1160), Helper.fixHeight2(142), Helper.fixWidth2(600), Helper.fixHeight2(600));//ships
        g.setColor(Color.BLACK);
        int i = 0;
        if (shipsToDraw != null) {
            for (Ship ship : shipsToDraw) {
                g.drawRect(Helper.fixWidth2(1160), Helper.fixHeight2(142) + i * Helper.fixHeight2(75), (int) (Helper.fixWidth2(275) * ship.getLength() / 5.0), Helper.fixHeight2(50));
                i++;
            }
        }
        if (targetX != -1) {
            if (shipsPlaced != null) {
                for (Ship ship : shipsPlaced) {
                    //System.out.println("ship");
                    g.setColor(Color.BLUE);
                    g.fillRect(ship.getX() * Helper.cellWidth + Helper.fixWidth2(473), Helper.fixHeight2(143) + Helper.cellWidth * ship.getY(), -2 + Helper.cellWidth * (ship.isHorizontal() ? ship.getLength() : 1), -2 + Helper.cellWidth * (ship.isHorizontal() ? 1 : ship.getLength()));
                    g.setColor(Color.BLACK);
                }
            }
            if (placing != null) {
                if (!isValidPlacement()) {
                    g.setColor(Color.RED);
                }
                g.fillRect(placing.getX() * Helper.cellWidth + Helper.fixWidth2(472), Helper.fixHeight2(142) + Helper.cellWidth * placing.getY(), Helper.cellWidth * (horizontal ? placing.getLength() : 1), Helper.cellWidth * (horizontal ? 1 : placing.getLength()));

            }
        }
        //g.fillRect(x,y,30,30);
        //System.out.println(targetX+" X");
        //System.out.println(targetY+" Y");
        //drawTarget(g);

    }
}
