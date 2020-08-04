server: server.o
	gcc server.o -o server -lm -pthread

server.o: server.c
	gcc -c server.c

clean:
	rm *.o server
