import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.GregorianCalendar;
import java.util.List;
import java.net.Socket;
import java.util.ArrayList;

// class is named after the hw specs.
// @Author: Josh Loftus
public class BeaconListener extends Thread {
	
	AgentMonitor agent_monitor;		// pass in agent_monitor to access the active_agents
	DatagramSocket udp_socket;		// socket used for receiving beacons

	public BeaconListener(AgentMonitor agentMonitor) {
		this.agent_monitor = agentMonitor;
	}

	public void run() {
		System.out.println("  BeaconListener.run()");
		int buffer_size;
		DatagramPacket packet;
		byte[] buffer, storage;

		try { udp_socket = new DatagramSocket(Manager.BASE_UDP_PORT); }			// init udp_socket for port 1234
		catch (Exception e) { System.out.println("ERROR: BeaconListener.run() line 34"); }
		while(true) {
			try {
				buffer_size = udp_socket.getReceiveBufferSize();				// get max buffer size
				buffer = new byte[buffer_size];
				packet = new DatagramPacket(buffer, buffer_size);				// create new packet object
				udp_socket.receive(packet);										// receive info using packet
				storage = new byte[packet.getLength()];
				System.arraycopy(packet.getData(), 0, storage, 0, storage.length);	// copy data to new object so it doesn't get overwritten by the new buffer
				if (storage.length > 0) handle(new String(storage));				// send to handel() method
			}
			catch (Exception e) { System.out.println("ERROR: BeaconListener.run()"); }
		}
	}

	void handle(String data) {
		int index;
		Client current;
		List<Object> beacon;	// list containing parsed data from beacon sent

		beacon = parse(data);	// sends data to be split into predefined fragments
		current = null;
		for (Client client : agent_monitor.active_agents) { // check all agents to see if it is a repeat
			if (client.id == (int) beacon.get(0))
				current = client;
		}

		if (current == null) {		// if its still null that means its a new client
			try {
				int tcp_port;
				tcp_port = findTcpPort();		// port number to be used for cmd agent tcp messages
				current = new Client((int) beacon.get(0), (long) beacon.get(1), beacon.get(2).toString(), (int) beacon.get(3), tcp_port);	// create new client object with beacon info
				agent_monitor.active_agents.add(current);										// add new agent to the active agent list
				agent_monitor.beacon_times.add(new GregorianCalendar().getTimeInMillis());		// add current time to beacon list
				System.out.println("new agent " + current.id + " added...");
				cmdAgentTrigger(current);			// send new client to use tcp ports for cmd agent
			}
			catch (Exception e) { System.out.println(e.getLocalizedMessage()); }
		}
		else if (current != null && current.start_up_time != (long) beacon.get(1)) {		// if an agent is found in active_agent but the startup time is different it means the agent restarted
			try {
				System.out.println("agent " + current.id + " restarted...");
				index = agent_monitor.active_agents.indexOf(current);
				current.start_up_time = (long) beacon.get(1);								// update startup time of existing agent
				agent_monitor.active_agents.set(index, current);
				agent_monitor.beacon_times.set(index, new GregorianCalendar().getTimeInMillis());	// update beacon times
				current = agent_monitor.active_agents.get(index);
				cmdAgentTrigger(current);			// send new client to use tcp ports for cmd agent
			}
			catch (Exception e) { System.out.println("ERROR: BeaconListener.handel() block 2"); }
		}
		else {
			index = agent_monitor.active_agents.indexOf(current);
			agent_monitor.beacon_times.set(index, new GregorianCalendar().getTimeInMillis());		// update beacon times
		}
	}

	// simple helper method to find lowest number TCP port available
	int findTcpPort() {
		int port = Manager.BASE_TCP_PORT;
		boolean taken;
		while (true) {
			taken = false;
			for (Client agent : agent_monitor.active_agents) {
				if (agent.tcp_port == port)
					taken = true;
			}
			if (!taken)
				return port;
			else
				port++;
		}
	}

	/*
	beacon list structure
	0. ID
	1. Start Time
	2. IP
	3. Time Interval
	4. Command Port
	*/
	List<Object> parse(String data) {
		List<Object> beacon;		
		int a, b;
		
		beacon = new ArrayList<Object>();	// simple parsing of beacon to make data more manageable
		try {
			a = data.indexOf("ID: ") + 4;
			b = data.indexOf("ST: ") - 1;
			beacon.add(Integer.parseInt(data.substring(a, b)));
			a = b + 5;
			b = data.indexOf("IP: ") - 1;
			beacon.add(Long.parseLong(data.substring(a, b)));
			a = b + 5;
			b = data.indexOf("TI: ") - 1;
			beacon.add(data.substring(a, b));
			a = b + 5;
			b = data.indexOf("CP: ") - 1;
			beacon.add(Integer.parseInt(data.substring(a, b)));
			beacon.add(Integer.parseInt(data.substring(b + 5)));
		}
		catch (Exception e) {
			System.out.println("ERROR: BeaconListener.parse() malformed packet!");
			System.out.println("packet: " + data);
		}
		return beacon;
	}

	void cmdAgentTrigger(Client client) {
		List<String> commands;				// group the two commands to be sent through the tcp port
		DataOutputStream output_stream;
		DataInputStream input_stream;
		byte[] buffer, buffer_info;
		try {
			commands = new ArrayList<String>(); 
			commands.add("GetLocalOS()");
			commands.add("GetLocalTime()");
			Socket socket = new Socket(Manager.SERVER_IP, client.tcp_port);			// used global ip and client specific 
			output_stream = new DataOutputStream(socket.getOutputStream());			// open streams after socket connection
			input_stream = new DataInputStream(socket.getInputStream());
			System.out.println("\n-------| Agent " + client.id + " |-------");
			for (String output : commands) {
				buffer = output.getBytes();
				buffer_info = toBytes(buffer.length);
				output_stream.write(buffer_info, 0, buffer_info.length);
				output_stream.write(buffer, 0, buffer.length);					// write out cmds
				output_stream.flush();
				input_stream.readFully(buffer);				// retrieve info from response
				if (commands.indexOf(output) == 0) System.out.println("	OS: " + new String(buffer));	// print info to console
				else System.out.println("	Start Time: " + new String(buffer) + "\n");
			}
			socket.close();
		}
		catch (Exception e) { 
			System.out.println("ERROR IN cmdAgentTrigger");
			System.out.println(e.getLocalizedMessage());
		}
	}

	static byte[] toBytes(int i) {				// this method was provided, not created by me!!!
        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);
        return result;
    }
}