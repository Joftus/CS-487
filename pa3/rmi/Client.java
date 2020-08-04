/*
	@Author Josh Loftus
	Manager side reference for an Agent, essentially a storage object for beacon info.
*/
public class Client {

	int id;
	long start_up_time;
	int time_interval;
	long last_beacon_time;

	public Client(int _id, long _start_up_time, int _time_interval) {
		this.id = _id;
		this.start_up_time = _start_up_time;
		this.time_interval = _time_interval;
		this.last_beacon_time = _start_up_time;
	}
}