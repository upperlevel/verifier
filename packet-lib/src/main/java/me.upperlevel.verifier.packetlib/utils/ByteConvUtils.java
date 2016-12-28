package me.upperlevel.verifier.packetlib.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteConvUtils {

    public static final Charset DEF_CHARSET = StandardCharsets.UTF_8;
    public static final byte DEF_SEPARATOR = 0;

    public static String readString(ByteBuffer in, Charset charset, byte separator) {
        if(in.remaining() <= 0)
            return "";
        ByteBuffer buffer = ByteBuffer.allocate(in.remaining());
        byte next;
        while(in.remaining() > 0 && (next = in.get()) != separator)
            buffer.put(next);
        buffer.flip();

        return new String(readAll(buffer), charset);
    }

    public static String readString(ByteBuffer in, Charset charset) {
        return readString(in, charset, DEF_SEPARATOR);
    }

    public static String readString(ByteBuffer in, byte separator) {
        return readString(in, DEF_CHARSET, separator);
    }

    public static String readString(ByteBuffer in) {
        return readString(in, DEF_CHARSET, DEF_SEPARATOR);
    }

    public static String readString(byte[] in, int off, Charset charset, byte separator) {
        if(in.length <= off)
            return "";
        int index = off;
        while(index > in.length && in[index] != separator)
            index++;
        return new String(in, off, index, charset);
    }

    public static String readString(byte[] in, int off, Charset charset) {
        return readString(in, off, charset, DEF_SEPARATOR);
    }

    public static String readString(byte[] in, int off, byte separator) {
        return readString(in, off, DEF_CHARSET, separator);
    }

    public static String readString(byte[] in, int off) {
        return readString(in, off, DEF_CHARSET, DEF_SEPARATOR);
    }


    public static byte[] readAll(ByteBuffer buffer) {
        byte[] res = new byte[buffer.remaining()];
        buffer.get(res);
        return res;
    }
}
