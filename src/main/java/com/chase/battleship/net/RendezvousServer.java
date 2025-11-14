package com.chase.battleship.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class RendezvousServer {

    private static class Entry {
        final String ip;
        final int port;
        final Instant created;

        Entry(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.created = Instant.now();
        }
    }

    private final Map<String, Entry> registry = new ConcurrentHashMap<>();

    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket(RendezvousConfig.PORT)) {
            System.out.println("RendezvousServer listening on port " + RendezvousConfig.PORT);
            var pool = Executors.newCachedThreadPool();

            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> handleClient(socket));
            }
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line = in.readLine();
            if (line == null) return;

            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0) return;

            switch (parts[0]) {
                case "REG" -> handleRegister(parts, socket, out);
                case "GET" -> handleGet(parts, out);
                default -> out.println("ERR Unknown command");
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    private void handleRegister(String[] parts, Socket socket, PrintWriter out) {
        if (parts.length != 3) {
            out.println("ERR Usage: REG <code> <port>");
            return;
        }
        String code = parts[1];
        int port = Integer.parseInt(parts[2]);
        String ip = socket.getInetAddress().getHostAddress();
        registry.put(code, new Entry(ip, port));
        out.println("OK");
        System.out.println("Registered code " + code + " -> " + ip + ":" + port);
    }

    private void handleGet(String[] parts, PrintWriter out) {
        if (parts.length != 2) {
            out.println("ERR Usage: GET <code>");
            return;
        }
        String code = parts[1];
        Entry e = registry.get(code);
        System.out.println("GET " + code + " -> " + (e == null ? "NF" : e.ip + ":" + e.port));
        if (e == null) {
            out.println("NF");
        } else {
            out.println(e.ip + " " + e.port);
        }
    }

    public static void main(String[] args) throws IOException {
        new RendezvousServer().start();
    }
}
