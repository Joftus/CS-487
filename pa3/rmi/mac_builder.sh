javac *.java
javah CmdAgentImpl
javah BeaconListenerImpl
rmic BeaconListenerImpl
rmic CmdAgentImpl

JAVA_HOME=$($(dirname $(readlink $(which javac)))/java_home)
cc -fPIC -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin" -shared -o libglos.so glos.c
cc -fPIC -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin" -shared -o libglt.so glt.c
cc -fPIC -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin" -shared -o libgv.so gv.c
