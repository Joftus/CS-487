#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <sys/utsname.h>
#include <unistd.h>
#include <string.h>
#include <pthread.h>
#include <time.h>

// GetLocalOS C equivelent
typedef struct {
	char OS[16];
	char valid[4];
} GET_LOCAL_OS;

// GetLocalTime C equivelent
typedef struct {
	int time;
	char valid[4];
} GET_LOCAL_TIME;


// all but last 3 method signatures (lines 25-29) were provided!
int read_one_byte(int client_socket, char* buffer);
int receiveFully(int client_socket, char* buffer, int length);
void printBinaryArray(char* buffer, int length);
int toInteger32(char* bytes);
void convertUpperCase(char* buffer, int length);
void GetLocalOS(GET_LOCAL_OS* ds);
void GetLocalTime(GET_LOCAL_TIME* ds);
void* CmdProcessor(int client_socket);



#define log 0
int port = 1234;
int max_clients = 500;	// set so high because I am unsure if this should even be tracked

int main(void) {
	printf("\nstarting server...\n");
	int server_socket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
	struct sockaddr_in sin;				// basic server socket init section...
	memset(&sin, 0, sizeof(sin));
	sin.sin_family = AF_INET;
	sin.sin_port = htons(port);
	sin.sin_addr.s_addr= INADDR_ANY;
	if (bind(server_socket, (struct sockaddr *)&sin, sizeof(sin)) < 0)
		printf("Error: failed to connect socket");
	listen(server_socket, max_clients);
	struct sockaddr client_addr;
	unsigned int client_len;
	int counter = 0;
	while(1) {			// command receive loop
		pthread_t thread_id;
		int client_socket = accept(server_socket, &client_addr, &client_len);
		printf("command %d received...\n", counter + 1);
		pthread_create(&thread_id, NULL, CmdProcessor, client_socket);
		pthread_join(thread_id, NULL);
		counter++;
		if (counter == max_clients) {
			printf("  maximum number of clients connected, server is closed!\n");
			break;
		}
	}
	return 0;
}

void* CmdProcessor(int client_socket) {
	char packet_length_bytes[4];
	receiveFully(client_socket, packet_length_bytes, 4);		// get length of buffer
	int packet_length = toInteger32(packet_length_bytes);
	char* buffer = (char *) malloc(packet_length);
	receiveFully(client_socket, buffer, packet_length);			// receive full buffer
	convertUpperCase(buffer, packet_length);
	const char* cmd1 = "GETLOCALOS";			// the two tags for the impl commands
	const char* cmd2 = "GETLOCALTIME";
	int out_packet_length;
	if (strcmp(cmd1, buffer) == 0) {			// command 1 handeling
		GET_LOCAL_OS ds;
		GetLocalOS(&ds);
		out_packet_length = (sizeof(char) * 16) + (sizeof(char) * 4);
		if (log == 1) {
			printf("  GetLocalOS\n");
			printf("    os: %s\n", ds.OS);
			printf("    valid: %s\n", ds.valid);
			printf("    packet length: %d\n", out_packet_length);
		}
		send(client_socket, &ds, out_packet_length, 0);
	}
	else if (strcmp(cmd2, buffer) == 0) {		// command 2 handeling
		GET_LOCAL_TIME ds;
		GetLocalTime(&ds);
		out_packet_length = sizeof(int) + (sizeof(char) * 4);
		if (log == 1) {
			printf("  GetLocalTime\n");
			printf("    time: %d\n", ds.time);
			printf("    valid: %s\n", ds.valid);
			printf("    packet length: %d\n", out_packet_length);
		}
		send(client_socket, &ds, out_packet_length, 0);
	}
	return NULL;
}

// method defined by hw specs
void GetLocalOS(GET_LOCAL_OS* ds) {
	struct utsname name;
	if(uname(&name)) exit(-1);
	strcpy((*ds).valid, "true");
	strcpy((*ds).OS, name.sysname);
}

// method defined by hw specs
void GetLocalTime(GET_LOCAL_TIME* ds) {
	(*ds).time = time(NULL);
	// (*ds).time = 1; // for testing
	strcpy((*ds).valid, "true");
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