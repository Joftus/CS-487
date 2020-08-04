// class not created by me!
class Beacon {
	public:
		int id;
		int start_up_time;
		char* ip;
		int time_interval;
		int cmd_port;

		// beacon object outlined in the hw
		Beacon() {}

		Beacon(int _id, int _start_up_time, char* _ip, int _time_interval, int _cmd_port) {
			id = _id;
			start_up_time = _start_up_time;
			ip = _ip;
			time_interval = _time_interval;
			cmd_port = _cmd_port;
		}
};