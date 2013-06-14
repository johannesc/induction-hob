package pctestapp;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class SerialPortHelper implements SerialPortEventListener {

    private InputStream is;
    private OutputStream os;

    /**
     * @param args
     */
    public static void main(String[] args) {
        new SerialPortHelper();
    }

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

    public SerialPortHelper() {
        CommPortIdentifier portId = getPortId();

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

    public InputStream getInputStream() {
        return is;
    }

    public OutputStream getOutputStream() {
        return os;
    }

    @Override
    public void serialEvent(SerialPortEvent serialEvent) {
        switch (serialEvent.getEventType()) {
        case SerialPortEvent.DATA_AVAILABLE:
            System.out.println("DATA_AVAILABLE");
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
}
