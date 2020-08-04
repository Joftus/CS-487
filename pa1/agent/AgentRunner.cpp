#include <thread>
#include <iostream>

#include "Agent.cpp"

char ip_[] = {"127.0.0.1"};
int time_interval_ = 5000000;
int port_ = 1235;

using namespace std;

// main loop to run a single agent
// @Author: Josh Loftus
int main() {
	Agent agent(ip_, time_interval_, port_);
	agent.toString();							// show info about the newly created agent
	thread cmd_thread(&CmdAgent::run, &agent.cmd_agent);		// create a thread to run the cmd agent
	thread beacon_thread(&BeaconSender::run, &agent.beacon_sender, agent.getBeacon());	// create a thread to run the beacon sender
	cmd_thread.join();
	beacon_thread.join();
	return 0;
}

