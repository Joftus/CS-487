import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.lang.Object;

/*
	@Author Josh Loftus
	RMI object also used to access jni using the 3 native methods found at the bottom
	of the class. I had problems starting on line 27 with using loadLibrary so I have
	only gotten it to work using an absolute path to the libraries.
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
		// used absolute path beacuse of unresolved error: "invalid ELF header" (Possible cause: endianness mismatch)
		System.setProperty("java.library.path", "/home/jtloftus/cs487/PA 3/rmi");
		System.load("/home/jtloftus/cs487/PA 3/rmi/libglos.so");
		System.load("/home/jtloftus/cs487/PA 3/rmi/libglt.so");
		System.load("/home/jtloftus/cs487/PA 3/rmi/libgv.so");

		/*System.loadLibrary("glos");
		System.loadLibrary("gv");
		System.loadLibrary("glt");*/

		/*
		// personal os absolute path
		System.setProperty("java.library.path", "/Users/joshloftus/Desktop/3.2/Semester/CS 487/PA 3/rmi/");
		System.load("/Users/joshloftus/Desktop/3.2/Semester/CS 487/PA 3/rmi/libglos.so");
		System.load("/Users/joshloftus/Desktop/3.2/Semester/CS 487/PA 3/rmi/libgv.so");
		System.load("/Users/joshloftus/Desktop/3.2/Semester/CS 487/PA 3/rmi/libglt.so");
		*/
	}
	
	native GetLocalTime C_GetLocalTime(GetLocalTime cmdObj);
	native GetLocalOS C_GetLocalOS(GetLocalOS cmdObj);
	native GetVersion C_GetVersion(GetVersion cmdObj);
}