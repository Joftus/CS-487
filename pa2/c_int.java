import java.nio.ByteBuffer;

// @Author: Josh Loftus (unless a method is tagged otherwise)
public class c_int extends c_type {

	public c_int() {
		super();
	}

	public int getValue() {
		return ByteBuffer.wrap(this.buf).getInt();
	}

	public void setValue(int v) {
        byte[] result = new byte[4];
        result[0] = (byte) (v >> 24);
        result[1] = (byte) (v >> 16);
        result[2] = (byte) (v >> 8);
		result[3] = (byte) (v /*>> 0*/);
		this.buf = result;
	}

	@Override
	public String toString() {
		return "" +
			"  int: " + getValue() +
			"\n  byte[]: " + buf;
	}
}