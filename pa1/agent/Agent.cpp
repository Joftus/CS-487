#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "BeaconSender.cpp"
#include "CmdAgent.cpp"

// @Author: Josh Loftus
class Agent {
	public:
		BeaconSender beacon_sender;
		CmdAgent cmd_agent;
		int id;
		int start_up_time;
		char* ip;
		int time_interval;
		int cmd_port;

		Agent(char _ip[4], int _time_interval, int _port) {
			srand(time(NULL));
			id = (rand() % 10000) + 1;				// randomly generate the id
			start_up_time = time(0);				// set the startup time of the agent
			ip = _ip;								// ip / time interval / udp port can all be changed for all agents in AgentRunner.cpp
			time_interval = _time_interval;			
			cmd_port = _port;
		}

		Beacon getBeacon() {
			Beacon out(id, start_up_time, ip, time_interval, cmd_port);		// complie beacon "object" based off this agents info
			return out;
		}

		void toString() {
			printf("\n-------| Agent Log |-------");
			printf("\n	ID: %d", id);
			printf("\n	Start Time: %d", start_up_time);
			printf("\n	Time Interval: %d", time_interval / 1000);
			printf("\n	IP: %s", ip);
		}
};
