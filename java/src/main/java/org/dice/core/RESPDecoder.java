package org.dice.core;

public final class RESPDecoder {

    private RESPDecoder() {}

    /**
     * Reads the length from a byte array. The length is typically the first integer in the string.
     * The function reads until it encounters a non-digit byte and returns the integer length
     * and the delta, which is the position of the non-digit byte plus 2 (to account for CRLF).
     *
     * @param data the byte array containing the length
     * @return an Optional containing a tuple where the first element is the parsed length and the second element is the delta,
     *         or an empty Optional if the length could not be parsed
     */
    private static Ct.RESPLong readInt(final byte[] data) {
        int pos = 1;
        long value = 0;

        while (data[pos] != '\r') {
            value = value * 10 + (data[pos] - '0');
            pos++;
        }
        return new Ct.RESPLong(value, pos + 2);
    }

    /**
     * Reads a RESP encoded simple string from data and returns
     * the string and the delta.
     *
     * @param data the byte array containing the simple string
     * @return a tuple containing the decoded string and the delta
     */
    private static Ct.RESPSimpleString readSimpleString(final byte[] data) {
        int pos = 1; // first character +
        while (data[pos] != '\r') pos++;
        return new Ct.RESPSimpleString(new String(data, 1, pos - 1),pos + 2);
    }

    /**
     * Reads a RESP encoded error from the given byte array and returns
     * the error string and the delta.
     *
     * @param data the byte array containing the RESP encoded error
     * @return a tuple containing the error string and the delta
     */
    private static Ct.RESPError  readError(byte[] data) {
        final Ct.RESPSimpleString parseError = readSimpleString(data);
        return new Ct.RESPError(parseError.st, parseError.i);
    }

    // Reads a RESP encoded string from data and returns
    // the string, the delta, and the error
    private static String[] readBulkString(final byte[] data) {
        // first character $
        int pos = 1;

        // reading the length and forwarding the pos by
        // the length of the integer + the first special character
        int[] lenAndDelta = readLength(data, pos);
        int len = lenAndDelta[0];
        pos += lenAndDelta[1];

        // reading `len` bytes as string
        return new String[]{new String(data, pos, len), String.valueOf(pos + len + 2)};
    }

    private static Ct.RESPTypes decode(final byte[] data) throws Exception {
        if (data.length == 0) throw new Exception("no data");
        return switch (data[0]) {
            case '+' -> readSimpleString(data);
            case '-' -> readError(data);
            case ':' -> readInt(data);
            case '$' -> readBulkString(data);
            case '*' -> readArray(data);
            default ->  throw new Exception("Invalid RESP data type");
        };
    }

}
