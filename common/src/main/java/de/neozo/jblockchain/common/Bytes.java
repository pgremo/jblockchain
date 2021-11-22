package de.neozo.jblockchain.common;

public final class Bytes {
    public static byte[] toByteArray(int value) {
        var result = new byte[Integer.BYTES];
        for (var i = 3; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= Integer.BYTES;
        }
        return result;
    }

    public static byte[] toByteArray(long value) {
        var result = new byte[Long.BYTES];
        for (var i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= Long.BYTES;
        }
        return result;
    }
}
