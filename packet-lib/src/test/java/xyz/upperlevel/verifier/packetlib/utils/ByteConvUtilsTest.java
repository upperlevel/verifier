package xyz.upperlevel.verifier.packetlib.utils;

import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static xyz.upperlevel.verifier.packetlib.utils.ByteConvUtils.*;

public class ByteConvUtilsTest extends TestCase {
    public ByteConvUtilsTest(String name) {
        super(name);
    }


    public void testString() {
        assertEquals(
                readString("lol".getBytes(DEF_CHARSET), 0),
                "lol"
        );

        assertEquals(
                readString("lol\0".getBytes(DEF_CHARSET), 0, (byte)'\0'),
                "lol"
        );

        assertEquals(
                readString(" lol".getBytes(DEF_CHARSET), 1),
                "lol"
        );

        assertEquals(
                readString(ByteBuffer.wrap("lol".getBytes(DEF_CHARSET))),
                "lol"
        );

        ByteBuffer buffer = ByteBuffer.wrap(concat("test1".getBytes(DEF_CHARSET), DEF_SEPARATOR, "test2".getBytes(DEF_CHARSET)));
        assertEquals(
                readString(buffer),
                "test1"
        );

        assertEquals(
                readString(buffer),
                "test2"
        );
    }

    public void testStringArray() {
        //------------------------write
        //-------------Array
        assertArrayEquals(
                writeStringArray(new String[]{"test1", "test2"}),
                concat(
                        (byte)2,
                        "test1".getBytes(DEF_CHARSET),
                        DEF_SEPARATOR,
                        "test2".getBytes(DEF_CHARSET)
                )
        );

        assertArrayEquals(
                writeStringArray(new String[]{"test1"}),
                concat(
                        (byte)1,
                        "test1".getBytes(DEF_CHARSET)
                )
        );

        //--------------List
        assertArrayEquals(
                writeStringArray(Arrays.asList("test1", "test2")),
                concat(
                        (byte)2,
                        "test1".getBytes(DEF_CHARSET),
                        DEF_SEPARATOR,
                        "test2".getBytes(DEF_CHARSET)
                )
        );

        assertArrayEquals(
                writeStringArray(Collections.singletonList("test1")),
                concat(
                        (byte)1,
                        "test1".getBytes(DEF_CHARSET)
                )
        );

        //----------------------read


        assertArrayEquals(
                readStringArray(ByteBuffer.wrap(
                        concat(
                                (byte)2,
                                "test1".getBytes(DEF_CHARSET),
                                DEF_SEPARATOR,
                                "test2".getBytes(DEF_CHARSET)
                        )
                )),
                new String[] {"test1", "test2"}
        );

        assertArrayEquals(
                readStringArray(ByteBuffer.wrap(concat((byte)1, "test1".getBytes(DEF_CHARSET)))),
                new String[] {"test1"}
        );

        //----------------------both

        assertArrayEquals(
                readStringArray(
                        ByteBuffer.wrap(writeStringArray(
                                new String[]{"lol", "lal", "lel"}
                        ))
                ),
                new String[]{"lol", "lal", "lel"}
        );
        assertArrayEquals(
                readStringArray(
                        ByteBuffer.wrap(writeStringArray(
                                new String[]{"lol"}
                        ))
                ),
                new String[]{"lol"}
        );
    }

    private byte[] concat(byte a, byte[] b) {
        return ByteBuffer.allocate(1 + b.length)
                .put(a)
                .put(b)
                .array();
    }

    private byte[] concat(byte[] a, byte b, byte[] c) {
        return ByteBuffer.allocate(a.length + 1 + c.length)
                .put(a)
                .put(b)
                .put(c)
                .array();
    }

    private byte[] concat(byte a, byte[] b, byte c, byte[] d) {
        return ByteBuffer.allocate(1 + b.length + 1 + d.length)
                .put(a)
                .put(b)
                .put(c)
                .put(d)
                .array();
    }
}
