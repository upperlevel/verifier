package xyz.upperlevel.verifier.packetlib.utils;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.deshuffle;
import static xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.zero;

public class ByteSecurityUtilTest extends TestCase {

    public ByteSecurityUtilTest(String name) {
        super(name);
    }

    public void testBytes() {
        Random random = new Random();

        byte[] testArr = new byte[256];
        random.nextBytes(testArr);
        zero(testArr);
        checkZero(testArr);
    }

    public void testChars() {
        Random random = new Random();

        char[] testArr = "This is an ufficial test string, also used as password because we need to see if the util can clear this".toCharArray();
        zero(testArr);
        checkZero(testArr);
    }

    public void testDeshuffleList() {
        long seed = System.currentTimeMillis();
        List<String> alphabet = Arrays.asList("a", "b", "c", "d", "e", "f");
        Collections.shuffle(alphabet, new Random(seed));
        deshuffle(alphabet, new Random(seed));
        assertEquals(
                Arrays.asList("a", "b", "c", "d", "e", "f"),
                alphabet
        );
    }

    public void testDeshuffleArray() {
        long seed = System.currentTimeMillis();
        String[] alphabet = new String[]{"a", "b", "c", "d", "e", "f"};
        Collections.shuffle(Arrays.asList(alphabet), new Random(seed));
        deshuffle(alphabet, new Random(seed));
        Assert.assertArrayEquals(
                new String[]{"a", "b", "c", "d", "e", "f"},
                alphabet
        );
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
