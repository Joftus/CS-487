// Author: Joshua Loftus 4/20/2020
import java.util.*;

/*
Created to mirror the host cache outlined in the paragraph below the protocol description
table on page 1 of /storage/Gnutella_Protocol.
*/
public class Directory extends Thread {

	List<String[]> host_cache;		// list of "ip:port" for nodes currently connected to network
	Queue<String> udp_queue;		// msgs passed from UdpServer server
	UdpServer server;				// used to receive msgs on port 1234, i.e. new node connections	
	
	static final int SEARCH_DEPTH = 5;		// neighbor propagate query depth to reduce query over-saturation
	static final int SEARCH_TIME = 20;		// TTL (seconds)

	static final int SLEEP_TIME = 1000;				// sleep time for while(true) loop, decreases overhead
	static final int INITAL_NEIGHBORS = 1;			// # of neighbors the directory should try to connect with a new node
	static final int PORT = 1234;					// port used for UdpServer
	static final int TCP_PORT_OFFSET = 1000;		// offset for nodes tcp port numbers
	static final int DOWNLOAD_PORT_OFFSET = 2000;	// offset for nodes tcp port equipped for file transfer
	private static final boolean LOG = true;		// logging variable because tracking concurrent threads is hell

	public Directory() {
		host_cache = new ArrayList<String[]>();
		udp_queue = new LinkedList<String>();
		server = new UdpServer(this, PORT);			// start register for receiving new node connections
		server.start();
	}

	@SuppressWarnings({"resource"})
	public void run() {
		System.out.println("\n  running directory...");
		while (true) {
			while (!udp_queue.isEmpty()) {			// if there are udp msgs, process them
				handel(udp_queue.remove());
			}
			try {
				sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
		}
	}

	// udp msg processor
	void handel(String in) {
		int index;
		String[] data;
		
		data = new String[2];
		index = 0;
		if (in.contains("-")) {								// char that signifies that a node has disconnected
			data[0] = in.substring(1, in.indexOf(":"));
			data[1] = in.substring(in.indexOf(":") + 1);
			System.out.println("  node on port " + data[1] + " has disconnected!");
			for (String[] node : host_cache) {
				if (node[1].equals(data[1]))
					break;
				index++;
			}
			if (index < host_cache.size())					// if node is in host_cache, remove it
				host_cache.remove(index);
			System.out.println("  host cache now keeps records of " + host_cache.size() + " node(s)...");
			return;
		}
		data[0] = in.substring(0, in.indexOf(":"));			// format udp msg into form of host_cache
		data[1] = in.substring(in.indexOf(":") + 1);
		if (!host_cache.contains(data))
			connect(data);									// this option means its a new node
		else
			replace_neighbor(data);							// this means it is an established node looking to replace a disconnected neighbor
	}

	// connects new client to 0 <= x < INITAL_NEIGHBORS nodes that will become its neighbors
	void connect(String[] in) {
		String info;
		int x;
		Random random;
		List<Integer> added;
		
		added = new ArrayList<Integer>();
		if (LOG)
			System.out.println("  connecting new client on port " + in[1] + "...");
		if (!host_cache.isEmpty()) {
			while (added.size() < INITAL_NEIGHBORS && added.size() < host_cache.size()) {	// picks random nodes from network to connect with the new node
				random = new Random();
				x = Math.abs(random.nextInt() % host_cache.size());
				if (x >= host_cache.size())
					x = host_cache.size() - 1;
				if (x >= 0) {
					if (added.isEmpty())
						added.add(x);
					else if (!added.contains(x))
						added.add(x);
				}
			}
			info = "+" + in[0] + ":" + in[1];
			for (Integer y : added) {														// send new nodes info to established nodes
				Utility.udp_sender(info, Integer.parseInt(host_cache.get(y)[1]));
			}
			Utility.udp_sender(added.size() + "", Integer.parseInt(in[1]));					// send number of neighbors to expect to the new node
		}
		else
			Utility.udp_sender(0 + "", Integer.parseInt(in[1]));							// this is in case it is the first node added
		host_cache.add(in);																	// add the new node to host_cache as it now has neighbors
	}

	// replaces neighbor if "quit" is entered into a nodes ui
	void replace_neighbor(String[] in) {
		String info;
		int x;
		Random random;
		
		if (LOG)
			System.out.println("  replacing neighbor...");
		if (!host_cache.isEmpty()) {
			while (true) {												// requests new random node as a possible neighbor, underdeveloped
				random = new Random();
				x = random.nextInt() % (host_cache.size() - 1);
				if (!host_cache.get(x)[1].equals(in[1])) {
					info = "+" + in[0] + ":" + in[1];
					Utility.udp_sender(info, Integer.parseInt(host_cache.get(x)[1]));
					Utility.udp_sender(Protocol.VERIFY_HEADER, Integer.parseInt(in[1]));
					break;
				}
			}
		}
	}

	// for ui, no dynamic use
	public static String static_toString() {
		String out;

		out = "DIRECTORY INFO";
		out += "\n" + UI.TAB + "search depth: " + SEARCH_DEPTH + " layers";
		out += "\n" + UI.TAB + "search time: " + SEARCH_TIME + " seconds";
		out += "\n" + UI.TAB + "initial neighbors: " + INITAL_NEIGHBORS + " nodes";
		out += "\n" + UI.TAB + "udp port: " + PORT;
		out += "\n" + UI.TAB + "node tcp port offset: " + TCP_PORT_OFFSET;

		return out;
	}

	// Directory runner
	public static void main(String[] args) {
		new Directory().run();
	}
}