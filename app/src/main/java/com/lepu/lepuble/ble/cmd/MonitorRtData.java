package com.lepu.lepuble.ble.cmd;

import java.util.ArrayList;

public class MonitorRtData {
    public int hr;
    public boolean isRMark; // R wave mark, use for play ECG sound.
    public int pr, spo2, pi;
    public boolean isPulseMark; // pulse detected, use for play pulse sound.
    public int battery;
    /**
     * ecgWave: ECG original sample data
     */
    public ArrayList<Integer> ecgWave = new ArrayList<Integer>();
    /**
     * ecgFloats: ECG voltage mV values
     */
    public ArrayList<Float> ecgFloats = new ArrayList<Float>();
    public ArrayList<Integer> spo2Wave = new ArrayList<Integer>();

    public MonitorRtData(byte[] buf) {
        int index = 0;
        index += 2; // 0xA5,0x5A
        int pkg_size = buf[index] & 0xff;
        index++;
        index++; // ecg type
        for (int i = 0; i<5; i++) {
            int sample = buf[index]&0xff + ((buf[index+1]&0xff)<<8);
            ecgWave.add(sample);
            ecgFloats.add(sample * 4033/32767/12/8.0f);
            index += 2;
        }
        hr = buf[index]&0xff + ((buf[index+1]&0xff)<<8);
        index += 2;
        index += 2; // qrs
        index += 2; // st
        index += 2; // pvcs
        isRMark = buf[index] != (byte) 0x00;
        index++;
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
