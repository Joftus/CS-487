output: Agent.o AgentRunner.o Beacon.o BeaconSender.o CmdAgent.o
	g++ Agent.o AgentRunner.o Beacon.o BeaconSender.o CmdAgent.o -o agent -lm -pthread

Agent.o: Agent.cpp
	g++ -c Agent.cpp

AgentRunner.o: AgentRunner.cpp
	g++ -c AgentRunner.cpp

Beacon.o: Beacon.cpp
	g++ -c Beacon.cpp

BeaconSender.o: BeaconSender.cpp
	g++ -c BeaconSender.cpp

CmdAgent.o: CmdAgent.cpp
	g++ -c CmdAgent.cpp
	

clean:
	rm *.o agent
