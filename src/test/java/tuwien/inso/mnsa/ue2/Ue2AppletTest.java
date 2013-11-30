package tuwien.inso.mnsa.ue2;

import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;

import javacard.framework.AID;
import javacard.framework.Applet;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.licel.jcardsim.base.Simulator;

@SuppressWarnings("restriction")
public class Ue2AppletTest {

	private final static AID aid = new AID(b("8985C0E78B8117F6D8"), (short) 0, (byte) 9);

	private static final Class<? extends Applet> clazz = Ue2Applet.class;

	private Simulator simulator;

	@Before
	public void setUp() throws Exception {
		simulator = new Simulator();

		simulator.installApplet(aid, clazz);
		simulator.selectApplet(aid);
	}

	@After
	public void tearDown() throws Exception {
		simulator.reset();
		simulator.resetRuntime();
	}

	@Test
	public void testAdd1() {
		assertAPDUResponse("0x00 0x01 0x04 0x06 0x00", "0x0a 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x01 0x00 0x00 0x00", "0x00 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x01 0x7f 0x01 0x00", "0x80 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x01 0x7f 0x02 0x00", "0x81 0x00", "0x90 0x00");
	}

	@Test
	public void testSub1() {
		assertAPDUResponse("0x00 0x02 0xb0 0xa1 0x00", "0x0f 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x02 0xA3 0xA0 0x00", "0x03 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x02 0xf5 0xf4 0x00", "0x01 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x02 0x04 0x06 0x00", "0xfe 0xff", "0x90 0x00");
		assertAPDUResponse("0x00 0x02 0xb0 0xb1 0x00", "0xff 0xff", "0x90 0x00");
		assertAPDUResponse("0x00 0x02 0xf5 0xf6 0x00", "0xff 0xff", "0x90 0x00");
	}

	@Test
	public void testMul1() {
		assertAPDUResponse("0x00 0x03 0x00 0xff 0x00", "0x00 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x03 0x02 0x0a 0x00", "0x14 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x03 0x13 0x02 0x00", "0x26 0x00", "0x90 0x00");
	}

	@Test
	public void testAnd() {
		assertAPDUResponse("0x00 0x05 0x00 0xff 0x00", "0x00 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x05 0xf1 0xa9 0x00", "0xa1 0xff", "0x90 0x00");
		assertAPDUResponse("0x00 0x05 0x87 0x53 0x00", "0x03 0x00", "0x90 0x00");
	}

	@Test
	public void testOr() {
		assertAPDUResponse("0x00 0x06 0x00 0xff 0x00", "0xff 0xff", "0x90 0x00");
		assertAPDUResponse("0x00 0x06 0x00 0x7f 0x00", "0x7f 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x06 0xf1 0xa9 0x00", "0xf9 0xff", "0x90 0x00");
		assertAPDUResponse("0x00 0x06 0x87 0x53 0x00", "0xd7 0xff", "0x90 0x00");
		assertAPDUResponse("0x00 0x06 0x77 0x53 0x00", "0x77 0x00", "0x90 0x00");
	}

	@Test
	public void testNot() {
		assertAPDUResponse("0x00 0x07 0x00 0x00 0x00", "0xff 0xff", "0x90 0x00");
		assertAPDUResponse("0x00 0x07 0xff 0x00 0x00", "0x00 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x07 0xf1 0x00 0x00", "0x0e 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x07 0x87 0x00 0x00", "0x78 0x00", "0x90 0x00");
		assertAPDUResponse("0x00 0x07 0x02 0x00 0x00", "0xfd 0xff", "0x90 0x00");
	}

	@Test
	public void testInvalidINS() {
		assertAPDUResponse("0x00 0x04 0x00 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0x13 0xff 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0x37 0xf1 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0xCA 0x87 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0xFE 0x02 0x00 0x00", "", "0x6d 0x00");
	}

	private void assertAPDUResponse(String request, String responseData,
			String responseStatus) {
		assertAPDUResponse(b(request), b(responseData), b(responseStatus));
	}

	private void assertAPDUResponse(byte[] request, byte[] responseData,
			byte[] responseStatus) {
		ResponseAPDU response = simulator.transmitCommand(new CommandAPDU(
				request));

		assertArrayEquals(responseData, response.getData());
		assertEquals(responseStatus[0], (byte) response.getSW1());
		assertEquals(responseStatus[1], (byte) response.getSW2());
	}

	// convenience method: converts hex-string to bytes
	// ignores spaces, 'x' letters and zeroes before 'x' letters
	// ignores case of hexadecimal digits (a..f == A..F)
	// whenever two digits are found, a byte is created
	// single-digit numbers (0, 1, ..., f) are not supported
	// and must be noted as 00, 01, ..., 0f
	private static byte[] b(String data) {
		List<Byte> bytes = new LinkedList<>();
		Character firstDigit = null;

		for (char c : data.toCharArray()) {
			if (c == 'x') {
				if (firstDigit == null || firstDigit == '0')
					firstDigit = null;
				else
					throw new InputMismatchException(
							"invalid 'x' character found");
			} else if (isHexDigit(c)) {
				if (firstDigit == null)
					firstDigit = c;
				else {
					bytes.add(getByte(firstDigit, c));
					firstDigit = null;
				}
			} else if (Character.isWhitespace(c)) { // nop
			} else
				throw new InputMismatchException("invalid character ('" + c
						+ "') found");
		}

		byte[] primitive = new byte[bytes.size()];
		for (int i = 0; i < primitive.length; i++)
			primitive[i] = bytes.get(i);

		return primitive;
	}

	private static byte getByte(char msb, char lsb) {
		return Integer.valueOf(msb + "" + lsb, 16).byteValue();
	}

	// for performance reasons
	private static final String hexAlphabet = "0123456789ABCDEFabcdef";

	private static boolean isHexDigit(char c) {
		return hexAlphabet.indexOf(c) >= 0;
	}
}
