package xyz.upperlevel.verifier.packetlib.utils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @SuppressWarnings("unchecked")
    public static  <T> void deshuffle(List<T> in, Random rand) {
        List<Integer> mapping = IntStream.range(0, in.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(mapping, rand);
        Object[] res = new Object[in.size()];
        for(int i = 0; i < mapping.size(); i++)
            res[mapping.get(i)] = in.get(i);
        for(int i = 0; i < mapping.size(); i++)
            in.set(i, (T) res[i]);
    }

    @SuppressWarnings("unchecked")
    public static  <T> void deshuffle(T[] in, Random rand) {
        List<Integer> mapping = IntStream.range(0, in.length).boxed().collect(Collectors.toList());
        Collections.shuffle(mapping, rand);
        Object[] res = new Object[in.length];
        for(int i = 0; i < mapping.size(); i++)
            res[mapping.get(i)] = in[i];
        for(int i = 0; i < mapping.size(); i++)
            in[i] = (T) res[i];
    }
}
