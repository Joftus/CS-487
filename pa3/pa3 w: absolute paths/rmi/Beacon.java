import java.util.Random;
import java.io.Serializable;
import java.rmi.RemoteException;

/*
	@Author Josh Loftus
	Beacon object that is sent between the Beacon Sender and the Beacon Listener Register using rmi
*/
public class Beacon implements Serializable {

	int id;
	long start_up_time;
	int time_interval;
	static final long serialVersionUID = 0;

	public Beacon(CmdRegister cmd_register) {
		Random random = new Random();
		this.id = random.nextInt(1000) + 1;
		GetLocalTime glt = new GetLocalTime();
		try {
			glt = (GetLocalTime) cmd_register.cmd_service.execute("GetLocalTime", glt);
		} catch(RemoteException e) { e.printStackTrace(); }
		this.start_up_time = glt.time;
		this.time_interval = Agent.TIME_INTERVAL;
	}
}