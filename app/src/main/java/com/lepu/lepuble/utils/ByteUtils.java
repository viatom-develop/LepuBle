package com.lepu.lepuble.utils;

public class ByteUtils {

    public static int byte2UInt(byte b) {
        return b & 0xff;
    }

    /*
     * @param 2 byte
     * @return
     */
    public static int bytes2UIntBig(byte b1, byte b2) {
        return (((b1 & 0xff) << 8) + (b2 & 0xff));
    }

    public static int bytes2UIntBig(byte b1, byte b2, byte b3, byte b4) {
        return (((b1 & 0xff) << 24) + ((b2 & 0xff) << 16) + ((b3 & 0xff) << 8) + (b4 & 0xff));
    }
}
