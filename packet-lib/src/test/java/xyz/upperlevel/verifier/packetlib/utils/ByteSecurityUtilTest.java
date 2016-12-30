package xyz.upperlevel.verifier.packetlib.utils;

import junit.framework.TestCase;

import java.util.Random;

public class ByteSecurityUtilTest extends TestCase {

    public ByteSecurityUtilTest(String name) {
        super(name);
    }

    public void testBytes() {
        Random random = new Random();

        byte[] testArr = new byte[256];
        random.nextBytes(testArr);
        ByteSecurityUtil.zero(testArr);
        checkZero(testArr);
    }

    public void testChars() {
        Random random = new Random();

        char[] testArr = "This is an ufficial test string, also used as password because we need to see if the util can clear this".toCharArray();
        ByteSecurityUtil.zero(testArr);
        checkZero(testArr);
    }


    private void checkZero(byte[] arr) {
        for (byte anArr : arr)
            assert anArr == 0;
    }

    private void checkZero(char[] arr) {
        for (char anArr : arr)
            assert anArr == 0;
    }
}
