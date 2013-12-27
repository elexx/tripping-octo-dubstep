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

	private final static String OxADD = Ox(Ue2Applet.ADD);
	private final static String OxSUB = Ox(Ue2Applet.SUB);
	private final static String OxMUL = Ox(Ue2Applet.MUL);
	private final static String OxAND = Ox(Ue2Applet.AND);
	private final static String OxOR = Ox(Ue2Applet.OR);
	private final static String OxNOT = Ox(Ue2Applet.NOT);

	private final static boolean ALLOW_NEGATIVE_INPUT = Ue2Applet.ALLOW_NEGATIVE_INPUT;
	private final static boolean ALLOW_NEGATIVE_OUTPUT = Ue2Applet.ALLOW_NEGATIVE_OUTPUT;

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

	@SuppressWarnings("unused")
	@Test
	public void testAdd1() {
		assertAPDUResponse("0x00 " + OxADD + " 0x04 0x06 0x00", "0x0a 0x00", "0x90 0x00"); // 04 +  06 =  0a
		assertAPDUResponse("0x00 " + OxADD + " 0x62 0x62 0x00", "0xc4 0x00", "0x90 0x00"); // 62 +  62 =  c4
		assertAPDUResponse("0x00 " + OxADD + " 0x7f 0x7f 0x00", "0xfe 0x00", "0x90 0x00"); // 62 +  62 =  c4
		assertAPDUResponse("0x00 " + OxADD + " 0x00 0x00 0x00", "0x00 0x00", "0x90 0x00"); // 00 +  00 =  00
		assertAPDUResponse("0x00 " + OxADD + " 0x7f 0x01 0x00", "0x80 0x00", "0x90 0x00"); // 7f +  01 =  80
		assertAPDUResponse("0x00 " + OxADD + " 0x7f 0x02 0x00", "0x81 0x00", "0x90 0x00"); // 7f +  02 =  81
		assertAPDUResponse("0x00 " + OxADD + " 0x7f 0x7f 0x00", "0xfe 0x00", "0x90 0x00"); // 7f +  7f =  fe

		if (ALLOW_NEGATIVE_INPUT) {
			assertAPDUResponse("0x00 " + OxADD + " 0x05 0xff 0x00", "0x04 0x00", "0x90 0x00"); // 05 + -01 =  04
			assertAPDUResponse("0x00 " + OxADD + " 0x7f 0xf0 0x00", "0x6f 0x00", "0x90 0x00"); // 7f + -10 =  6f
		} else {
			assertAPDUResponse("0x00 " + OxADD + " 0x05 0xff 0x00", "", "0x6a 0x86");
			assertAPDUResponse("0x00 " + OxADD + " 0x7f 0xf0 0x00", "", "0x6a 0x86");
		}

		if (ALLOW_NEGATIVE_INPUT && ALLOW_NEGATIVE_OUTPUT) {
			assertAPDUResponse("0x00 " + OxADD + " 0x05 0xf0 0x00", "0xf5 0xff", "0x90 0x00"); // 05 + -10 = -0b (0xff f5 signed short)
		} else {
			assertAPDUResponse("0x00 " + OxADD + " 0x05 0xf0 0x00", "", "0x6a 0x86");
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testSub1() {
		assertAPDUResponse("0x00 " + OxSUB + " 0x43 0x40 0x00", "0x03 0x00", "0x90 0x00"); // 43 -  40 =  03

		if (ALLOW_NEGATIVE_OUTPUT) {
			assertAPDUResponse("0x00 " + OxSUB + " 0x04 0x06 0x00", "0xfe 0xff", "0x90 0x00"); //  04 -  06 = -02 (0xff 0xfe signed short)
			assertAPDUResponse("0x00 " + OxSUB + " 0x00 0x06 0x00", "0xfa 0xff", "0x90 0x00"); //  00 -  06 = -02 (0xff 0xfa signed short)
		} else {
			assertAPDUResponse("0x00 " + OxSUB + " 0x04 0x06 0x00", "", "0x6a 0x86");
			assertAPDUResponse("0x00 " + OxSUB + " 0x00 0x06 0x00", "", "0x6a 0x86");
		}

		if (ALLOW_NEGATIVE_INPUT) {
			assertAPDUResponse("0x00 " + OxSUB + " 0xf5 0xf4 0x00", "0x01 0x00", "0x90 0x00"); // -0b - -0c =  01
			assertAPDUResponse("0x00 " + OxSUB + " 0xb0 0xa1 0x00", "0x0f 0x00", "0x90 0x00"); // -50 - -5f =  0f
			assertAPDUResponse("0x00 " + OxSUB + " 0x05 0xff 0x00", "0x06 0x00", "0x90 0x00"); //  05 - -01 =  06
		} else {
			assertAPDUResponse("0x00 " + OxSUB + " 0xf5 0xf4 0x00", "", "0x6a 0x86");
			assertAPDUResponse("0x00 " + OxSUB + " 0xb0 0xa1 0x00", "", "0x6a 0x86");
			assertAPDUResponse("0x00 " + OxSUB + " 0x05 0xff 0x00", "", "0x6a 0x86");
		}

		if (ALLOW_NEGATIVE_INPUT && ALLOW_NEGATIVE_OUTPUT) {
			assertAPDUResponse("0x00 " + OxSUB + " 0xb0 0xb1 0x00", "0xff 0xff", "0x90 0x00"); // -50 - -4f = -01 (0xff 0xff signed short)
			assertAPDUResponse("0x00 " + OxSUB + " 0xf5 0xf6 0x00", "0xff 0xff", "0x90 0x00"); // -0b - -0a = -01 (0xff 0xff signed short)
		} else {
			assertAPDUResponse("0x00 " + OxSUB + " 0xb0 0xb1 0x00", "", "0x6a 0x86");
			assertAPDUResponse("0x00 " + OxSUB + " 0xf5 0xf6 0x00", "", "0x6a 0x86");
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testMul1() {
		assertAPDUResponse("0x00 " + OxMUL + " 0x00 0x10 0x00", "0x00 0x00", "0x90 0x00"); //  00 *  10 =    00
		assertAPDUResponse("0x00 " + OxMUL + " 0x02 0x0a 0x00", "0x14 0x00", "0x90 0x00"); //  02 *  0a =    14
		assertAPDUResponse("0x00 " + OxMUL + " 0x13 0x02 0x00", "0x26 0x00", "0x90 0x00"); //  13 *  02 =    26
		assertAPDUResponse("0x00 " + OxMUL + " 0x70 0x03 0x00", "0x50 0x01", "0x90 0x00"); //  70 *  03 =   150
		assertAPDUResponse("0x00 " + OxMUL + " 0x70 0x30 0x00", "0x00 0x15", "0x90 0x00"); //  70 *  30 =  1500
		assertAPDUResponse("0x00 " + OxMUL + " 0x7f 0x7f 0x00", "0x01 0x3f", "0x90 0x00"); //  7f *  7f =  3f01

		if (ALLOW_NEGATIVE_INPUT && ALLOW_NEGATIVE_OUTPUT) {
			assertAPDUResponse("0x00 " + OxMUL + " 0x70 0xd0 0x00", "0x00 0xeb", "0x90 0x00"); //  70 * -30 = -1500 (0xeb 0x00 signed short)
			assertAPDUResponse("0x00 " + OxMUL + " 0x7f 0x80 0x00", "0x80 0xc0", "0x90 0x00"); //  7f * -80 = -3f80 (0xc0 0x80 signed short)
		} else {
			assertAPDUResponse("0x00 " + OxMUL + " 0x70 0xd0 0x00", "", "0x6a 0x86");
			assertAPDUResponse("0x00 " + OxMUL + " 0x7f 0x80 0x00", "", "0x6a 0x86");
		}

		if (ALLOW_NEGATIVE_INPUT) {
			assertAPDUResponse("0x00 " + OxMUL + " 0x80 0x80 0x00", "0x00 0x40", "0x90 0x00"); // -80 * -80 =  4000
		} else {
			assertAPDUResponse("0x00 " + OxMUL + " 0x80 0x80 0x00", "", "0x6a 0x86");
		}
	}

	@Test
	public void testAnd() {
		assertAPDUResponse("0x00 " + OxAND + " 0x00 0xff 0x00", "0x00 0x00", "0x90 0x00"); //  00 (0000) & -01 (ffff) =   00 (0000)
		assertAPDUResponse("0x00 " + OxAND + " 0xf1 0xa9 0x00", "0xa1 0xff", "0x90 0x00"); // -0f (fff1) & -57 (ffa9) =  -5f (ffa1) 
		assertAPDUResponse("0x00 " + OxAND + " 0x87 0x53 0x00", "0x03 0x00", "0x90 0x00"); // -79 (ff87) &  53 (0053) =   03 (0003)
	}

	@Test
	public void testOr() {
		assertAPDUResponse("0x00 " + OxOR + " 0x00 0xff 0x00", "0xff 0xff", "0x90 0x00"); //  00 (0000) | -01 (ffff) = -01 (ffff)
		assertAPDUResponse("0x00 " + OxOR + " 0x00 0x7f 0x00", "0x7f 0x00", "0x90 0x00"); //  00 (0000) |  7f (007f) =  7f (007f)
		assertAPDUResponse("0x00 " + OxOR + " 0xf1 0xa9 0x00", "0xf9 0xff", "0x90 0x00"); // -0f (fff1) | -57 (ffa9) = -07 (fff9)
		assertAPDUResponse("0x00 " + OxOR + " 0x87 0x53 0x00", "0xd7 0xff", "0x90 0x00"); // -79 (ff87) |  53 (0053) = -29 (ffd7)
		assertAPDUResponse("0x00 " + OxOR + " 0x77 0x53 0x00", "0x77 0x00", "0x90 0x00"); //  77 (0077) |  53 (0053) =  77 (0077)
	}

	@Test
	public void testNot() {
		assertAPDUResponse("0x00 " + OxNOT + " 0x00 0x00 0x00", "0xff 0xff", "0x90 0x00"); //  00 (0000) -> -01 (ffff)
		assertAPDUResponse("0x00 " + OxNOT + " 0xff 0x00 0x00", "0x00 0x00", "0x90 0x00"); // -01 (ffff) ->  00 (0000)
		assertAPDUResponse("0x00 " + OxNOT + " 0xf1 0x00 0x00", "0x0e 0x00", "0x90 0x00"); // -0f (fff1) ->  0e (000e)
		assertAPDUResponse("0x00 " + OxNOT + " 0x87 0x00 0x00", "0x78 0x00", "0x90 0x00"); // -79 (ff87) ->  78 (0078)
		assertAPDUResponse("0x00 " + OxNOT + " 0x02 0x00 0x00", "0xfd 0xff", "0x90 0x00"); //  02 (0002) -> -03 (fffd)
	}

	@Test
	public void testInvalidINS() {
		assertAPDUResponse("0x00 0x10 0x00 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0x13 0xff 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0x37 0xf1 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0xCA 0x87 0x00 0x00", "", "0x6d 0x00");
		assertAPDUResponse("0x00 0xFE 0x02 0x00 0x00", "", "0x6d 0x00");
	}

	private void assertAPDUResponse(String request, String responseData, String responseStatus) {
		assertAPDUResponse(b(request), b(responseData), b(responseStatus));
	}

	private void assertAPDUResponse(byte[] request, byte[] responseData, byte[] responseStatus) {
		ResponseAPDU response = simulator.transmitCommand(new CommandAPDU(request));

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
					throw new InputMismatchException("invalid 'x' character found");
			} else if (isHexDigit(c)) {
				if (firstDigit == null)
					firstDigit = c;
				else {
					bytes.add(getByte(firstDigit, c));
					firstDigit = null;
				}
			} else if (Character.isWhitespace(c)) { // nop
			} else
				throw new InputMismatchException("invalid character ('" + c + "') found");
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

	private static String Ox(byte b) {
		if (b < 0x10)
			return "0x0" + Integer.toHexString(b);
		else
			return "0x" + Integer.toHexString(b);
	}
}
