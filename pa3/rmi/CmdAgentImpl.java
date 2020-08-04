import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.lang.Object;

/*
	@Author Josh Loftus
	RMI object also used to access jni using the 3 native methods found at the bottom
	of the class.
*/
public class CmdAgentImpl extends UnicastRemoteObject implements CmdAgent {

	static final long serialVersionUID = 0;
	
	public CmdAgentImpl() throws RemoteException {}

	public Object execute(String CmdID, Object CmdObj) throws RemoteException {
		if (CmdID.equals("GetLocalOS"))
			return C_GetLocalOS((GetLocalOS) CmdObj);
		else if (CmdID.equals("GetLocalTime"))
			return C_GetLocalTime((GetLocalTime) CmdObj);
		else if (CmdID.equals("GetVersion"))
			return C_GetVersion((GetVersion) CmdObj);
		System.out.println("\n\n    UNKNOWN COMMAND!\n\n\n");
		return null;
	}

	static {
		System.load(Agent.PATH + "/libglos.so");
		System.load(Agent.PATH + "/libglt.so");
		System.load(Agent.PATH + "/libgv.so");
	}
	
	native GetLocalTime C_GetLocalTime(GetLocalTime cmdObj);
	native GetLocalOS C_GetLocalOS(GetLocalOS cmdObj);
	native GetVersion C_GetVersion(GetVersion cmdObj);
}