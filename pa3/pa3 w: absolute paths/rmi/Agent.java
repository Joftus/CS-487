/*
	@Author Josh Loftus
	Driver class for Agent, inits both the command register and the beacon sender.
*/
public class Agent {
	
	public static final int TIME_INTERVAL = 5000;

	public Agent() {
		CmdRegister cmd_register = new CmdRegister();
		BeaconSender beacon_sender = new BeaconSender(cmd_register);

		beacon_sender.start();
		cmd_register.start();
	}

	public static void main(String[] args) {
		System.out.println("\nstarting agent (java)...");
		new Agent();
	}
}