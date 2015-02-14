/*
 Dr J.J.Trinder 2015
 jont@ninelocks.com

This was built to assist testing an mbed program that is used to communicate with a megasquirt.

The code is not pretty, or mega efficient. Its quick and dirty :-)

At the moment it just responds to the A and S commands.

You can add more as you need them. CSome of the commands take arguments so will require more sophisticated handling
than I am currently doing.

For the moment on receiving an A it will send out an arruy of N characters
you can tweak how many bytes are sent and change the values at the locations you want. See the code in
send_ecu_data() below

 code based on the example serial handling code from 
 https://code.google.com/p/java-simple-serial-connector/wiki/jSSC_examples
 this version is rather clunky but this is scaffolding test code
 so Ive not been overley tidy 



 */
package com.ninelocks.desktop.command_line.gu_squirt_serial;

/**
 *
 * @author jont
 */
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class Squirt_serial {

    static SerialPort serialPort;

    public static void main(String[] args) {
        //let them pass the port in as a command line argument

        String portname = "/dev/ttyACM0";
        if (args.length > 0) {
            portname = args[0];

        } else {

            System.out.println("No portname supplied so assuming " + portname);
        }

        //now ope th port and do dat stuff
        serialPort = new SerialPort(portname);
        try {
            serialPort.openPort();//Open port
            serialPort.setParams(9600, 8, 1, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    /*
     * In this class must implement the method serialEvent, through it we learn about 
     * events that happened to our port. But we will not report on all events but only 
     * those that we put in the mask. In this case the arrival of the data 
     */
    static class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent spe) {
            if (spe.isRXCHAR() || spe.isRXFLAG()) {
                if (spe.getEventValue() > 0) {
                    try {

                        byte[] buffer = serialPort.readBytes(spe.getEventValue());
                        for (int i = 0; i < buffer.length; i++) {
                            //dump out the character we we received 
                           // System.out.println(Integer.toHexString((int) buffer[i]) + " " + (char) buffer[i]);
                            //now react to it. we only at the moment care about seeing an A so
                            switch (buffer[i]) {
                                case 'A':
                                    System.out.println("Received an A");
                                    send_ecu_data();
                                    break;
                                case 'S':
                                    System.out.println("Received an S");
                                    send_identifier_string();
                                    break;    
                                default:
                                    break;
                            }

                        }

                    } catch (SerialPortException ex) {
                        System.out.println(ex);
                    }

                }
            }

        }

        /*
        Send identifier string. NO idea if real ms sends an cr/lf at the end
        
        */
        public void send_identifier_string(){
            try {
                    serialPort.writeString("gu_squirt_serial");
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
                       
        }
        /*
         send the ecu data out, up serial port and we could show it here in nsome nice form    as well
         the var ecu_data_length determines how much of the ecu data is sent out
         so you can experiment with what happens at the mbed end if the data is not right.
         */
        public void send_ecu_data() {
            byte[] ecu_data = new byte[250];           // declare in array variable
            int ecu_data_length = 100;
            //init our ecu test values
            ecu_data[22] = 0x34;
            ecu_data[23] = 0x12;

            for (int i = 0; i < ecu_data_length; i++) {
                                //I like hex as 2 digits
                 String val_hex    =  String.format("%2s",Integer.toHexString((int) ecu_data[i] &0xff)).replace(' ', '0');
                
                System.out.println("location " + i + " was hex " + val_hex + " As ASCII is" + (char) ecu_data[i]);
                try {
                    serialPort.writeByte(ecu_data[i]);
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }

        }
    }
}
