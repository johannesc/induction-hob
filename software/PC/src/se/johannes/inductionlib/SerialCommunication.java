package se.johannes.inductionlib;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Enumeration;

/**
 *
 * This class understands the serial communication for my induction cooker
 * TODO: Split the OS-specific things into a separate class and only talk
 * In-/Out-Streams here. Also remove debug prints or make them possible to turn
 * off.
 *
 */
public class SerialCommunication implements
        SerialPortEventListener {

    private final PowerCardCallback powerCardCallback;
    private final KeyBoardCallback keyBoardCallback;

    InputStream is;
    OutputStream os;

    public static CommPortIdentifier getPortId() {
        System.out.println("Starting...");
        @SuppressWarnings("unchecked")
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier
                .getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = portList.nextElement();
            System.out.println("name=" + portId.getName());
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                // On my windows machine my Bus pirate shows up at COM9
                // On my Linux it shows up as /dev/ttyUSB0 for now
                // TODO make this better.
                if ((portId.getName().equals("COM9"))
                        || (portId.getName().equals("/dev/ttyUSB0"))) {
                    return portId;
                }
            }
        }
        System.err.println("Could not found a serial port to talk with");
        return null;
    }

    public SerialCommunication(CommPortIdentifier portId,
            PowerCardCallback powerCardCallback,
            KeyBoardCallback keyBoardCallback) {
        this.powerCardCallback = powerCardCallback;
        this.keyBoardCallback = keyBoardCallback;
        // Using RXTX library, see http://rxtx.qbang.org/wiki/index.php/Download
        try {
            SerialPort serialPort = (SerialPort) portId.open("string", 2000);
            is = serialPort.getInputStream();
            os = serialPort.getOutputStream();
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            int speed = 115200;
            speed = 9600;
            serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new SerialCommunication(getPortId(), PowerCardCallback.empty,
                KeyBoardCallback.empty);
    }

    // This could have been a ring buffer.
    byte[] buffer = new byte[100];
    int bufferSize = 0;

    @Override
    public void serialEvent(SerialPortEvent serialEvent) {
        switch (serialEvent.getEventType()) {
        case SerialPortEvent.DATA_AVAILABLE:

            try {
                int numBytes = 0;
                if (is.available() > 0) {
                    numBytes = is.read(buffer, bufferSize, buffer.length
                            - bufferSize);
                }
                bufferSize += numBytes;
                decodeData();
            } catch (IOException e) {
                System.out.println(e);
            }
            break;
        case SerialPortEvent.FE:
            System.out.println("FE");
        case SerialPortEvent.PE:
            System.out.println("PE");
            break;
        default:
            System.out.println("Unknown event:" + serialEvent.getEventType());
            break;
        }
    }

    public static final int ZONE_LEFT_FRONT = 0;
    public static final int ZONE_LEFT_BACK = 1;
    public static final int ZONE_RIGHT_BACK = 2;
    public static final int ZONE_RIGHT_FRONT = 3;

    private final byte[] powerLevels2Byte = { POWER_LEVEL_0, POWER_LEVEL_U,
            POWER_LEVEL_1, POWER_LEVEL_2, POWER_LEVEL_3, POWER_LEVEL_4,
            POWER_LEVEL_5, POWER_LEVEL_6, POWER_LEVEL_7, POWER_LEVEL_8,
            POWER_LEVEL_9, POWER_LEVEL_P };

    private int getPowerLevelFromByte(byte data) {
        for (int i = 0; i < powerLevels2Byte.length; i++) {
            if (powerLevels2Byte[i] == data) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Turn the power card on or off, sent by keyboard
     *
     * @param on
     */
    public void setMainPower(boolean on) {
        // POWER_ON_COMMAND
        // C9 44 2C 03 C0 00 63 01
        byte[] packetData = { (byte) 0xC0, 0x00, 0x00 };
        if (on) {
            packetData[2] = 0x63;
        } else {
            packetData[2] = 0x64;
        }
        sendPacket(POWER_ON_COMMAND, packetData);
    }

    /**
     * Send command to power card in order to control power level on all zones.
     *
     * @param powerLevels
     *            What level 0 - 11 of each zone, see ZONE_*
     */
    public void setPowerLevel(int[] powerLevels) {
        // POWER_ON_COMMAND 50 00 01 01 01 01 00
        byte[] packetData = { 0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        for (int i = 0; i < 4; i++) {
            packetData[i + 2] = powerLevels2Byte[powerLevels[i]];
        }
        sendPacket(POWER_ON_COMMAND, packetData);
    }

    private void sendPacket(short command, byte[] packetData) {
        byte[] packet = new byte[packetData.length + 5];
        packet[0] = (byte) 0xC9;
        packet[1] = (byte) (command >>> 8);
        packet[2] = (byte) (command & 0xFF);
        packet[3] = (byte) packetData.length;
        System.arraycopy(packetData, 0, packet, 4, packetData.length);
        packet[packet.length - 1] = calculateCheckSum(packet, packet.length);
        try {
            os.write(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendAckPacket(byte checksum) {
//        if (true) {
//            System.out.println("Ack disabled");
//            return;
//        }
        System.out.println("Sending ack packet:"
                + String.format("%02X ", checksum));
        byte[] packet = {PACKET_TYPE_ACK, checksum ,0};
        packet[2] = calculateCheckSum(packet, 3);
        try {
            os.write(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Data protocol is a inverted UART with open collector where both Rx and Tx
     * share the same line. Communication settings is 9600, even parity, 1 stop
     * bit.
     *
     * The data is sent as packets of two types: PACKET_TYPE_COMMAND and
     * PACKET_TYPE_ACK. The format of the packets are described below.
     *
     * NOTE: The PACKET_TYPE_* Is not a unique byte in the stream, the checksum
     * might very well be one of these values (have been seen in the pot on
     * command).
     *
     * Both the power card and the keyboard can initiate communication. I guess
     * collisions are handled with the checksum ping-pong protocol.
     */

    /**
     * PACKET_TYPE_ACK
     *
     * This packet is sent as a response to PACKET_TYPE_COMMAND the format is:
     * 98 xx yy Where
     * xx is the checksum of the PACKET_TYPE_COMMAND (CC below)
     * yy is the checksum of this PACKET_TYPE_ACK
     */
    static final byte PACKET_TYPE_ACK = (byte) 0x98;

    /**
     * PACKET_TYPE_COMMAND
     *
     * This packet has the following format: C9 XX YY LL nn nn nn nn CC Where:
     * XX YY is the command, if XX is zero it seems like there are no ack sent
     *       XX could also be seen as a "from" address and YY the "to" address
     *       where 2C would be the power card and 44 and 47 the keyboard
     * nn is the pay load, see code below
     * LL is the remaining length of the packet not counting the checksum byte
     * CC is the checksum byte if this packet.
     */
    static final byte PACKET_TYPE_COMMAND = (byte) 0xC9;

    static final short POWER_ON_COMMAND = 0x442C; // From keyboard
                                                  // Seems like this is resent
                                                  // every ~10s. If not, the
                                                  // power card powers off

    static final short POWERED_ON_COMMAND = 0x2C44; // From power card

    static final short POWERED_ON_COMMAND_NO_ACK = 0x0044; // From power card,
                                                           // no ack is sent

    static final short POT_PRESENT_STATUS_NO_ACK = 0x0047; // From power card,
                                                           // no ack is sent

    static final short POT_PRESENT_STATUS = 0x2C47; // From power card
                                                    // when removing pot

    // Unknown packets. Sent after power cycle:
    // C9 24 2C 02 17 02 D6
    //
    // C9 2C 44 09 17 03 01 84 00 F4 5A EF 88 F0
    // C9 44 2C 03 73 02 00 D3
    // C9 2C 44 03 73 03 00 D2
    // C9 44 2C 02 15 02 B4

    static final byte LB_DETECT_MASK = 0x04;
    static final byte RB_DETECT_MASK = 0x10;
    static final byte RF_DETECT_MASK = 0x40;
    static final byte LF_DETECT_MASK = 0X01;

    static final byte ZONE_HOT_MASK = 0x40;
    static final byte POWER_ACTIVE_MASK = 0x01;

    static final byte POWER_LEVEL_0 = 0x00;
    static final byte POWER_LEVEL_U = 0x01;
    static final byte POWER_LEVEL_1 = 0x02;
    static final byte POWER_LEVEL_2 = 0x08;
    static final byte POWER_LEVEL_3 = 0x0C;
    static final byte POWER_LEVEL_4 = 0x0F;
    static final byte POWER_LEVEL_5 = 0x11;
    static final byte POWER_LEVEL_6 = 0x14;
    static final byte POWER_LEVEL_7 = 0x15;
    static final byte POWER_LEVEL_8 = 0x16;
    static final byte POWER_LEVEL_9 = 0x17;
    static final byte POWER_LEVEL_P = 0x18;

    public static final byte POWERSTATUS_OFF = 0x00;
    public static final byte POWERSTATUS_ON_IDLE = 0x01;
    public static final byte POWERSTATUS_ON_ACTIVE = 0x03;

    String commandString = "";
    String paramString = "";

    private void decodeData() {
        // Start patterns:
        // 0x9C packet with length at index 3 (minus checksum)
        // 0x98 packet with length = 3
        //
        // Note, this code is not correct since the "start pattern" can
        // exist elsewhere in the data stream. A more robust packet detection
        // would be to use some timing when the bytes are arrived (i.e. "old"
        // bytes are dropped). This seems to be used at least by the power card
        // which seems to drop packets when they are sent one byte a time with
        // some delay between.

        boolean packetFound = false;
        for (int i = 0; i < bufferSize; i++) {
            if ((buffer[i] == PACKET_TYPE_ACK)
                    || (buffer[i] == PACKET_TYPE_COMMAND)) {
                packetFound = true;
                if (i != 0) {
                    System.out.println("Hmm, packet not at start?");
                    // remove crap data, packet should start at index 0
                    System.arraycopy(buffer, i, buffer, 0, bufferSize - i);
                    bufferSize -= i;
                }
                break;
            }
        }
        if (!packetFound) {
            System.out.println("No packet found, clearing buffer");
            bufferSize = 0;
            return;
        }

        int packetLen = 0;
        if (buffer[0] == PACKET_TYPE_COMMAND) {
            // System.out.println("Command package");
            if (bufferSize < 6) {
                // System.out.println("Need more data");
                return;
            }
            packetLen = buffer[3] + 5;
            if (bufferSize < packetLen) {
                // System.out.println("Incomplete expected=" + packetLen +
                // " got " + bufferSize);
                return;
            }

            byte checksum = calculateCheckSum(buffer, packetLen);
            if (buffer[packetLen - 1] != checksum) {
                System.err.println("Wrong packet checksum!");
            } else {

                short command = (short) ((buffer[1] << 8) | (buffer[2] & 0xFF));
                commandString = String.format("%04X", command);
                paramString = "";

                boolean expectAck = buffer[1] == 0 ? false : true;

                switch (command) {
                case POWER_ON_COMMAND:
                    decodePowerOnCommand(expectAck, checksum);
                    break;
                case POWERED_ON_COMMAND_NO_ACK:
                case POWERED_ON_COMMAND:
                    decodePoweredOnCommand(expectAck, checksum);
                    break;
                case POT_PRESENT_STATUS_NO_ACK:
                case POT_PRESENT_STATUS:
                    decodePotPresentStatus(expectAck, checksum);
                    break;
               default:
                    commandString += " ------------ - -- - UNKNOWN";
                    break;
                }
                System.out
                        .println(getTs() + getHexString(buffer, 0, packetLen));
                System.err.println(getTs() + "COMMAND cmd=" + commandString
                        + paramString);
                System.err.println();
            }
        } else if (buffer[0] == PACKET_TYPE_ACK) {
            packetLen = 3; // including checksum
            if (bufferSize < packetLen) {
                // System.out.println("Incomplete expected=" + packetLen +
                // " got " + bufferSize);
                return;
            }

            byte checksum = calculateCheckSum(buffer, packetLen);
            if (buffer[packetLen - 1] != checksum) {
                System.err.println("Wrong packet checksum!");
            }

            String checksumString = String.format("%02X", buffer[1]);
            System.out.println(getTs() + getHexString(buffer, 0, packetLen));
            System.err.println(getTs() + "ACK of package with checksum = "
                    + checksumString);
            System.err.println();
        } else {
            System.err.println(new Timestamp(System.currentTimeMillis())
                    .toString()
                    + getHexString(buffer, 0, packetLen)
                    + "Unknown package: " + buffer[0] + " sz=" + bufferSize);
            bufferSize = 0;
            // Hmm, should never come here..
            return;
        }

        // Throw away packet from buffer.
        System.arraycopy(buffer, packetLen, buffer, 0, bufferSize - packetLen);
        bufferSize -= packetLen;
        if (bufferSize != 0) {
            decodeData(); // Decode next packet
        }
    }

    private void decodePotPresentStatus(boolean expectAck, byte checksum) {
        commandString += "<-(POT_PRESENT_STATUS)";
        // C9 2C 47 04 54 03 55 3C 98
        // 0  1  2  3  4  5  6  7  8
        // Check so this really is a command that we know about
        if ((buffer[4] == (byte) 0x54) && (buffer[5] == (byte) 0x03)
                && (buffer[7] == (byte) 0x3C)) {

            boolean[] present = new boolean[4];
            paramString = "ST: ";
            byte presentMask = buffer[6];
            if ((presentMask & LF_DETECT_MASK) != 0) {
                paramString += "LF ";
                present[0] = true;
            }
            if ((presentMask & LB_DETECT_MASK) != 0) {
                paramString += "LB ";
                present[1] = true;
            }
            if ((presentMask & RB_DETECT_MASK) != 0) {
                paramString += "RB ";
                present[2] = true;
            }
            if ((presentMask & RF_DETECT_MASK) != 0) {
                paramString += "RF ";
                present[3] = true;
            }
            powerCardCallback.onPotPresent(present, expectAck, checksum);
        } else {
            paramString = "Unknown POT_PRESENT_STATUS params!";
        }
    }

    private void decodePoweredOnCommand(boolean expectAck, byte checksum) {
        // Also seen, don't know what they mean:
        // C9 2C 44 03 73 03 00 D2
        // C9 2C 44 03 73 03 02 D0

        // power limit, no ack is sent
        // C9 00 44 07 18 03 00 00 11 18 FF 67
        // C9 00 44 07 18 03 11 18 11 18 FF 6E
        //
        // power limit:
        // C9 2C 44 07 18 03 00 00 11 18 FF 4B
        // | <- limit command?
        // |                 lf
        // |                    lb
        // |                       rb ==> limited to level 5!
        // |                          rf
        // LF Hot, all off:
        // C9 2C 44 08 15 03 00 40 00 00 00 04 FB

        // Back on U LF hot:
        // C9 2C 44 08 15 03 03 40 01 01 00 04 F8
        //
        //
        // C9 2C 44 08 15 03 03 00 00 01 00 04 B9
        // Idle power plate indication?
        // C9 2C 44 08 15 03 03 01 01 01 01 04 B8
        //                      | left front
        //                         | left back
        //                            | right back
        //                               | right front
        //
        // C9 2C 44 08 15 03 01 00 00 00 00 04 BA Off
        // C9 2C 44 08 15 03 00 00 00 00 00 04 BB On
        // 0 1 2 3 4 5 6
        // |-- 0x00 -> Off, 0x01 -> On
        commandString += "<-(POWRD)";
        if (buffer[4] == (byte) 0x15) {
            byte powerStatus = buffer[6];

            if (powerStatus == POWERSTATUS_OFF) {
                paramString = " OFF";
            } else if (powerStatus == POWERSTATUS_ON_IDLE) {
                paramString = " ON (idle)";
            } else if (powerStatus == POWERSTATUS_ON_ACTIVE) {
                paramString = " ON (active)";
            } else {
                paramString = " -------------------UNKNOWN powerStatus: "
                        + powerStatus;
                powerCardCallback.onUnknownData();
            }
            boolean[] powered = new boolean[4];
            boolean[] hot = new boolean[4];
            String[] zoneNames = {"LF", "LB", "RB", "RF"};
            for (int i = 0; i < powered.length; i++) {
                powered[i] = (buffer[i + 7] & POWER_ACTIVE_MASK) != 0;
                hot[i] = ((buffer[i + 7] & ZONE_HOT_MASK) != 0);
                paramString += " " + zoneNames[i] + ":"
                        + (powered[i] ? " On" : " Off")
                        + (hot[i] ? " Hot" : " Cold");
            }

            powerCardCallback.onPoweredOnCommand(powerStatus, powered,
                    hot, expectAck, checksum);
        } else if (buffer[4] == (byte) 0x18) {
            paramString = " (PLIMIT)"; // Power limited
            paramString += " LF LB RB RF:" + getHexString(buffer, 6, 4);

            int[] levels = new int[4];
            for (int i = 0; i < levels.length; i++) {
                levels[i] = getPowerLevelFromByte(buffer[i + 6]);
            }
            powerCardCallback.onPowerLimitCommand(levels, expectAck, checksum);
        } else if (buffer[4] == (byte) 0x73) {
            // Example of packages:
            // C9 2C 44 03 73 03 00 D2
            // C9 2C 44 03 73 03 02 D0
            System.out.println("------------------------------" +
                        "Unknown packet that has been seen before");
            if (expectAck) {
                // Lets send an ack...
                sendAckPacket(checksum);
            }
        } else {
            paramString = " -------------------UNKNOWN!:" +
                    String.format("%02X", buffer[4]);
            powerCardCallback.onUnknownData();
        }
    }

    private void decodePowerOnCommand(boolean expectAck, byte checksum) {
        commandString += "->(PWR)";
        if (buffer[4] == (byte) 0xC0) {
            // C9 44 2C 03 C0 00 63 01
            paramString += " MPWR";
            if (buffer[6] == 0x64) {
                paramString += " OFF";
                keyBoardCallback.onSetMainPowerCommand(false, expectAck,
                        checksum);
            } else if (buffer[6] == (byte) 0x63) {
                paramString += " ON";
                keyBoardCallback.onSetMainPowerCommand(true, expectAck,
                        checksum);
            } else {
                paramString += " --------------------UNKNOWN!!!-------";
                keyBoardCallback.onUnknownData();
            }
        } else if (buffer[4] == (byte) 0x50) {
            // zone power
            // 50 00 01 01 01 01 00
            //       | left front
            //          | left inner
            //             | right inner
            //                | right front
            paramString = " FL BL BR FR:" + getHexString(buffer, 6, 4);
            int[] powerLevels = new int[3];
            for (int i = 0; i < powerLevels.length; i++) {
                powerLevels[i] = getPowerLevelFromByte(buffer[i + 6]);
            }
            keyBoardCallback.onPowerOnCommand(powerLevels, expectAck, checksum);
        } else {
            paramString = "          --- UNKNOWN ---";
            keyBoardCallback.onUnknownData();
        }
    }

    private byte calculateCheckSum(byte[] packet, int packetLenght) {
        byte checkSum = 0;
        for (int i = 0; i < packetLenght - 1; i++) {
            checkSum = (byte) (checkSum ^ packet[i]);
        }
        return checkSum;
    }

    private String getTs() {
        return new Timestamp(System.currentTimeMillis()).toString() + " ";
    }

    private String getHexString(byte[] array, int off, int len) {
        StringBuffer data = new StringBuffer();
        for (int i = off; i < (len + off); i++) {
            data.append(String.format("%02X ", array[i] & 0xFF));
        }
        return data.toString();
    }
}
