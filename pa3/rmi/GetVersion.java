import java.io.Serializable;

/*
	@Author Josh Loftus
	Object that represents the return of the matching C method ->
		Java_CmdAgentImpl_C_1GetVersion
	in gv.c
*/
public class GetVersion implements Serializable {

	int version;
	int valid;
	static final long serialVersionUID = 0;

	public GetVersion() {
		this.version = 0;
		this.valid = 0;
	}
}