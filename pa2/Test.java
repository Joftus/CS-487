// @Author: Josh Loftus (unless a method is tagged otherwise)
public class Test {
	/*
		Layout of project
			super class: RPC
				sub classes: GetLocalOS, GetLocalTime
			super class: c_type
				sub classes: c_int, c_char
	*/

	public final static String IP = "127.0.0.1";
	public final static int PORT = 1234;

	public static void main(String[] args) {
		RPC c1, c2;
		
		System.out.println("\nstarting tests...");
		c1 = new GetLocalOS();
		c2 = new GetLocalTime();
		
		// Testing GetLocalOS
		System.out.println("\n  (Test.java) GetLocalOS");
		c1.execute(IP, PORT);
		System.out.println("    os: " + c1.info.to_char().getValue());
		System.out.println("    valid: " + c1.valid.getValue());

		// Testing GetLocalTime
		System.out.println("\n  (Test.java) GetLocalTime");
		c2.execute(IP, PORT);
		System.out.println("    time: " + c2.info.to_int().getValue());
		System.out.println("    valid: " + c2.valid.getValue());
	}
}