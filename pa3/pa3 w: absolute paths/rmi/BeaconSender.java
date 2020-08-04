import java.rmi.*;

/*
	@Author Josh Loftus
	Simple infinite loop in run() that deposits a beacon through
	rmi registry, sleep time set in Agent.java.
*/
public class BeaconSender extends Thread {

	Beacon beacon;
	BeaconListener beacon_listener;

	public BeaconSender(CmdRegister _cmd_register) {
		this.beacon = new Beacon(_cmd_register);
		init_rmi_client();
	}

	void init_rmi_client() {
		String registration;
		Remote remote_service;

		try {
			registration = "rmi://localhost/BeaconListener";
			remote_service = Naming.lookup(registration);
			beacon_listener = (BeaconListener) remote_service;
		}
		catch (RemoteException re) { re.printStackTrace(); }
		catch (NotBoundException nbe) { nbe.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
	}

	public void run() {
		System.out.println("  BeaconSender.run()");
		while (true) {
			try {
				beacon_listener.deposit(beacon);
				// System.out.println("    sent a beacon!");
			}
			catch (RemoteException re) { re.printStackTrace(); }
			catch (Exception e) { e.printStackTrace(); }
			
			try { Thread.sleep(Agent.TIME_INTERVAL); }
			catch (InterruptedException e) {}
		}
	}
}