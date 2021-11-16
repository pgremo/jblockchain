package de.neozo.jblockchain.common;

public final class Bytes {
    public static byte[] toByteArray(long value) {
        var result = new byte[8];
        for (var i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return result;
    }
}
