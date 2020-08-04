// Author: Joshua Loftus 4/20/2020
import java.util.*;
import java.net.*;

/*
Core component of network, runs in tandem with UI.java to let the user
trigger static methods from Protocol.java and view info.
*/
public class Node extends Thread {

	int port;			// port for UdpServer, port + Directory.TCP_PORT_OFFSET = tcp port, port + Directory.DOWNLOAD_PORT_OFFSET = file transfer port.
	boolean found;		// communication boolean with ui to signal if a query was found
	boolean failed;		// communication boolean with ui to signal if a query timed out
	boolean kill;		// changed to true if user enters "quit" into ui, triggers Protocol.kill_links
	String ip;			// essentially unused as this network works on localhost
	String search;		// "" unless ui enters "search" then this changes to UI.file_chosen
	long start_search_time;				// start time of search, tracks TTL
	Map<String, Integer> files;			// map of file names added and their size
	List<String> search_history;		// keeps list of search input entries from the ui
	List<Node> neighbors;				// list of neighbor nodes that this node exchanges ping/pongs with
	List<Integer> possible_neighbors;	// list of ports sent to this node in pong msgs from neighbors, ui lets users add nodes from this list
	Queue<String> udp_queue;			// udp msgs sent from UdpServer server
	UdpServer server;					// server unique to this node running on port entered in args[0]

	final static int SLEEP_TIME = 1000;			// sleep timing for while(true) loop, reduce overhead
	private final static boolean LOG = false;	// for developer, used to track thread progress

	// core constructor run after starting program, this builds your node
	public Node(int _port) {
		port = _port;
		init();
	}

	// constructor for neighbor nodes where only port and ip are ever tracked, simple nodes
	public Node(String _ip, int _port) {
		ip = _ip;
		port = _port;
	}

	// inits variables and finds starting neighbors through the Directory then verifying them with a tcp connection and msg exchange
	public void init() {
		int expected;

		System.out.println("\n  initializing your node...");
		found = false;
		failed = false;
		kill = false;
		search = "";
		udp_queue = new LinkedList<String>();
		neighbors = new ArrayList<Node>();
		possible_neighbors = new ArrayList<Integer>();
		files = new HashMap<String, Integer>();
		search_history = new ArrayList<String>();
		server = new UdpServer(this, port);								// starts udp server that will receive msgs from other nodes and place them in udp_queue for node to process
		server.start();
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("  init unknown host exception...");
		}
		if (LOG)
			System.out.println("  finding a link through directory...");
		Utility.udp_sender(ip + ":" + port, Directory.PORT);
		while (udp_queue.isEmpty()) {
			try {
				sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
		}
		expected = Integer.parseInt(udp_queue.remove());
		if (LOG)
			System.out.println("  expecting " + expected + " links to be made...");
		neighbors = Protocol.verify_links(port, expected);
		if (LOG)
			System.out.println("  " + neighbors.size() + "/" + expected + " links made!");
	}

	// main loop for node running, here queries are sent, TTL is tracked, pings are ponged back and the node sends pings here.
	@SuppressWarnings({"resource"})
	public void run() {
		System.out.println("  running node...");

		while (true) {
			if (kill) {							// if user enters "quit" through ui
				Protocol.kill_links(neighbors, ip, port);
				break;
			}
			if (!search.equals("")) {			// search/query functionality
				search_history.add(search);
				start_search_time = System.currentTimeMillis() / 1000l;
				Protocol.query(neighbors, search, ip, port, Directory.SEARCH_DEPTH, System.currentTimeMillis() / 1000l);
				search = "";
			}
			while (!udp_queue.isEmpty()) {				// udp msgs deposited by udp server
				try {
					handel(udp_queue.remove());			// send msgs to be parsed based on header
				} catch (Exception e) {}
			}
			if (start_search_time != 0 && start_search_time + Directory.SEARCH_TIME - (System.currentTimeMillis() / 1000l) <= 0) {		// check TTL
				failed = true;			// signal ui that file was not found
				start_search_time = 0;
			}
			Protocol.ping(neighbors, files, ip, port);		// ping neighbors
			try {
				sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
		}
	}

	// function tasked with parsing incoming udp msgs and parsing/responding to their data
	void handel(String in) {
		String[] data;
		Node tmp;
		int p;

		// add node to neighbors, msg sent from Directory or Protocol.add_neighbors_neighbor()
		if (in.contains("+")) {
			if (LOG)
				System.out.println("  handling: " + in);
			in = in.substring(1);
			
			data = new String[2];
			data[0] = in.substring(0, in.indexOf(":"));
			data[1] = in.substring(in.indexOf(":") + 1);
			tmp = Protocol.verify_link_resident(data, ip, port);
			if (tmp != null) {
				for (Node n : neighbors) {
					if (n.port == tmp.port)
						return;
				}
				neighbors.add(tmp);
				possible_neighbors.remove(tmp.port);
			}
		}
		// remove node because it is disconnecting from the network
		else if (in.contains(Protocol.KILL_HEADER)) {
			if (LOG)
				System.out.println("  handling: " + in);
			in = in.substring(1);
			
			data = new String[2];
			data[0] = in.substring(0, in.indexOf(":"));
			data[1] = in.substring(in.indexOf(":") + 1);
			for (Node n : neighbors) {
				if (n.ip.equals(data[0]) && n.port == Integer.parseInt(data[1])) {
					neighbors.remove(n);
					break;
				}
			}
			Utility.udp_sender(ip + ":" + port, Directory.PORT);			// msgs directory to replace neighbor
		}
		// ping msg received and send pong back
		else if (in.contains(Protocol.PING_HEADER)) {
			if (in.indexOf("/") == -1)
				p = Integer.parseInt(in.substring(in.indexOf(":") + 1));
			else
				p = Integer.parseInt(in.substring(in.indexOf(":") + 1, in.indexOf("/")));
			Protocol.pong(neighbors, p, ip, port);
		}
		// pong msg received, parse to check for new possible_neighbors that can be added through the ui
		else if (in.contains(Protocol.PONG_HEADER)) {
			in = in.substring(4);
			if (in.contains("/")) {
				while (in.contains("@")) {
					in = in.substring(in.indexOf("@") + 1);
					int possible_port;
					if (in.contains("@")) {
						possible_port = Integer.parseInt(in.substring(in.indexOf(":") + 1, in.indexOf("@")));
						in = in.substring(in.indexOf("@"));
					}
					else
						possible_port = Integer.parseInt(in.substring(in.indexOf(":") + 1));
					if (!possible_neighbors.contains(possible_port) && possible_port != port) {
						boolean unseen = true;
						for (Node n : neighbors) {
							if (n.port == possible_port)
								unseen = false;
						}
						if (unseen)
							possible_neighbors.add(possible_port);
					}
				}
			}
			return;
		}
		// query received, parsed, then processed by Protocol.propagate_query()
		else if (in.contains(Protocol.QUERY_HEADER)) {
			if (LOG)
				System.out.println("  handling: " + in);
			data = new String[6];

			data[0] = in.substring(in.indexOf("(A)") + 3, in.indexOf(":"));
			data[1] = in.substring(in.indexOf(":") + 1, in.indexOf("(S)"));
			data[2] = in.substring(in.indexOf("(S)") + 3, in.indexOf("(N)"));
			data[3] = in.substring(in.indexOf("(N)") + 3, in.indexOf("(T)"));
			data[4] = in.substring(in.indexOf("(T)") + 3, in.indexOf(":", in.indexOf("(T)")));
			data[5] = in.substring(in.indexOf(":", in.indexOf("(T)")) + 1);
			Protocol.propagate_query(files, neighbors, data, ip, port);
		}
		// hit on previous query received, send data including other nodes ip and port to Utility.file_downloader
		else if (in.contains(Protocol.HIT_HEADER)) {
			String download_ip;
			int download_port;
			
			if (LOG)
				System.out.println("  handling: " + in);
			in = in.substring(Protocol.HIT_HEADER.length());
			download_ip = in.substring(0, in.indexOf(":"));
			download_port = Integer.parseInt(in.substring(in.indexOf(":") + 1));
			Utility.file_downloader(download_ip, download_port, search_history.get(search_history.size() - 1));
			found = true;			// notify ui that the file has been found
		}
		// reused the verify_links protocol to connect to a single new node
		else if (in.contains(Protocol.VERIFY_HEADER)) {
			if (LOG)
				System.out.println("  handling: " + in);
			tmp = Protocol.verify_links(port, 1).get(0);
			if (tmp != null) {
				for (Node n : neighbors) {
					if (n.port == tmp.port)
						return;
				}
				neighbors.add(tmp);
				possible_neighbors.remove(tmp.port);
			}
		}
	}

	// formatting, better described in README.txt
	public String simple_toString() {
		String out;

		out = "";
		out += "@" + ip + ":" + port;
		return out;
	}

	// formatting, only used by ui to display node info
	public String toString() {
		String out;

		out = "USER INFO";
		out += "\n" + UI.TAB + "ip: " + ip;
		out += "\n" + UI.TAB + "port: " + port;
		if (!files.isEmpty()) {
			out += "\n" + UI.TAB + "files: " + files.keySet().size();
			for (String file : files.keySet())
				out += "\n" + UI.DTAB + file + " (" + files.get(file) + ")";
		}
		if (!neighbors.isEmpty()) {
			out += "\n"+ UI.TAB + "neighbors: " + neighbors.size();
			for (Node n : neighbors)
				out += "\n" + UI.DTAB + n.port;
		}
		if (!search_history.isEmpty()) {
			out += "\n" + UI.TAB + "search history: " + search_history.size();
			for (String s : search_history)
				out += "\n" + UI.DTAB + s;
		}
		return out;
	}

	// for ui only, formats string for showing possible neighbors
	public String possible_toString() {
		String out;

		out = "NEIGHBOR PROSPECTS";
		for (Integer n : possible_neighbors)
			out += "\n" + UI.TAB + n;
		return out;
	}

	// node driver that also checks port validity
	public static void main(String[] args) {
		Node node;
		UI ui;
		int __port;

		__port = Integer.parseInt(args[0]);
		if (__port < 1235 || __port > 2234) {
			System.out.println("PORT VIOLATION: port " + __port + " is outside the designated range (1235-2234)");
			System.exit(0);
		}
		node = new Node(__port);
		ui = new UI(node);
		node.start();
		ui.start();
	}
}