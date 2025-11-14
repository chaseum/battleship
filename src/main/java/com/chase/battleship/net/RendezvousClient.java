package com.chase.battleship.net;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RendezvousClient {

    public record HostEndpoint(String host, int port) {}

    public static void registerCode(String code, int port) throws IOException {
        System.out.println("[RZ] Registering code " + code + " on port " + port);
        try (Socket s = new Socket(RendezvousConfig.HOST, RendezvousConfig.PORT);
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {
            out.println("REG " + code + " " + port);
        }
    }

    public static HostEndpoint resolveCode(String code) throws IOException {
        System.out.println("[RZ] Resolving code " + code + "...");
        try (Socket s = new Socket(RendezvousConfig.HOST, RendezvousConfig.PORT);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            out.println("GET " + code);   // sends GET + newline + flush

            String line = in.readLine();  // waits for single-line response
            System.out.println("[RZ] Response: " + line);

            if (line == null || "NF".equals(line)) {
                return null;
            }
            String[] parts = line.split("\\s+");
            return new HostEndpoint(parts[0], Integer.parseInt(parts[1]));
        }
    }
}