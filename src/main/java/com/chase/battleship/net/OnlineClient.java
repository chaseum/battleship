package com.chase.battleship.net;

import com.chase.battleship.core.GameConfig;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class OnlineClient {

    public void joinGame(GameConfig config, Scanner scanner) throws IOException {
        System.out.print("Enter lobby code: ");
        String code = scanner.nextLine().trim();

        RendezvousClient.HostEndpoint ep = RendezvousClient.resolveCode(code);
        if (ep == null) {
            System.out.println("Code not found on rendezvous server.");
            return;
        }

        System.out.println("Connecting to host " + ep.host() + ":" + ep.port());

        try (Socket socket = new Socket(ep.host(), ep.port());
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Connected. Waiting for host instructions...");

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    System.out.println("Disconnected from host.");
                    break;
                }

                if (line.equals("BOWN_BEGIN")) {
					System.out.println("Your board:");
					while (!(line = in.readLine()).equals("BOWN_END")) {
						if (line.startsWith("BOWN ")) {
							System.out.println(line.substring(5));
						}
					}
				} else if (line.equals("BTRK_BEGIN")) {
					System.out.println("Enemy board (what you know):");
					while (!(line = in.readLine()).equals("BTRK_END")) {
						if (line.startsWith("BTRK ")) {
							System.out.println(line.substring(5));
						}
					}
				} else if (line.startsWith("PROMPT_MOVE")) {
					System.out.print("Your move (F r c | A EMP | A SHIELD r c | A MULTISHOT n | A SONAR r c): ");
					String cmd = scanner.nextLine().trim();
					out.println(cmd);
				} else if (line.startsWith("MSG ")) {
					System.out.println(line.substring(4));
				} else if (line.startsWith("OVER ")) {
					System.out.println("Game over. Winner: " + line.substring(5));
					break;
				} else {
					System.out.println("[Host] " + line);
				}
			}
        }
    }
}
