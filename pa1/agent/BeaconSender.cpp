#include <stdlib.h>
#include <unistd.h>
#include <string>
#include <sstream>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>

#include "Beacon.cpp"

using namespace std;

// @Author: Josh Loftus
class BeaconSender {
	public:
		Beacon beacon;
		struct sockaddr_in sin;
		int udp_socket;
		string output;
		int buffer_size;
		char buffer[1024];
		int port;
		
		// init object to send to designated udp port w/ a large buffer
		BeaconSender() {
			buffer_size = 1024;
			port = 1234;
		}

		// core loop to execute the beacon sender functionality
		void run(Beacon _beacon) {
			beacon = _beacon;		// save the input beacon to this scope
			packetBuilder();		// build packet based of beacon parameter
			while(1) {
				sendUDP();			// send the packet created to the udp port on the manager side
				usleep(beacon.time_interval);		// sleep for the time interval shown in the beacon
			}
		}

		void packetBuilder() {
			ostringstream oss;		// put all beacon info into a single string to be sent, note the defined delimeters.
			oss << "ID: " << beacon.id << " ST: " << beacon.start_up_time << " IP: " << beacon.ip
					<< " TI: " << beacon.time_interval << " CP: " << beacon.cmd_port;
			output = oss.str();
		}

		// this method was provided by instructor, almost no changes made by me.
		void sendUDP() {
			if ( (udp_socket = socket(AF_INET, SOCK_DGRAM, 0)) < 0 )
				printf("\nERROR: BeaconSender.sendUDP() failed to create socket!");
			memset(&sin, 0, sizeof(sin)); 
			
			sin.sin_family = AF_INET; 
			sin.sin_port = htons(port); 
			sin.sin_addr.s_addr = INADDR_ANY; 
			int n, len;
			
			sendto(udp_socket, output.c_str(), strlen(output.c_str()), buffer_size, 
				(const struct sockaddr *) &sin, sizeof(sin)); 
		}
};
