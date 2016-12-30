package xyz.upperlevel.verifier.packetlib;

import lombok.Builder;
import lombok.Data;
import xyz.upperlevel.verifier.packetlib.PacketManager.PacketTypeLength;

import static xyz.upperlevel.verifier.packetlib.PacketManager.PacketTypeLength.SHORT;

@Data
@Builder
public class SimpleConnectionOptions {
    public static final SimpleConnectionOptions DEFAULT = builder().build();
    private final PacketTypeLength typeBytes;
    private final int maxPacketSize;
    private final int lengthBytes;

    public static class SimpleConnectionOptionsBuilder {
        private int maxPacketSize = 8192;
        private PacketTypeLength typeBytes = SHORT;
        private int lengthBytes = 4;

        public SimpleConnectionOptionsBuilder typeBytes(int bytes) {
            PacketTypeLength n = PacketTypeLength.getFromBytes(bytes);
            if(n == null)
                throw new IllegalArgumentException("The Type length must be 1, 2 or 4");
            this.typeBytes = n;
            return this;
        }

        public SimpleConnectionOptionsBuilder typeBytes(PacketTypeLength bytes) {
            this.typeBytes = bytes;
            return this;
        }

        public SimpleConnectionOptionsBuilder lengthBytes(int bytes) {
            if(bytes < 1 || bytes > 8)
                throw new IllegalArgumentException("The bytes length must be between 1 and 8 (inclusive)");
            lengthBytes = bytes;
            return this;
        }

        public SimpleConnectionOptionsBuilder lengthBytes(PacketTypeLength bytes) {
            lengthBytes = bytes.bytes;
            return this;
        }
    }
}
