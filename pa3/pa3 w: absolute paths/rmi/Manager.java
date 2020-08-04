import java.rmi.RemoteException;

/*
	@Author Josh Loftus
	Driver class for Manager, inits both the Manager Thread and the Beacon Listener Register.
*/
public class Manager {

	public Manager() throws RemoteException {
		ManagerThread manager_thread = new ManagerThread();
		BeaconListenerRegister beacon_listener_register = new BeaconListenerRegister(manager_thread);
		
		manager_thread.start();
		beacon_listener_register.start();
	}

	public static void main(String[] args) throws RemoteException {
		System.out.println("\nstarting manager (java)...");
		new Manager();
	}
}