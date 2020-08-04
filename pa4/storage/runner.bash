# only works on MAC

javac *.java

osascript -e 'tell application "Terminal" to do script "cd ~/Desktop/3.2/Workspace/cs487/pa4/ && java Directory"'
sleep 2

osascript -e 'tell application "Terminal" to do script "cd ~/Desktop/3.2/Workspace/cs487/pa4/ && java Node 1235"'
sleep 10

osascript -e 'tell application "Terminal" to do script "cd ~/Desktop/3.2/Workspace/cs487/pa4/ && java Node 1236"'
sleep 10

osascript -e 'tell application "Terminal" to do script "cd ~/Desktop/3.2/Workspace/cs487/pa4/ && java Node 1237"'