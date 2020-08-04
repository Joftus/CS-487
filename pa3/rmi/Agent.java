/*
	@Author Josh Loftus
	Driver class for Agent, inits both the command register and the beacon sender.
*/
public class Agent {
	
	public static final int TIME_INTERVAL = 2000;
	public static final int LOOP_TIME = 500;
	public static String PATH;
	//static { System.setProperty("java.library.path", "./"); }


	public Agent() {
		CmdRegister cmd_register = new CmdRegister();
		BeaconSender beacon_sender = new BeaconSender(cmd_register);

		beacon_sender.start();
		cmd_register.start();
	}

	public static void main(String[] args) {
		System.out.println("\nstarting agent...");
		if (args.length > 0) {
			PATH = args[0];
			System.out.println("  path: " + PATH);
		}
		new Agent();
	}
}