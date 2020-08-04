// named after module described in hw
public class Manager {

	public AgentMonitor am; 	// agent monitor thread
	public BeaconListener bl;	// beacon listener thread

	public static final String SERVER_IP = "127.0.0.1";		// localhost
	public static final int BASE_UDP_PORT = 1234;			// udp port for receiving beacons
	public static final int BASE_TCP_PORT = 1235;			// base number for tcp ports, incremented to reflect the number of agents

	// basic runner to init the two threads
	public Manager() {
		try {
			System.out.println("\n\n\nstarting manager...");
			am = new AgentMonitor();
			am.start();
			bl = new BeaconListener(am);
			bl.start();
		}
		catch (Exception e) { System.out.println( e.getLocalizedMessage() ); }
	}

	// runs the class above;
	public static void main(String[] args) { 
		new Manager();
	}
}