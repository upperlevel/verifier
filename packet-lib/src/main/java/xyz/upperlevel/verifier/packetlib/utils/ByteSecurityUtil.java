package xyz.upperlevel.verifier.packetlib.utils;

import java.nio.ByteBuffer;

public class ByteSecurityUtil {

    public static void zero(char[] arr, int offset, int to) {
        for(int i = offset; i < to; i++)
            arr[i] = 0;
    }

    public static void  zero(char[] arr) {
        zero(arr, 0, arr.length);
    }

    public static void zero(byte[] arr, int offset, int to) {
        for(int i = offset; i < to; i++)
            arr[i] = 0;
    }

    public static void zero(byte[] arr) {
        zero(arr, 0, arr.length);
    }

    public static void zero(ByteBuffer in) {
        if(in.hasArray())
            zero(in.array());
        else {
            in.clear();
            while (in.remaining() > 0)
                in.put((byte) 0);
        }
    }
}
