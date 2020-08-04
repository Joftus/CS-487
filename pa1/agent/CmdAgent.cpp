#include <ctime>
#include <stdio.h>
#include <string>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <cstring>
#include <sys/utsname.h>
#include <unistd.h>
#include <sstream>

using namespace std;

// all but last 2 methods in this list were used in the TcpServer and are not mine
int read_one_byte(int client_socket, char *buffer);
int receiveFully(int client_socket, char *buffer, int length);
void printBinaryArray(char *buffer, int length);
int toInteger32(char *bytes);
void convertUpperCase(char *buffer, int length);
void GetLocalOS(char OS[16], int *valid);
void GetLocalTime(int* t, int *valid);

// represents the cmd agent from hw specs
// @Author: Josh Loftus (except the methods mentioned above)
class CmdAgent {
	public:
		int port;
		int max_clients;

		CmdAgent() {
			port = 1235;
			max_clients = 5;
		}

		// most of the lines involving sockets are from the TcpServer provided by instructor
		void run() {
			int server_socket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
			struct sockaddr_in sin;
			memset(&sin, 0, sizeof(sin));
			sin.sin_family = AF_INET;
			while (1) {
				sin.sin_port = htons(port);
				sin.sin_addr.s_addr= INADDR_ANY;
				if (bind(server_socket, (struct sockaddr *)&sin, sizeof(sin)) < 0)			// increment port number until it matches the agent specific one decided by the manager
					port += 1;
				else break;
			}
			printf("\n TCP port: %d", port);
			listen(server_socket, 5);
			int counter = 0;
			struct sockaddr client_addr;
			unsigned int client_len;
			int client_socket = accept(server_socket, &client_addr, &client_len);
			for (int i = 0; i < 2; i++){			// two comands so expect 
				char packet_length_bytes[4];
				receiveFully(client_socket, packet_length_bytes, 4);		// receive the cmds from BeaconListener.java (in the cmdAgentTrigger())
				int packet_length = toInteger32(packet_length_bytes);
				char *buffer = (char *)malloc(packet_length);
				receiveFully(client_socket, buffer, packet_length);
				convertUpperCase(buffer, packet_length);

				const char* cmd1 = "GETLOCALOS()";
				const char* cmd2 = "GETLOCALTIME()";
				int send_packet_length;
				if (strcmp(cmd1, buffer) == 0) {						// check if its the first or 2nd command
					char* packet;
					int valid1 = 0;
					char OS[16];
					send_packet_length = sizeof(char) * 16;
					packet = (char*) malloc(sizeof(char) * 16);
					GetLocalOS(OS, &valid1);
					if (valid1 == 1) {							// checks validity of the OS
						strcpy(packet, OS);
						send(client_socket, packet, send_packet_length, 0);		// sends info from the GetLocalOS() command, line 93
					}
					else printf("\nGetLocalOS() did not return a valid result");
				}
				else if (strcmp(cmd2, buffer) == 0) {						// check if its the first or 2nd command
					int valid2 = 0;
					int* time = (int*) malloc(sizeof(int) * 32);
					send_packet_length = sizeof(int) * 32;
					GetLocalTime(time, &valid2);
					if (valid2 == 1) {							// checks validity of the time
						stringstream ss;
						// printf("  time: %d\n", *time);
						ss << *time;
						send(client_socket, ss.str().c_str(), send_packet_length, 0);		// sends info from the GetLocalTime() command, line 100
					}
					else printf("\nGetLocalTime() did not return a valid result");
				}
			}
		}

		// method defined by hw specs
		void GetLocalOS(char OS[16], int *valid) {
			struct utsname name;
			if(uname(&name)) exit(-1);
			*(valid) = 1;
			strcpy(OS, name.sysname);
		}

		// method defined by hw specs
		void GetLocalTime(int *t, int *valid) {
			*t = (int) time(0);
			//printf("\nt: %d", *t);
			*(valid) = 1;
		}

		// this method and all below were not written by me!
		void convertUpperCase(char *buffer, int length) {
			int i = 0;
			while (i < length) {
				if (buffer[i] >= 'a' && buffer[i] <= 'z')
					buffer[i] = buffer[i] - 'a' + 'A'; 
				i++;
			}
		}

		int receive_one_byte(int client_socket, char *cur_char) {
			ssize_t bytes_received = 0;
			while (bytes_received != 1)
				bytes_received = recv(client_socket, cur_char, 1, 0);
			
			return 1;
		}

		int receiveFully(int client_socket, char *buffer, int length) {
			char *cur_char = buffer;
			ssize_t bytes_received = 0;
			while (bytes_received != length) {
				receive_one_byte(client_socket, cur_char);
				cur_char++;
				bytes_received++;
			}
			return 1;
		}

		void printBinaryArray(char *buffer, int length) {
			int i=0;
			while (i<length) {
				printf("%d ", buffer[i]);
				i++;
			}
			printf("\n");
		}

		
		int toInteger32(char *bytes) {
			int tmp = (bytes[0] << 24) + 
					(bytes[1] << 16) + 
					(bytes[2] << 8) + 
					bytes[3];
		
			return tmp;
		}
};
