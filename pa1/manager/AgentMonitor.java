import java.util.List;
import java.util.ArrayList;
import java.util.GregorianCalendar;


// class is named after the hw specs.
// @Author: Josh Loftus
class AgentMonitor extends Thread {
	
	List<Client> active_agents;			// List of current active agents
	List<Long> beacon_times;			// keeps track of the most recent beacon sent by the corresponding index in active_agents
	
	public AgentMonitor() { 
		this.active_agents = new ArrayList<Client>(); // init lists for object
		this.beacon_times = new ArrayList<Long>();
	}

	public void run() {		// the loop to keep the AgentMonitor thread alive
		System.out.println("  AgentMonitor.run()");
		int index;
		long time;
		List<Client> check;		// stores a copy of active_agents to stop changes from BeaconListener.java messing up the indexing.
		while (true) {
			check = active_agents;
			time = new GregorianCalendar().getTimeInMillis();			// gets current time
			for (index = 0; index < check.size(); index++) {
				Client client = check.get(index);						// term client and agent are used interchangeably
				if (Math.abs(time - beacon_times.get(index)) > 2 * client.time_interval) {			// if time_interval * 2 has passed since last beacon the client has died
					System.out.println("agent " + client.id + " has died...");
					active_agents.remove(index);
					beacon_times.remove(index);
				}
			}
			if (check.size() > 0) {
				try { Thread.sleep(check.get(0).time_interval); }		// sleeps thread to decrease stress on machine
				catch (InterruptedException e) { System.out.println("ERROR: AgentMonitor.run()"); }
			}
		}
	}
}