rmi/jni Readme File

- Notable Variables
	Agent.java (time_interval for beacons)
	help.txt for build steps

- Project Overview
	- Manager
		- ManagerThread (Thread / RMI Client)
		- BeaconListenerRegister (Thread / RMI Server)
		- BeaconListenerImpl (RMI Object)
		- BeaconListener (RMI Interface)
		- Client (Object)
	- Agent
		- BeaconSender (Thread / RMI Client)
		- CmdRegister (Thread / RMI Server)
		- CmdAgentImpl (RMI Object)
		- CmdAgent (RMI Interface)
	- Shared
		- Beacon (Object)
		- GetLocalOS (Command)
		- GetLocalTime (Command)
		- GetVersion (Command)

- Summary
	This project use rmi skeletons / stubs (BeaconListener / BeaconSender) to pass beacon objects from any of the x Agents connected to the singleton Manager object
	to confirm there connection. When a new agent connects to the Manager, the Manager sends a request for the Agents OS, Time and 
	Version through using rmi skeletons / stubs (CmdAgent / ManagerThread). This makes the Agent, specifically CmdAgentImpl, call 3 methods implemented in C using jni
	(GetLocalTime.java => glt.c, GetLocalOS.java => glos.c, GetVersion => gv.c) which returns objects matching their java equivalents and returns them back through 
	CmdAgentImpl by rmi.
