import java.io.Serializable;

/*
	@Author Josh Loftus
	Object that represents the return of the matching C method ->
		Java_CmdAgentImpl_C_1GetLocalOS
	in glos.c
*/
public class GetLocalOS implements Serializable {

	String os;
	int valid;
	static final long serialVersionUID = 0;

	public GetLocalOS() {
		this.os = "";
		this.valid = 0;
	}
}