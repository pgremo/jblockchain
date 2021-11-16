package de.neozo.jblockchain.common;

public final class Bytes {
    public static byte[] toByteArray(int value) {
        var result = new byte[4];
        for (var i = 3; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 4;
        }
        return result;
    }

    public static byte[] toByteArray(long value) {
        var result = new byte[8];
        for (var i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return result;
    }
}
