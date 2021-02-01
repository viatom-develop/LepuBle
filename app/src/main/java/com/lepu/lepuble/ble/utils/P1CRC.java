package com.lepu.lepuble.ble.utils;

public class P1CRC {

    public static byte CalCrc(byte[] buf) {
        if (buf == null || buf.length == 0) {
            return 0;
        }

        byte crc = 0;

        for (byte b : buf) {
            crc += (b & 0xff);
        }

        return crc;
    }
}
