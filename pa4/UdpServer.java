// Author: Joshua Loftus 4/20/2020
import java.net.*;
import java.io.*;

// helper thread for Node.java and Directory.java that receives udp msgs on the nodes port
// little documentation because it was concept covered in previous projects
public class UdpServer extends Thread {

	int port;
	Node node;
	Directory directory;

	private final static boolean LOG = false;

	public UdpServer(Node _node, int _port) {
		port = _port;
		node = _node;
		directory = null;
	}

	public UdpServer(Directory _directory, int _port) {
		port = _port;
		node = null;
		directory = _directory;
	}

	@SuppressWarnings({"resource"})
	public void run() {
		int buffer_size;
		DatagramPacket packet;
		DatagramSocket socket;
		byte[] buffer, storage;
		
		System.out.println("  udp server running...");
		socket = null;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("  udp_receiver socket exception...");
		}
		while (true) {
			try {
				buffer_size = socket.getReceiveBufferSize();
			} catch (SocketException e) {
				System.out.println("  UdpServer.run socket exception...");
				buffer_size = 0;
			}	
			buffer = new byte[buffer_size];
			packet = new DatagramPacket(buffer, buffer_size);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				System.out.println("  UdpServer.run io exception");
			}
			storage = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), 0, storage, 0, storage.length);
			if (storage.length > 0) {
				if (LOG)
					System.out.println("  " + new String(storage) + " was received!");
				if (directory == null)
					node.udp_queue.add(new String(storage));
				else
					directory.udp_queue.add(new String(storage));
			}
		}
	}
}