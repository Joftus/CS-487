javac *.java
javah CmdAgentImpl
javah BeaconListenerImpl
rmic BeaconListenerImpl
rmic CmdAgentImpl

JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))
cc -fPIC -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -shared -o libglos.so glos.c
cc -fPIC -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -shared -o libglt.so glt.c
cc -fPIC -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -shared -o libgv.so gv.c
