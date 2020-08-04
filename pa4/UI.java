// Author: Joshua Loftus 4/20/2020
import java.io.*;
import java.util.*;

/*
UI that tries to mirror the functionality described in /storage/Gnutella.pdf whenever the phrase
"can choose" or "the user" was used. commands are outlined in README.txt under "UI overview".
*/
public class UI extends Thread {

	Node node;
	String[] commands;
	Queue<String> delayed_msgs;

	private static final char[] RESTRICTED_CHARACTERS = new char[] {'+', '-', ':', '@', '/', '(', ')'};
	private static final String NEW_PAGE = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
	static final String TAB = "    ";
	static final String DTAB = "        ";

	public UI(Node _node) {
		node = _node;
		commands = new String[] {"quit", "search", "add file", "remove file", "user info visibility", "directory info visibility", "possible neighbor visibility", "add neighbor"};
		delayed_msgs = new LinkedList<String>();
	}

	@SuppressWarnings({"unused", "resource"})
	public void run() {
		Scanner scanner;
		Random random;
		String user_input, file_chosen, restricted_msg;
		boolean user_info, directory_info, possible_neighbors;

		scanner = new Scanner(System.in);
		user_info = true;
		directory_info = true;
		possible_neighbors = true;

		restricted_msg = "RESTRICTED CHARACTERS: ";
		for (char c : RESTRICTED_CHARACTERS)
			restricted_msg += c + " ";
		delayed_msgs.add(restricted_msg);
		while (true) {
			user_input = "";
			file_chosen = "";
			random = new Random();
			System.out.print(NEW_PAGE);
			if (!delayed_msgs.isEmpty()) {
				System.out.println("SYSTEM MESSAGES");
				while (!delayed_msgs.isEmpty())
					System.out.println(TAB + delayed_msgs.remove());
			}
			if (user_info)
				System.out.println(node.toString());
			if (possible_neighbors)
				System.out.println(node.possible_toString());
			if (directory_info)
				System.out.println(Directory.static_toString());
			System.out.println("COMMANDS");
			for (String cmd : commands)
				System.out.println(TAB + cmd);
			System.out.print("action: ");
			user_input = protector(scanner.nextLine());
			
			if (user_input.equals(commands[0]))
				break;
			else if (user_input.equals(commands[1])) {
				System.out.println("System Note: enter \"back\" to return to main menu...");
				while (true) {
					System.out.print("search: ");
					file_chosen = protector(scanner.nextLine());
					if (!file_chosen.equals("back")) {
						node.search = file_chosen;
						System.out.println("max search time: " + Directory.SEARCH_TIME + " seconds!");
						System.out.print("waiting");
						while (!node.found && !node.failed) {
							System.out.print(".");
							try {
								sleep(1000);
							} catch (InterruptedException e) {}
						}
						if (node.found) {
							delayed_msgs.add("\nfile was downloaded as copy_" + file_chosen + " in the project folder!");
						}
						else
							delayed_msgs.add(file_chosen + " was not found in network");
						node.found = false;
						node.failed = false;
						break;
					}
					else
						break;
				}
			}
			else if (user_input.equals(commands[2])) {
				System.out.println("System Note: enter \"back\" to return to main menu...");
				while (true) {
					int size;
					System.out.print("add file: ");
					file_chosen = protector(scanner.nextLine());
					
					if (!file_chosen.equals("back")) {
						try {
							File file = new File(file_chosen);
							FileInputStream checker = new FileInputStream(file);
							size = (int) file.length();
							Protocol.push(node, file_chosen, size);
							delayed_msgs.add("added a file named " + file_chosen + " of size " + size + "!");
							break;
						} catch (FileNotFoundException e) {
							System.out.println("file not found in project folder, failed to add it...");
						}
					}
					else
						break;
				}
			}
			else if (user_input.equals(commands[3])) {
				System.out.println("System Note: enter \"back\" to return to main menu...");
				while (true) {
					System.out.print("remove file: ");
					file_chosen = protector(scanner.nextLine());
					if (file_chosen.equals("back"))
						break;
					else if (node.files.keySet().contains(file_chosen)) {
						Protocol.pull(node, file_chosen);
						System.out.println(file_chosen + " has been removed!");
						break;
					}
					else
						System.out.println("INVALID FILE NAME ENTERED!"); 
				}
			}
			else if (user_input.equals(commands[4])) {
				System.out.print("user info on or off: ");
				user_input = protector(scanner.nextLine());
				if (user_input.equals("on"))
					user_info = true;
				else
					user_info = false;
			}
			else if (user_input.equals(commands[5])) {
				System.out.print("directory info on or off: ");
				user_input = protector(scanner.nextLine());
				if (user_input.equals("on"))
					directory_info = true;
				else
					directory_info = false;
			}
			else if (user_input.equals(commands[6])) {
				System.out.print("possible neighbor info on or off: ");
				user_input = protector(scanner.nextLine());
				if (user_input.equals("on"))
					possible_neighbors = true;
				else
					possible_neighbors = false;
			}
			else if (user_input.equals(commands[7])) {
				System.out.print("add neighbor (port): ");
				user_input = protector(scanner.nextLine());
				Protocol.add_neighbors_neighbor(node, node.ip, node.port, Integer.parseInt(user_input));
				delayed_msgs.add("if a node exists on port " + user_input + " it will be added to your neighbors list!");
			}
			else {
				delayed_msgs.add("INVALID COMMAND ENTERED!");
			}
		}
		node.kill = true;
		try {
			sleep(Node.SLEEP_TIME * 2);
		} catch (InterruptedException e) {}
		scanner.close();
		System.out.println("good bye!");
		System.exit(1);
	}

	static String protector(String input) {
		String output;
		boolean remove;

		output = "";
		for (char c : input.toCharArray()) {
			remove = false;
			for (char unsafe : RESTRICTED_CHARACTERS) {
				if (c == unsafe)
					remove = true;
			}
			if (!remove)
				output += c;
			else
				output += " ";
		}
		return output;
	}
}