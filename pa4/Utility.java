// Author: Joshua Loftus 4/20/2020
import java.io.*;
import java.net.*;

/*
functionality that include sending udp messages, file_server i.e. a tcp server for 
the node that has the queried file, and file_downloader i.e. a tcp connection to the file_server node
*/
public class Utility {

	private final static boolean LOG = false;

	// outgoing udp messages
	static void udp_sender(String info, int port) {
		DatagramPacket packet;
		byte[] buffer;
		DatagramSocket socket;
		InetAddress address;
		
		socket = null;
		address = null;
		if (LOG)
			System.out.println("  sending " + info + " to " + port + "...");
		try {
			socket = new DatagramSocket();
			address = InetAddress.getByName("localhost");
		} catch (SocketException e) {
			System.out.println("  udp_sender socket exception 1...");
			System.exit(0);
		} catch (UnknownHostException e) {
			System.out.println("  udp_sender unknown host exception...");
			System.exit(0);
		}
		buffer = info.getBytes();
		packet = new DatagramPacket(buffer, buffer.length, address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("  udp_sender i/o exception...");
		}
		socket.close();
	}

	// tcp server
	@SuppressWarnings({"resource"})
	static void file_server(String ip, int port, String file_name) {
		byte[] buffer;
		ServerSocket server;
		FileInputStream file_input_stream;
		DataOutputStream output_stream;
		File file;
		Socket socket;

		buffer = new byte[4096];
		file = new File(file_name);
		port += Directory.DOWNLOAD_PORT_OFFSET;
		try {
			server = new ServerSocket(port);
			socket = server.accept();
			output_stream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("  file_server io exception 1...");
			return;
		}
		try {
			file_input_stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("  file_server file not found exception...");
			return;
		}
		try {
			output_stream.writeLong(file.length());
			output_stream.flush();
			while (file_input_stream.read(buffer) > 0) {
				output_stream.write(buffer);
			}
			file_input_stream.close();
			output_stream.close();
			server.close();
		} catch (IOException e) {
			System.out.println("  file_server io exception 2...");
		}
	}

	// tcp connection to the tcp server above
	static void file_downloader(String ip, int port, String file_name) {
		int read, remaining;
		byte[] buffer;
		Socket socket;
		File file;
		FileOutputStream file_output_stream;
		DataInputStream input_stream;

		file_name = "copy_" + file_name;
		buffer = new byte[4096];
		file = new File(file_name);
		port += Directory.DOWNLOAD_PORT_OFFSET;
		try {
			if (file.createNewFile())
				System.out.println("\n  created the file named " + file_name + "...");
		} catch (IOException e) {
			System.out.println("  file_downloader io exception 1...");
			return;
		}
		while (true) {
			try {
				socket = new Socket(ip, port);
				break;
			} catch (IOException e) {}
		}
		try {
			input_stream = new DataInputStream(socket.getInputStream());
			remaining = (int) input_stream.readLong();
		} catch (IOException e) {
			System.out.println("  file_downloader io exception 2...");
			return;
		}
		try {
			file_output_stream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("  file_downloader file not found exception...");
			return;
		}
		try {
			while((read = input_stream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				remaining -= read;
				file_output_stream.write(buffer, 0, read);
			}
			file_output_stream.close();
			input_stream.close();
		} catch (IOException e) {
			System.out.println("  file_downloader io exception 3...");
			return;
		}
	}
}