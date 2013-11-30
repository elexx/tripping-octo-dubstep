package tuwien.inso.mnsa.ue2;

import javacard.framework.*;

public class Ue2Applet extends Applet {

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
		case (byte) 0x00: // NOP
			return;

		case (byte) 0x01: // ADD
			result = (short) ((p1 + p2) & 0xffff);
			break;

		case (byte) 0x02: // SUB
			result = (short) ((p1 - p2) & 0xffff);
			break;

		case (byte) 0x03: // MUL
			result = (short) ((p1 * p2) & 0xffff);
			break;

		// case (byte)0x04: // DIV

		case (byte) 0x05: // AND
			result = (short) ((p1 & p2) & 0xffff);
			break;

		case (byte) 0x06: // OR
			result = (short) ((p1 | p2) & 0xffff);
			break;

		case (byte) 0x07: // NOT
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
