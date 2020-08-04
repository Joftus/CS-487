import java.rmi.*;
import java.util.Queue;
import java.util.LinkedList;

/*
	Core component of Manager-side. Monitors new beacons through rmi registry by checking for an
		empty beacon_queue (line ~40) and removing any thing inside to later be processed by handel().
		handel() checks if it is an update beacon, if an agent restarted (i.e. a known id but different
		start_up_time), or if it is a new Agent (referenced as "Client" Manager-side). Receiving a 
		beacon triggers a C command, GetLocalTime, that is referenced through the use of jni *hand-waive
		for explanation later in docs*.
*/
public class BeaconListenerRegister extends Thread {

	ManagerThread manager_thread;
	BeaconListenerImpl beacon_service;
	public Queue<Beacon> beacon_queue;

	public BeaconListenerRegister(ManagerThread _manager_thread) throws RemoteException {
		this.manager_thread = _manager_thread;
		this.beacon_queue = new LinkedList<>();
		this.beacon_service = new BeaconListenerImpl(this);
		init_rmi_server();
	}

	void init_rmi_server() {
		String registration;

		try {
			System.out.println("    registering BeaconListener...");
			registration = "rmi://localhost/BeaconListener";
			Naming.rebind(registration, beacon_service);
		}
		catch (Exception e) { System.out.println("ERROR: BLR line 29\n\n\n"); }
	}

	public void run() {
		System.out.println("  BeaconListenerRegister.run()");
		while (true) {
			if (!beacon_queue.isEmpty()) {
				handel(beacon_queue.remove());
			}
			try { Thread.sleep(Agent.TIME_INTERVAL / 2); }
			catch (InterruptedException e) { System.out.println("ERROR: BLR line 39\n\n\n"); }
		}
	}

	public void handel(Beacon beacon) {
		int index, count;
		
		index = -1;
		for (count = 0; count < manager_thread.agents.size(); count++) {
			if (manager_thread.agents.get(count).id == beacon.id)
				index = count;
		}
		if (index != -1) {
			Client updating = manager_thread.agents.get(index);
			if (beacon.start_up_time != updating.start_up_time) {
				System.out.println("  Client " + updating.id + " has restarted...");
				updating.start_up_time = beacon.start_up_time;
			}
			try {
				updating.last_beacon_time = 
					((GetLocalTime) manager_thread.cmd_agent.execute("GetLocalTime", new GetLocalTime())).time;
			} catch (RemoteException e) { System.out.println("ERROR: BLR line 60"); }
		}
		else
			manager_thread.new_agents.add(beacon);
	}
}