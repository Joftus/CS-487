README

Note:
	- This program matches the assignment specs except the valid value
		is set to false in the constructor of RPC rather than in 
		Test.java main method because this is always assumed to begin as false.
	- To view details about the actual values on the server-side change "log" to 1
		on line 34 of server.c.
	


To Run
	- server side (c)
		1. make clean
		2. make
		3. ./server
	- command side (java)
		1. java Test
		*. if that fails the below line will recompile the project to guarantee "java Test" execution...
			javac c_int.java && javac c_char.java && javac c_type.java && javac GetLocalOS.java && javac GetLocalTime.java && javac RPC.java && javac Test.java



Think Further Responses!!!
	1. A new command needs to be added?
		Java: The commands and transmission of said command are abstracted through
			the RPC superclass. Using c_type allows easy implementation of new return types
			since RPC.info (i.e. the return of the command) uses this generic c_type. Finally,
			new implementation of a command could easily be derived from the minimal code used
			for GetLocalOS and GetLocalTime.
		C: add strcmp() for the command tag and then implement the functionality in server.c.
	2. An existing command need to be deleted?
		Java: the command is contained within a single class therefor all that needs to be done
			is delete said class and wipe any tests containing a reference to it.
		C: delete the block of code following the strcmp() check.
	3. Some parameters to a command need to be changed?
		a. Add a new field...
		b. Delete an existing field...
		c. Change the type of existing field...
			Java: changes to RPC.bufferBuilder() would be needed and an overload of RPC.execute() required.
			C: support input of params from readfully() and parse it for each specific method.
			*: I didn't explore parameter flexibility in this project as it seems to be beyond the scope
				as all of our commands have empty parameter fields and instead rely on system level info.