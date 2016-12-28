package me.upperlevel.verifier.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketHandler;
import me.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;
import me.upperlevel.verifier.packetlib.utils.ByteConvUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import static me.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.zero;

@AllArgsConstructor
public class LoginPacket {
    public static final PacketHandler<LoginPacket> HANDLER = new LogPacketHandler();

    public static final Charset CHARSET = ByteConvUtils.DEF_CHARSET;
    public static final byte SEPARATOR = ByteConvUtils.DEF_SEPARATOR;


    @Getter
    private final String clazz;

    @Getter
    private final String user;

    @Getter
    private final char[] password;

    private static class LogPacketHandler extends PacketHandler<LoginPacket> {
        protected LogPacketHandler() {
            super("login", LoginPacket.class);
        }

        @Override public LoginPacket decode(byte[] encoded) throws IllegalPacketException {
            int i = 0;

            ByteBuffer buffer = ByteBuffer.wrap(encoded);

            String clazz = ByteConvUtils.readString(buffer, CHARSET, SEPARATOR);
            String user = ByteConvUtils.readString(buffer, CHARSET, SEPARATOR);

            char[] chars = ByteConvUtils.DEF_CHARSET.decode(buffer).array();
            zero(encoded, i, encoded.length);

            return new LoginPacket(clazz, user, chars);
        }

        @Override public byte[] encode(LoginPacket decoded) {
            byte[] clazz_raw = decoded.clazz.getBytes(CHARSET);
            byte[] user_raw = decoded.user.getBytes(CHARSET);
            ByteBuffer passw_raw = CHARSET.encode(CharBuffer.wrap(decoded.password));
            byte[] res = ByteBuffer.allocate(clazz_raw.length + 1 + user_raw.length + 1 + passw_raw.remaining())
                    .put(clazz_raw)
                    .put(SEPARATOR)
                    .put(user_raw)
                    .put(SEPARATOR)
                    .put(passw_raw)
                    .array();
            zero(passw_raw);
            return res;
        }
    }

    public void dispose() {
        zero(password);
    }
}
