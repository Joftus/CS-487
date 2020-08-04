// @Author: Josh Loftus (unless a method is tagged otherwise)
public class c_char extends c_type {

	public c_char() {
		super();
	}

	public String getValue() {
		return new String(buf);
	}

	public void setValue(String str) {
		this.buf = str.getBytes();
	}

	@Override
	public String toString() {
		return "" + 
			"  String: " + getValue() +
			"\n  byte[]: " + buf;
	}
}