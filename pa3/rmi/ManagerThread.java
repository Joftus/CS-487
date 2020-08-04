import java.util.*;
import java.rmi.*;

/*
	@Author Josh Loftus
	Stores functionality intended for Manager.java, separated for low coupling.
	Creates RMI client to send all 3 commands to Agent from handel(Beacon). handel(Agent)
	is called from run() if an Agents last beacon was sent over 2 * beacon time interval
	to remove the Agent from the list. run() is the thread loop that ensures all the registered
	Agents have remained active.
*/
public class ManagerThread extends Thread {

	public List<Client> agents;
	public Queue<Beacon> new_agents;
	CmdAgent cmd_agent;
	boolean race_conditions;

	public ManagerThread() {
		this.agents = new ArrayList<Client>();
		this.new_agents = new LinkedList<>();
	}

	void init_rmi_client() {
		String registration;
		Remote remote_service;

		try {
			registration = "rmi://localhost/CmdAgent";
			remote_service = Naming.lookup(registration);
			cmd_agent = (CmdAgent) remote_service;
		}
		catch (RemoteException re) { re.printStackTrace(); }
		catch (NotBoundException nbe) { nbe.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
	}

	public void run() {
		int index;
		long current_time = -1, expiration;
		Client agent;
		race_conditions = false;
		
		// System.out.println("  ManagerThread.run()");
		while (true) {
			current_time = 0;
			while (!new_agents.isEmpty()) {
				Beacon beacon = new_agents.remove();
				Client connected = new Client(beacon.id, beacon.start_up_time, beacon.time_interval);
				if (!agents.contains(connected)) {
					agents.add(connected);
					System.out.printf("    agent %d has connected!\n", beacon.id);
					handel(beacon);
				}
				else 
					System.out.println("beacon missed for agent" + connected.id);
				beacon = null;
				connected = null;
			}
			if (agents.size() != 0 && (!race_conditions || agents.size() == 1)) {
				current_time = (new Date()).getTime() / 1000;
				for (index = 0; index < agents.size(); index++) {
					agent = agents.get(index);
					expiration = agent.last_beacon_time + ((4 * agent.time_interval) / 1000);
					if (expiration < current_time) {
						index = agents.size();
						agents.remove(agent);
						System.out.printf("    agent %d has disconnected!\n", agent.id);
					}
				}
			}
			try { Thread.sleep(Agent.LOOP_TIME / 2); }
			catch (InterruptedException e) { System.out.println("ERROR: MT line 51"); }
		}
	}

	void handel(Beacon beacon) {
		GetLocalTime time;
		GetLocalOS os;
		GetVersion version;

		if (cmd_agent == null)
			init_rmi_client();
		time = new GetLocalTime();
		os = new GetLocalOS();
		version = new GetVersion();

		boolean fail = true;
		while (fail) {
			try {
				time = (GetLocalTime) cmd_agent.execute("GetLocalTime", time);
				version = (GetVersion) cmd_agent.execute("GetVersion", version);
				os = (GetLocalOS) cmd_agent.execute("GetLocalOS", os);
				fail = false;
			}
			catch (RemoteException e) {
				init_rmi_client();
				fail = true;
				race_conditions = true;
			}
		}
		race_conditions = false;
		System.out.println("      Time ("+ time.valid + "): " + time.time);
		System.out.println("      OS ("+ os.valid + "): " + os.os);
		System.out.println("      Version ("+ version.valid + "): " + version.version);
	}
}