import java.rmi.Naming;
import java.rmi.RemoteException;

/*
	@Author Josh Loftus
	Simple RMI server that loops in run(). 
*/
public class CmdRegister extends Thread {

	CmdAgentImpl cmd_service;


	public CmdRegister() {
		// System.out.println("  CmdRegister()...");
		try { this.cmd_service = new CmdAgentImpl(); }
		catch (RemoteException re) { re.printStackTrace(); }
		init_rmi_server();
	}

	void init_rmi_server() {
		String registration;

		try {
			// System.out.println("    registering CmdAgent...");
			registration = "rmi://localhost/CmdAgent";
			Naming.rebind(registration, cmd_service);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public void run() {
		// System.out.println("  CmdRegister.run()");
		while (true) {
			try { Thread.sleep(Agent.TIME_INTERVAL); }
			catch (InterruptedException e) {}
		}
	}
}