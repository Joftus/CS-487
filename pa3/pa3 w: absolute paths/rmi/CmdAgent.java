import java.rmi.Remote;
import java.rmi.RemoteException;
/*
	@Author Josh Loftus
	rmi interface for transfer of C commands to be referenced on
	the Agent-side through the use of jni libraries. This interface
	differs from BeaconListener.java because the return is !void.
*/
public interface CmdAgent extends Remote {
	public Object execute(String CmdID, Object CmdObj) throws RemoteException;
}