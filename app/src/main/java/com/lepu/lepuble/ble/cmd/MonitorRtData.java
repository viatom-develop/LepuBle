package com.lepu.lepuble.ble.cmd;

import java.util.ArrayList;

public class MonitorRtData {
    public int hr;
    public boolean isRMark; // R wave mark, use for play ECG sound.
    public int pr, spo2, pi;
    public boolean isPulseMark; // pulse detected, use for play pulse sound.
    public int battery;
    public ArrayList<Integer> ecgWave;
    public ArrayList<Integer> spo2Wave;

    public MonitorRtData(byte[] buf) {
        int index = 0;
        index += 2; // 0xA5,0x5A
        int pkg_size = buf[index] & 0xff;
        index++;
        index++; // ecg type
        for (int i = 0; i<5; i++) {
            ecgWave.add(buf[index]&0xff + ((buf[index+1]&0xff)<<8));
            index += 2;
        }
        hr = buf[index]&0xff + ((buf[index+1]&0xff)<<8);
        index += 2;
        index += 2; // qrs
        index += 2; // st
        index += 2; // pvcs
        isRMark = buf[index] != (byte) 0x00;
        index++; //note
        index ++; // spo2 type
        for (int i = 0; i<5; i++) {
            spo2Wave.add(buf[index]&0xff + ((buf[index+1]&0xff)<<8));
            index += 2;
        }
        pr = buf[index]&0xff + ((buf[index+1]&0xff)<<8);
        index += 2;
        spo2 = buf[index]&0xff;
        index++;
        pi = buf[index]&0xff;
        index++;
        isPulseMark = buf[index] != (byte) 0x00;
        index++;
        index++; // note
        index++; // 0XF1
        battery = buf[index]&0xff;
        index++; // crc

    }

}
