import java.io.Serializable;

/*
	@Author Josh Loftus
	Object that represents the return of the matching C method ->
		Java_CmdAgentImpl_C_1GetLocalTime
	in glt.c
*/
public class GetLocalTime implements Serializable {

	public int time;
	public int valid;
	static final long serialVersionUID = 0;

	public GetLocalTime() {
		this.time = -1;
		this.valid = 0;
	}
}