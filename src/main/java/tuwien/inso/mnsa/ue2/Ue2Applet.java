package tuwien.inso.mnsa.ue2;

import javacard.framework.*;

public class Ue2Applet extends Applet {

	public static final byte NOP = (byte) 0x00;
	public static final byte ADD = (byte) 0x02;
	public static final byte SUB = (byte) 0x04;
	public static final byte MUL = (byte) 0x06;
	public static final byte DIV = (byte) 0x08;
	public static final byte AND = (byte) 0x0A;
	public static final byte OR = (byte) 0x0C;
	public static final byte NOT = (byte) 0x0E;

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new Ue2Applet();
	}

	protected Ue2Applet() {
		register();
	}

	public void process(APDU apdu) {
		// return 90 00 on SELECT

		if (selectingApplet()) {
			return;
		}

		byte[] buf = apdu.getBuffer();
		byte ins = buf[ISO7816.OFFSET_INS];
		short p1 = buf[ISO7816.OFFSET_P1];
		short p2 = buf[ISO7816.OFFSET_P2];

		short result = 0;

		switch (ins) {
		case NOP:
			return;

		case ADD:
			result = (short) ((p1 + p2) & 0xffff);
			break;

		case SUB:
			result = (short) ((p1 - p2) & 0xffff);
			break;

		case MUL:
			result = (short) ((p1 * p2) & 0xffff);
			break;

		// case DIV:

		case AND:
			result = (short) ((p1 & p2) & 0xffff);
			break;

		case OR:
			result = (short) ((p1 | p2) & 0xffff);
			break;

		case NOT:
			result = (short) ((~p1) & 0xffff);
			break;

		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			return;
		}

		buf[0] = (byte) (result & 0xff);
		buf[1] = (byte) ((result >>> 8) & 0xff);
		apdu.setOutgoingAndSend((short) 0, (short) 2);
	}
}
