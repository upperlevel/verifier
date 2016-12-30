package xyz.upperlevel.verifier.packetlib.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.List;

public class ByteConvUtils {

    public static final Charset DEF_CHARSET = StandardCharsets.UTF_8;
    public static final byte DEF_SEPARATOR = 0;

    public static String readString(ByteBuffer in, Charset charset, byte separator) {
        if(in.remaining() == 0)
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
        while(index < in.length && in[index] != separator)
            index++;
        return new String(in, off, index - off, charset);
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

    public static String[] readStringArray(ByteBuffer in, Charset charset, byte separator) throws IllegalFormatException {
        String[] strs = new String[in.get() & 0xFF];

        int index = 0;
        do {
            if(index >= strs.length)
                break;
            strs[index++] = readString(in, charset, separator);
        } while (in.remaining() > 0);
        if(index < strs.length)
            throw new IllegalArgumentException("ByteBuffer terminated before string array");
        return strs;
    }

    public static String[] readStringArray(ByteBuffer in, Charset charset) throws IllegalFormatException {
        return readStringArray(in, charset, DEF_SEPARATOR);
    }

    public static String[] readStringArray(ByteBuffer in) throws IllegalFormatException {
        return readStringArray(in, DEF_CHARSET, DEF_SEPARATOR);
    }


    public static byte[] writeStringArray(String[] in, Charset charset, byte separator) {
        byte[][] in_raw = new byte[in.length][];
        int bcount = 0;

        for(int i = 0; i < in.length; i++) {
            in_raw[i] = in[i].getBytes(charset);
            bcount += in_raw[i].length;
            if(i != 0)//The last can also have no separator
                bcount++;
        }
        byte[] res = new byte[bcount + 1];
        res[0] = (byte)in.length;
        bcount = 1;
        for(int i = 0; i < in.length; i++) {
            if(i != 0)
                res[bcount++] = separator;
            System.arraycopy(in_raw[i], 0, res, bcount, in_raw[i].length);
            bcount += in_raw[i].length;
        }
        return res;
    }

    public static byte[] writeStringArray(String[] in, Charset charset) {
        return writeStringArray(in, charset, DEF_SEPARATOR);
    }

    public static byte[] writeStringArray(String[] in) {
        return writeStringArray(in, DEF_CHARSET, DEF_SEPARATOR);
    }


    public static byte[] writeStringArray(List<String> in, Charset charset, byte separator) {
        byte[][] in_raw = new byte[in.size()][];
        int bcount = 0;

        int i = 0;
        for(String str : in) {
            in_raw[i] = str.getBytes(charset);
            bcount += in_raw[i].length;
            if(i != 0)//The last can also have no separator
                bcount++;
            i++;
        }
        byte[] res = new byte[bcount + 1];
        res[0] = (byte)in.size();
        bcount = 1;
        for(i = 0; i < in_raw.length; i++) {
            if(i != 0)
                res[bcount++] = separator;
            System.arraycopy(in_raw[i], 0, res, bcount, in_raw[i].length);
            bcount += in_raw[i].length;
        }
        return res;
    }

    public static byte[] writeStringArray(List<String> in, Charset charset) {
        return writeStringArray(in, charset, DEF_SEPARATOR);
    }

    public static byte[] writeStringArray(List<String> in) {
        return writeStringArray(in, DEF_CHARSET, DEF_SEPARATOR);
    }


    public static byte[] readAll(ByteBuffer buffer) {
        byte[] res = new byte[buffer.remaining()];
        buffer.get(res);
        return res;
    }
}
