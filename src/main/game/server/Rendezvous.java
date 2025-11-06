package src.main.game.server;

// RendezvousPlain.java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Rendezvous {
	static class Entry {
		String ip;
		int port;
		long t = System.currentTimeMillis();
	}

	static final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
	static final ExecutorService pool = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "rndz-handler");
		t.setDaemon(true);
		return t;
	});

	public static void main(String[] a) throws IOException {
		try (ServerSocket ss = new ServerSocket(9000)) {
			System.out.println("Rendezvous on 9000");
			while (true) {
				Socket s = ss.accept(); // only ONE accept loop
				pool.submit(() -> handle(s)); // hand off per connection
			}
		}
	}

	private static void handle(Socket s) {
		try (s;
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

			String line = in.readLine();
			if (line == null)
				return;

			if (line.startsWith("REG")) { // REG CODE PORT
				String[] p = line.split(" ");
				if (p.length == 3) {
					Entry e = new Entry();
					e.ip = s.getInetAddress().getHostAddress();
					e.port = Integer.parseInt(p[2]);
					map.put(p[1], e);
					out.println("OK");
				} else
					out.println("ERR");
			} else if (line.startsWith("GET")) { // GET CODE
				String[] p = line.split(" ");
				Entry e = (p.length == 2) ? map.get(p[1]) : null;
				if (e == null)
					out.println("NF");
				else
					out.println(e.ip + " " + e.port);
			} else {
				out.println("ERR");
			}
		} catch (IOException ignored) {
		}
	}
}
