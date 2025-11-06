
package src.main.game.logic;

import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Helper {

	public static boolean SINGLEPLAYER = true;
	public static Font titleFont;
	public static Font buttonFont;
	public static Color highlighted;
	public static Color bgColor;
	public static Color buttonBg;
	public static Color textColor;
	public static int highlightSteps;
	public static double width;
	public static double height;
	public static int fieldPadding;
	public static Font normalTxtFont;
	public static String ipAddress;
	public static int cellWidth;
	JFrame frame;
	// 217,255 236
	// 168,232 200
	// 69,148 109

	static {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = screenSize.getWidth();
		height = screenSize.getHeight();

		bgColor = new Color(52, 145, 183);
		highlighted = new Color(236, 200, 109);
		buttonBg = new Color(97, 97, 97);
		textColor = new Color(97, 97, 97);
		highlightSteps = 20;
		fieldPadding = 10;
		cellWidth = fixHeight2(60);

		try (InputStream in = Helper.class.getResourceAsStream("/fonts/Pixellari.ttf")) {
			Font base = Font.createFont(Font.TRUETYPE_FONT, in);
			titleFont = base.deriveFont((float) fixWidth(125));
			normalTxtFont = base.deriveFont((float) fixWidth(60));
			buttonFont = base.deriveFont((float) fixWidth(75));
		} catch (Exception e) {
			titleFont = new Font(Font.SANS_SERIF, Font.BOLD, fixWidth(125));
			normalTxtFont = new Font(Font.SANS_SERIF, Font.PLAIN, fixWidth(60));
			buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, fixWidth(75));
			e.printStackTrace();
		}
		try {
			ipAddress = java.net.InetAddress.getLocalHost().getHostAddress();
		} catch (Exception ignored) {
			ipAddress = "127.0.0.1";
		}
	}

	public Helper() {

	}

	public static int fixHeight(int height) {
		return (int) (height / 1080.0 * Helper.height);
	}

	public static int fixHeight2(int height) {
		return (int) (height / 864.0 * Helper.height);
	}

	public static int fixWidth(int width) {
		return (int) (width / 1920.0 * Helper.width);
	}

	public static int fixWidth2(int width) {
		return (int) (width / 1536.0 * Helper.width);
	}

	public static Color interpolate(Color c1, Color c2, double fraction) {
		int red = (int) ((c2.getRed() - c1.getRed()) * fraction + c1.getRed());
		int green = (int) ((c2.getGreen() - c1.getGreen()) * fraction + c1.getGreen());
		int blue = (int) ((c2.getBlue() - c1.getBlue()) * fraction + c1.getBlue());
		return new Color(red, green, blue);

	}

	public static String ipToCode(String ips) {
		String[] ip = ips.split("\\.");

		long num = (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8)
				+ Long.parseLong(ip[3]);
		long[] nums = { num / 380204032l,
				(num % 380204032l) / 7311616l,
				(num % 7311616l) / 140608l,
				(num % 140608l) / 2704l,
				(num % 2704l) / 52l,
				num % 52
		};
		String code = "";
		for (long lon : nums) {
			if (lon < 26) {
				code += ((char) (lon + 'A'));
			} else {
				code += ((char) (lon + 'G'));
			}
		}
		return code;

	}

	public static String codeToIp(String code) {
		long num = 0;
		int ind = 0;
		for (int i = code.length() - 1; i >= 0; i--) {
			if (code.charAt(i) <= 'Z') {
				num += (code.charAt(i) - 'A') * (long) Math.pow(52, ind);
			} else {
				num += (code.charAt(i) - 'a' + 26) * (long) Math.pow(52, ind);
			}
			ind++;

		}
		String binary = Long.toBinaryString(num);
		while (binary.length() < 32) {
			binary = "0" + binary;
		}
		while (binary.length() > 32) {
			binary = binary.substring(1);
		}

		return Integer.parseInt(binary.substring(0, 8), 2) + "." +
				Integer.parseInt(binary.substring(8, 16), 2) + "." +
				Integer.parseInt(binary.substring(16, 24), 2) + "." +
				Integer.parseInt(binary.substring(24, 32), 2);
	}

	public static String makeCode(int len) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		StringBuilder sb = new StringBuilder(len);
		java.util.Random r = new java.util.Random();
		for (int i = 0; i < len; i++) {
			sb.append(chars.charAt(r.nextInt(chars.length())));
		}
		return sb.toString();
	}

	public static int clamp(int min, int num, int max) {
		return Math.max(min, Math.min(max, num));

	}

	public static void showCard(JFrame frame, String name) {
		((CardLayout) frame.getContentPane().getLayout()).show(frame.getContentPane(), name);
	}

	public static boolean confirmExit(Component parent) {
		int choice = JOptionPane.showConfirmDialog(
				parent,
				"Are you sure you want to exit?",
				"Exit",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		return choice == JOptionPane.YES_OPTION;
	}
}
