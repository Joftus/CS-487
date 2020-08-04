import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// @Author: Josh Loftus (unless a method is tagged otherwise)
public class RPC {

	String command;
	c_type info;
	c_char valid;

	// params are the command signature and the return type of the command to be stored in info
	public RPC(String command, c_type info) {
		this.command = command;
		this.info = info;
		this.valid = new c_char();
		this.valid.setValue("false");
	}

	public void execute(String IP, int port) {
		// System.out.println("  executing " + command + "...");
		byte[] buf, buf_info, buf_in;
		Socket socket;
		DataInputStream input_stream;
		DataOutputStream output_stream;

		buf = build_buffer();
		try {
			socket = new Socket(IP, port);
			input_stream = new DataInputStream(socket.getInputStream());
			output_stream = new DataOutputStream(socket.getOutputStream());
			buf_info = getBytes(buf.length);
			output_stream.write(buf_info, 0, buf_info.length);
			output_stream.write(buf, 0, buf.length);
			output_stream.flush();
			while (input_stream.available() == 0)
				Thread.sleep(1000);
			
			buf_in = new byte[input_stream.available() - 4];	// buffer for info
			input_stream.read(buf_in);
			if (info instanceof c_int)		// need specific handel because I was getting endian errors (1 == 2^24)
				((c_int) info).setValue(ByteBuffer.wrap(buf_in).order(ByteOrder.LITTLE_ENDIAN).getInt());
			else
				info.setValue(buf_in);
			buf_in = new byte[input_stream.available()];		// buffer for valid
			input_stream.read(buf_in);
			valid.setValue(new String(buf_in));
		}
		catch (IOException e1) { e1.printStackTrace(); }
		catch (InterruptedException e2) { e2.printStackTrace(); }
	}

	// builds the buffer for output_stream.write() derived from assignment specs
	// 100 for command, 4 for length of info, x for info
	private byte[] build_buffer() {
		int index, ptr;
		byte[] buf, length_b, command_b, info_b, valid_b;
		
		index = 0;
		length_b = getBytes(this.info.getSize() + this.valid.getSize());
		info_b = this.info.toByte();
		valid_b = this.valid.toByte();
		command_b = command.getBytes();
		buf = new byte[100 + 4 + this.info.getSize() + this.valid.getSize()];	// 100 for header, 4 for length, length for method

		for (index = 0; index < command_b.length; index++)
			buf[index] = command_b[index];
		ptr = 100;
		for (index = ptr; index < 104; index++)
			buf[index] = length_b[index-ptr];
		ptr += length_b.length;
		for (; index-ptr < info_b.length; index++)
			buf[index] = info_b[index-ptr];
		ptr += info_b.length;
		for (; index-ptr < valid_b.length; index++)
			buf[index] = valid_b[index-ptr];
		return buf;
	}

	// not written by me! provided by the instructors
	private static byte[] getBytes(int v) {
        byte[] result = new byte[4];
        result[0] = (byte) (v >> 24);
        result[1] = (byte) (v >> 16);
        result[2] = (byte) (v >> 8);
		result[3] = (byte) (v /*>> 0*/);
		return result;
	}
}