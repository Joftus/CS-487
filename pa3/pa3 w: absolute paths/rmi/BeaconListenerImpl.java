import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

/*
	@Author Josh Loftus
	RMI object used to transfer beacons from Agent x to Manager.
*/
public class BeaconListenerImpl extends UnicastRemoteObject implements BeaconListener {

	BeaconListenerRegister beacon_listener_register;
	static final long serialVersionUID = 0;

	public BeaconListenerImpl(BeaconListenerRegister _beacon_listener_register) throws RemoteException {
		this.beacon_listener_register = _beacon_listener_register;
	}

	public void deposit(Beacon beacon) throws RemoteException {
		// System.out.println("    beacon deposited");
		beacon_listener_register.beacon_queue.add(beacon);
	}
}