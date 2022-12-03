package org.jdamico.greenwich;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.telnet.TelnetClient;


public class TimeSpaceRuntime {

	private static TelnetClient telnet = null;
	public static InputStream in;
	private PrintStream out;
	public static Map<String, String> outputMessageMap = null;
	public static boolean shouldListenOutput = false;
	public static final int PROCESS_INTERVAL_MS = 5000;
	public static final int DOP_MINIMAL_PRECISION = 10;//6;
	public static String server;
	public static int port;

	public TimeSpaceRuntime(String server, int port) throws SocketException, IOException {
		TimeSpaceRuntime.server = server;
		TimeSpaceRuntime.port = port;
		telnet = new TelnetClient();
		telnet.connect(server, port);
		shouldListenOutput = true;
		in = telnet.getInputStream();
		out = new PrintStream(telnet.getOutputStream());
		outputMessageMap = new HashMap<String, String>();
	}

	public static void readUntil() {
		try {

			StringBuffer sb = new StringBuffer();
			char ch = (char) in.read();
			while (shouldListenOutput) {
				sb.append(ch);
				if(ch == '\n') {
					String line = sb.toString();
					System.out.println(line);      
					if(line.contains("\"class\":\"VERSION\"")) outputMessageMap.put("VERSION", line);
					else if(line.contains("\"class\":\"DEVICES\"")) outputMessageMap.put("DEVICES", line);
					else if(line.contains("\"class\":\"WATCH\"")) outputMessageMap.put("WATCH", line);
					else if(line.contains("\"class\":\"SKY\"")) outputMessageMap.put("SKY", line);
					else if(line.contains("\"class\":\"TPV\"")) outputMessageMap.put("TPV", line);
					else if(line.contains("\"class\":\"GST\"")) outputMessageMap.put("GST", line);
					sb = new StringBuffer();
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			System.err.println("Exception at Main class: "+e.getMessage());
		}
	}

	public void write(String value) {
		try {
			out.println(value);
			out.flush();
			System.out.println(value);
		} catch (Exception e) {
			System.err.println("Exception at Main class: "+e.getMessage());
			System.exit(1);
		}
	}

	public void sendCommand(String command) {
		try {
			write(command);
		} catch (Exception e) {
			System.err.println("Exception at Main class: "+e.getMessage());
			System.exit(1);
		}
	}

	private static void disconnect() throws IOException {
		shouldListenOutput = false;
		telnet.disconnect();
	}

	public static void main(String[] args) {

		if(args.length == 2) {

			TimeSpaceRuntime.server = args[0];
			try {
				TimeSpaceRuntime.port = Integer.parseInt(args[1]);
				connetAndCollectFromGpsD();
			}catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("Exception at Main class: "+e.getMessage());
				System.exit(1);
			}

		}else {
			System.err.println("Invalid parameters: You must pass GPSD_HOST followed by GPSD_PORT. Example: localhost 2947.");
		}
	}

	public static void connetAndCollectFromGpsD() {
		TimeSpaceRuntime telnet = null;
		try {
			System.out.println("Trying to connect GPSD..."+server+":"+port);
			telnet = new TimeSpaceRuntime(server, port);	
			Thread outputThread = new ListenerOutputThread();
			Thread listenerThread = new ListenerThread();
			outputThread.start();

			Thread.sleep(PROCESS_INTERVAL_MS);

			if(telnet != null && outputMessageMap != null && outputMessageMap.containsKey("VERSION")) {
				System.out.println("GPSD connected!");
				telnet.sendCommand("?WATCH={\"enable\":true,\"json\":true}");
				listenerThread.start();
				System.out.println("Verifier thread started.");
			} else disconnect();

		} catch (IOException e) {
			System.err.println("Exception at Main class: "+e.getMessage());
			System.exit(1);
		} catch (InterruptedException e) {
			System.err.println("Exception at Main class: "+e.getMessage());
			System.exit(1);
		}
	}
}
