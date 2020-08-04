import java.rmi.Remote;
import java.rmi.RemoteException;

/*
	@Author Josh Loftus
	interface to be implemented by BeaconListenerImpl, used in rmi process.
*/
public interface BeaconListener extends Remote {
	public void deposit(Beacon beacon) throws RemoteException; // put b to the list
}