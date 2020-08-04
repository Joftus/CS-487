To Run Project
	1. java Directory
	2-x. java Node {port number}
	*I have added two files named test.png and test.txt that can be added through the ui and downloaded
		by another Node in the network*



UI overview
	restricted characters: '+', '-', ':', '@', '/', '(', ')'
		these are used as delimiters or signals for the protocols, if entered through the ui they will be replaced with ' ' using UI.protector().
	simple command line ui that takes text input for commands...
		1. "quit"
			A safe way to deactivate a node because then it sends out a udp packet telling all of its
			neighbors to replace the node as it has gone offline.
		2. "search"
			This input triggers the query protocol and then subsequent query propagation from the source
			node neighbors to their neighbors. To stop the network from being flooded with a single query
			it is only sent to Directory.SEARCH_DEPTH number of neighbor lists. Another limiting factor to
			queries is Directory.SEARCH_TIME which is TTL essentially. If the query responds with HIT header
			the node will download the file over a tcp connection with the node that has the file, saved as
			"copy_" + file name.
		3. "add file"
			This input triggers the push protocol, it only works for files in the project directory.
			This makes a file publicly available for other nodes to search for and then download.
		4. "remove file"
			This input triggers the pull protocol I made to let a node user remove
			a file previously marked as available for others to download.
		5. "user info visibility"
			default: on
			Shows node data in the ui.
		6. "directory info visibility"
			default: on
			Shows static Directory variables that could be useful to be aware and/or change for testing.



port range (required Node argument) 
	1235-2234
	This range is defined because tcp ports are found by doing (node port += Directory.TCP_OFFSET i.e. += 1000)



General Process Overview
	1. Node.init() is run which sends a message to the Directory, udp server on port 1234, to register it as a new node and starts
		up its own Udp server on the port specified by main args[0].
	2. The Directory (singleton) then looks to connect that node with another x number of random nodes (amount is 
		specified by the Directory.INITIAL_NEIGHBORS variable) in the host_cache, which is a list of ips/udp port numbers of
		all known nodes.
	3. The Directory sends out udp msgs to each of the neighbors with the port number and ip address of the new node.
		The new node receives a udp msg containing the number of neighbors to expect.
	4. The new node runs the verify_links protocol which starts a tcp server. Each prospective neighbor
		runs the verify_link_resident protocol where they join the new nodes tcp connection and swap 
		predetermined Strings and then the resident node sends its ip and port to the new node.
	5. They have now established that they are each others neighbors and will ping and pong back and forth until
		one goes offline. They will also be each others target for sending queries when searching for files.



Project Overview
	This project is a Gnutella network emulator. The directory must be started first and is a singleton
	object, it is only used to connect new nodes to the network and replace lost neighbors when a node quits.
	The number of nodes is only limited by the number of ports which is ~1000. Each node when newly connected
	pings the Directory to make its first links to other nodes in the network, after this stage without input through
	the ui the node just sends/receives pings and pongs from its neighbors. With input from the outcomes are
	detailed in the paragraph above titled UI overview. The general functionality of the project is nodes having
	the ability to ping and pong neighbors, send search queries when looking for a file, respond to other nodes
	queries with propagation of said query, running the hit protocol, or stopping the query if the search depth 
	or time limit have been passed. On the event of the hit protocol being run the file is automatically downloaded
	from the source of the hit message to the source of the query as "copy_" + file name. This operation is limited 
	to the files in the project folder.



Message Formats (w/o "" means it is a variable)
	Protocol.java (msg formatting in *_helper() methods for each type of msg)
		Ping: "PING"ip":"port"/:"file_name"("file_size")"...
			after "/" is the list of files, the ... signifies the unknown size
		Pong: "PONG"ip":"port"/@"ip":"port...
			after "/" is the list of neighbors, the ... signifies the unknown size
		Query: "QUERY(A)"ip":"port"(S)"file_name"(N)"id"(T)"depth":"start_time
			TTL is calculated based off start_time
		Hit: "HIT"ip":"port
		Kill: "-"ip":"port
			used to disconnect node from network

	Directory.java
		established node add new node: "+"ip":"port