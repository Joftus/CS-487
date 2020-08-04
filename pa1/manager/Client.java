// interchangeably used with the term agent
// uncomment out the log() line if you want details about the new client / the data received through the beacon
public class Client {
	
	int id, time_interval, tcp_port;
	long start_up_time;
	String ip;

	// an object created to store data sent from the beacon and to track the beacon times and tcp port used;
	public Client(int id, long start_up_time, String ip, int time_interval, int tpc_port) {
		this.id = id;
		this.start_up_time = start_up_time;
		this.time_interval = time_interval / 1000;
		this.ip = ip;
		this.tcp_port = tpc_port;
		// log();
	}

	void log() {
		System.out.println("\n-------| Client Log |-------");
		System.out.println("ID: " + id);
		System.out.println("Start Time: " + start_up_time);
		System.out.println("Time Interval: " + time_interval + "ms");
		System.out.println("IP: " + ip);
		System.out.println("TCP Port: " + tcp_port + "\n");
	}
}