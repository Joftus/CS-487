// @Author: Josh Loftus (unless a method is tagged otherwise)
public class c_type {

	byte[] buf;

	public c_type() { this.buf = new byte[4]; }
	public int getSize() { return this.buf.length; }
	public void setValue(byte[] b) { this.buf = b; }
	public byte[] toByte() { return buf; }



	public c_int to_int() {
		c_int out = new c_int();
		out.setValue(buf);
		return out;
	}

	public c_char to_char() {
		c_char out = new c_char();
		out.setValue(buf);
		return out;
	}
}