Project Readme File

- Notable Variables
	- beacon port (UDP): 1234
	- command agent port (TCP): 1235 + number of previously connected agents
	- server ip: "127.0.0.1"

- Project Overview
	- See each read me for steps to run each module
	- Run the manager before starting an agent instance
	- Multiple agents can be run concurrently on the same manager instance

This project simulates a single manager receiving continuous beacons from each of
i agents to ensure they are still "alive". After an agent sends its initial beacon or 
the manager finds an agent with the same id but a different start time the command agent
is triggered on the agent-side to send its current system time and its operating system.
The beacon transmission functions by using a udp connection (unverified link). The command
agent uses a tcp connection (verified link) that is specific to each agent instance and uses
its own port. The manager notifies the user on a new agent connection, an agents death,
and the responses from the command agent sent from the agent-side.
