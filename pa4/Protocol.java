// Author: Joshua Loftus 4/20/2020
import java.net.*;
import java.io.*;
import java.util.*;

public class Protocol {

	static final String GNUTELLA_VERSION = "0.4";
	static final String MSG_1 = "GNUTELLA CONNECT/" + GNUTELLA_VERSION + "\n\n";
	static final String MSG_2 = "GNUTELLA OK\n\n";

	private static final boolean LOG = false;

	// creates tcp connection to verify node is valid, outlined in /storage/Gnutella_Protocol.pdf starting on the end of page 1
	// tcp server code given in previous projects so little explanation required.
	static final String VERIFY_HEADER = "VERIFY";
	static List<Node> verify_links(int port, int expected) {
		int connected;
		String line;
		String[] data;
		List<Node> out;
		ServerSocket server;
		DataInputStream input_stream;
		DataOutputStream output_stream;
		Socket socket;
		
		connected = 0;
		port += Directory.TCP_PORT_OFFSET;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("  verify_links socket exception...");
			server = null;
		}
		out = new ArrayList<Node>();
		socket = null;
		input_stream = null;
		output_stream = null;
		while (connected < expected) {
			line = "";
			data = new String[2];
			try {
				socket = server.accept();
			} catch (IOException e) {
				System.out.println("  verify_links io exception 1...");
			}
			connected++;
			try {
				input_stream = new DataInputStream(socket.getInputStream());
				output_stream = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				System.out.println("  verify_links io exception 2...");
			}
			try {
				output_stream.writeUTF(MSG_1);
				output_stream.flush();
				line = input_stream.readUTF();
			} catch (IOException e) {
				System.out.println("  verify_links io exception 3...");
			}
			if (!line.equals(MSG_2))
				System.out.println("  verify_links error in the connection msg exchange...");
			else {
				try {
					data[0] = input_stream.readUTF();
					data[1] = input_stream.readUTF();
				} catch (IOException e) {
					System.out.println("  verify_links io exception 4...");
				}
				out.add(new Node(data[0], Integer.parseInt(data[1])));
			}
		}
		if (expected == 0)
			return new ArrayList<Node>();
		return out;
	}

	// other half of verify_links, resident implies the node has an established neighbors list
	// another simple tcp connection previously covered.
	static Node verify_link_resident(String[] in, String _ip, int _port) {
		int port;
		String line;
		DataInputStream input_stream;
		DataOutputStream output_stream;
		Socket socket;
		
		port = Integer.parseInt(in[1]) + Directory.TCP_PORT_OFFSET;
		line = "";
		while (true) {
			try {
				socket = new Socket("localhost", port);
				input_stream = new DataInputStream(socket.getInputStream());
				output_stream = new DataOutputStream(socket.getOutputStream());
				break;
			} catch (IOException e) {
				if (LOG)
					System.out.println("  resident is waiting for tcp socket...");
			}
		}
		try {
			line = input_stream.readUTF();
		} catch (IOException e) {
			System.out.println("  verify_link_resident io exception 1...");
		}
		if (!line.equals(MSG_1))
			System.out.println("  verify_link_resident error in the connection msg exchange...");
		else {
			try {
				output_stream.writeUTF(MSG_2);
				output_stream.flush();
				output_stream.writeUTF(_ip);
				output_stream.writeUTF(_port + "");
				output_stream.flush();
			} catch (IOException e) {
				System.out.println("  verify_link_resident io exception 2...");
			}
		}
		try {
			socket.close();
		} catch (IOException e) {}
		return new Node(in[0], Integer.parseInt(in[1]));
	}

	/*
	Used to actively discover hosts on the network.
	A servant receiving a Ping descriptor is expected to respond with a Pong descriptors.
	(IP address, port, number/size of shared files)
	*/
	static final String PING_HEADER = "PING";
	static void ping(List<Node> neighbors, Map<String, Integer> files, String ip, int port) {
		String info;

		info = ping_helper(files, ip, port);
		for (Node n : neighbors) {
			Utility.udp_sender(info, n.port);
		}
	}

	private static String ping_helper(Map<String, Integer> files, String ip, int port) {
		String out;

		out = PING_HEADER;
		out += ip + ":" + port;
		if (files.size() != 0)
			out += "/";
		for (String file : files.keySet())
			out += ":" + file + "(" + files.get(file) + ")";
		return out;
	}

	/*
	The response to a Ping.
	Includes the address of a connected Gnutella servant and information regarding the amount of data it is making available to the network.
	
	A neighbor received a ping will pong back with a list of its own neighbors.
	The original node can select any node from the list as new neighbors and ping it. 
	*/
	static final String PONG_HEADER = "PONG";
	static void pong(List<Node> neighbors, int sender_port, String ip, int port) {
		String info;

		info = pong_helper(neighbors, ip, port);
		Utility.udp_sender(info, sender_port);
	}

	private static String pong_helper(List<Node> neighbors, String ip, int port) {
		String out;

		out = PONG_HEADER;
		out += ip + ":" + port;
		if (neighbors.size() != 0)
			out += "/";
		for (Node n : neighbors)
			out += n.simple_toString();
		return out;
	}

	/*
	The primary mechanism for searching the distributed network.
	A servant receiving a Query descriptor will respond with a QueryHit if a match is found against its local data set.
	*/
	static final String QUERY_HEADER = "QUERY";
	static void query(List<Node> neighbors, String search, String ip, int port, int depth, long start_time) {
		String info;

		info = query_helper(ip, search, port, depth, start_time);
		for (Node n : neighbors)
			Utility.udp_sender(info, n.port);
	}

	private static String query_helper(String ip, String search, int port, int depth, long start_time) {
		Random random;
		String out;

		random = new Random();
		out = QUERY_HEADER;
		out += "(A)" + ip + ":" + port;
		out += "(S)" + search;
		out += "(N)" + Math.abs(random.nextInt() % 10000);
		out += "(T)" + depth + ":" + start_time;
		return out;
	}

	// method that reuses query() method above but checks if the msg should continue on based on TTL and search depth
	// this is also where a node checks if it has the file that is being searched for, if so it returns QueryHit
	static void propagate_query(Map<String, Integer> files, List<Node> neighbors, String[] data, String ip, int port) {
		int depth_remaining, _port, index;
		long start_time;
		List<Node> parsed_neighbors;
		
		parsed_neighbors = neighbors;
		_port = Integer.parseInt(data[1]);
		start_time = Long.parseLong(data[5]);
		if (start_time + Directory.SEARCH_TIME - (System.currentTimeMillis() / 1000l) <= 0) {
			return;
		}
		for (String file : files.keySet()) {
			if (file.equals(data[2])) {
				query_hit(Integer.parseInt(data[1]), ip, port, file);
				return;
			}
		}
		depth_remaining = Integer.parseInt(data[4]);
		if (depth_remaining == 0) {
			return;
		}
		depth_remaining -= 1;
		
		index = 0;
		while (index < parsed_neighbors.size()) {
			if (parsed_neighbors.get(index).ip.equals(data[0]) && parsed_neighbors.get(index).port == _port) {
				parsed_neighbors.remove(index);
				break;
			}
			index++;
		}
		if (parsed_neighbors.size() > 0) 
			query(parsed_neighbors, data[2], data[0], _port, depth_remaining, start_time);
	}

	/*
	The response to a Query.
	This descriptor provides the recipient with enough information to acquire the data matching the corresponding Query
	*/
	static final String HIT_HEADER = "HIT";
	static void query_hit(int source_port, String ip, int port, String file) {
		String info;
		
		info = HIT_HEADER + ip + ":" + port;
		Utility.udp_sender(info, source_port);
		Utility.file_server(ip, port, file);
	}

	/*
	A mechanism that allows a fire walled servant to contribute file-based data to the network.
	*/
	static void push(Node node, String file_chosen, int size) {
		node.files.put(file_chosen, size);
	}

	// custom protocol to allow users to remove files they previously added
	static void pull(Node node, String file_chosen) {
		node.files.remove(file_chosen);
	}

	// custom protocol to disconnect node from network and tell its neighbors / the directory to replace it
	static final String KILL_HEADER = "-";
	static void kill_links(List<Node> neighbors, String ip, int port) {
		String info;
		
		info = KILL_HEADER + ip + ":" + port;
		neighbors.add(new Node("0", Directory.PORT));
		for (Node n : neighbors)
			Utility.udp_sender(info, n.port);
	}

	// only called by ui, lets user add node that has been seen in a pong list if it still exists
	static void add_neighbors_neighbor(Node user, String ip, int my_port, int their_port) {
		String their_msg;

		for (Node n : user.neighbors) {
			if (n.port == their_port)
				return;
		}
		Utility.udp_sender(Protocol.VERIFY_HEADER, my_port);
		their_msg = "+" + ip + ":" + my_port;
		Utility.udp_sender(their_msg, their_port);
	}
}