package com.evergrande.ttyusb.ttyusb;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.lang.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.*;
import android.serialport.SerialPort;

/**
 * @author by Weiqifa on 2019/2/27.
 *
 * 通过串口用于接收或发送数据
 */

public class SerialPortUtil {

    private static final String TAG = "ttyusb";
    private static final String TTYUSB0 = "/dev/ttyUSB0";
    private static final String TTYUSB1 = "/dev/ttyUSB1";
    private static final String THVD0 = "/proc/thvd1500/thvd0";
    private static final String THVD1 = "/proc/thvd1500/thvd1";
    private volatile String CmdUsb="";

    private SerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private SerialPort serialPort1 = null;
    private InputStream inputStream1 = null;
    private OutputStream outputStream1 = null;

    private ReceiveThread mReceiveThread = null;
    private SendThread mSendThread = null;

    public volatile  boolean isStart = false;
    public volatile  boolean isRunning = false;
    public volatile  boolean isRecive = false;


    private int successCount = 0;
    //Lock lock = new ReentrantLock();


    /**
     * 设置设备节点值
     */
    private void setProcStringValue(String path, String value) {
       try {
            BufferedWriter bufWriter = null;
            bufWriter = new BufferedWriter(new FileWriter(path));
            bufWriter.write(value + "");
            bufWriter.close();
            Log.d(TAG, "value:" + value);
        } catch (IOException e) {
            Log.e(TAG,"erro= "+ Log.getStackTraceString(e));
        }
    }
    /**
     * 读取设备节点的值，1 true 0 false
     */
    private boolean getProcStringValue(String path){
        try {
                FileReader fr = new FileReader(path);
                BufferedReader br = new BufferedReader(fr);
                String readString = null;
                String valueString = null;
                while ((readString = br.readLine())!=null){
                    if(readString == null)break;
                    valueString = readString;
                 }
                br.close();
                return valueString != null && valueString.equals("1");
        } catch (IOException e) {
                Log.e(TAG,"erro= "+ Log.getStackTraceString(e));
        }
        return false;
    }
    /**
     * 设置485电源 使能为rx tx
     * 0 表示设置为 rx 1表示设置为tx
     */
     public  void setTtyusb0InputTtyusb1Ouput(String thvd0,String thvd1){
         Log.d(TAG, "thvd0:"+thvd0+" thvd1:"+thvd1);
         setProcStringValue(THVD0,thvd0);
         setProcStringValue(THVD1,thvd1);
     }

    /**
     * 打开串口，接收数据
     * 通过串口，接收单片机发送来的数据
     */
    public void openSerialPort() {
        try {
            serialPort = new SerialPort(new File(TTYUSB0), 115200, 0);
            //调用对象SerialPort方法，获取串口中"读和写"的数据流
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            serialPort1 = new SerialPort(new File(TTYUSB1), 115200, 0);
            //调用对象SerialPort方法，获取串口中"读和写"的数据流
            inputStream1 = serialPort1.getInputStream();
            outputStream1 = serialPort1.getOutputStream();

            Log.d(TAG, "打开串口");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //getSerialPort();
    }

    public void closeThread(){
        Log.i(TAG, "关闭线程");
        try {
        if(mReceiveThread != null)
            mReceiveThread.join();
        if(mSendThread != null)
            mSendThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 关闭串口
     * 关闭串口中的输入输出流
     */
    public void closeSerialPort() {
        Log.i(TAG, "关闭串口");
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream1 != null) {
                inputStream1.close();
            }
            if (outputStream1 != null) {
                outputStream1.close();
            }

            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送数据
     * @param data 要发送的数据
     */
    public void sendSerialPort(String ttyusbNumber,String data) {
        if(ttyusbNumber.equals(TTYUSB0)){
            try {
                byte[] sendData = DataUtils.HexToByteArr(data);
                outputStream.write(sendData);
                outputStream.flush();
                //Log.d(TAG, "发送数据[0]:"+data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(ttyusbNumber.equals(TTYUSB1)){
            try {
                byte[] sendData = DataUtils.HexToByteArr(data);
                outputStream1.write(sendData);
                outputStream1.flush();
                //Log.d(TAG, "发送数据[1]:"+data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getSerialPort() {
        if (mReceiveThread == null){
            mReceiveThread = new ReceiveThread();
        }
        isStart = true;
        mReceiveThread.start();

        if (mSendThread == null){
            mSendThread = new SendThread();
        }
        if(isRunning == false) {
            Log.d(TAG, "启动发送数据线程");
            mSendThread.start();
        }
        Log.d(TAG, "启动线程完成");
    }

    private void sendTreadSleep(int millis)
    {
        try{
            mSendThread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reciveTreadSleep(int millis)
    {
        try{
            mReceiveThread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SendThread extends Thread{

        int loopcount=0;
        public void run() {
            super.run();
            isRunning = true;
            successCount =0;
            Log.d(TAG,"开始测试发送数据");

            isRecive = false;
            setTtyusb0InputTtyusb1Ouput("0","1");
            CmdUsb = TTYUSB1;
            Log.d(TAG,CmdUsb);
            loopcount=0;
            do{
                sendSerialPort(TTYUSB1, Cmd.TEST_CMD_VALUE);
                sendTreadSleep(50);
                loopcount++;
                if(loopcount>=20){
                    loopcount=0;
                    break;
                }
            }while (isRecive == false);

            CmdUsb = TTYUSB0;
            isRecive = false;

            Log.d(TAG,CmdUsb);
            setTtyusb0InputTtyusb1Ouput("1","0");
            sendTreadSleep(10);

            do{
                sendSerialPort(TTYUSB0,Cmd.TEST_CMD_VALUE);
                sendTreadSleep(50);
                loopcount++;
                if(loopcount>=20){
                    loopcount=0;
                    break;
                }
            }while (isRecive == false);


            /*结果判断*/
            if((successCount/2 == Cmd.TEST_COUNT)||(isRecive == true) ){
                EventBus.getDefault().post(Cmd.TEXT_CMD_SUCCESS);
            }else{
                EventBus.getDefault().post(Cmd.TEXT_CMD_FAILL);
            }
            EventBus.getDefault().post(Cmd.TEST_CMD_FINISH);

            /*把运行标志位清除*/
            isRunning = false;
        }
    }

    /**
     * 接收串口数据的线程
     */
    private class ReceiveThread extends Thread {
        @Override
        public void run() {

            super.run();
            //条件判断，只要条件为true，则一直执行这个线程
            while (isStart == true)
            {
                if (inputStream == null || inputStream1 == null) {
                    Log.d(TAG,"未初始化inputStream");
                    return;
                }

                byte[] readData = new byte[1024];
                String readString = "none";
                int size =0;
                Log.d(TAG,">>>>>>>>>>>>>"+CmdUsb);


                try{
                    size = inputStream.read(readData);
                }catch (IOException e){
                    e.printStackTrace();
                }
                if (size > 0) {
                    readString = DataUtils.ByteArrToHex(readData, 0, size);
                    EventBus.getDefault().post("[0]" + readString);
                    Log.d(TAG, "接收数据[0]:" + readString);
                    isRecive = true;
                }


                try{
                    size = inputStream1.read(readData);
                }catch (IOException e){
                    e.printStackTrace();
                }
                if (size > 0) {
                    readString = DataUtils.ByteArrToHex(readData, 0, size);
                    EventBus.getDefault().post("[1]" + readString);
                    Log.d(TAG, "接收数据[1]:" + readString);
                    isRecive = true;
                }


                if(readString.equals(Cmd.TEST_CMD_VALUE)){
                    successCount++;
                    Log.d(TAG,"successCount:"+ successCount);
                }

            }

        }
    }

}
