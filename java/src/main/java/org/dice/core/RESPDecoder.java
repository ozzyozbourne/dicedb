package org.dice.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.String.format;

public final class RESPDecoder {

    private RESPDecoder() {}

    private static final Logger logger = LogManager.getLogger(RESPDecoder.class);

    private static Ct.Tuple<Integer, Integer> readLen(final byte[] data, int pos) {
        int val = 0;
        for(;pos < data.length; pos++){
            if (data[pos] < '0' || data[pos] > '9') break;
            val = (val * 10) + (data[pos] - '0');
        }return new Ct.Tuple<>(val, pos + 2);
    }


    private static Ct.RESPLong readLong(final byte[] data, int pos) {
        long sign = 1;

        switch (data[pos]) {
            case '+' -> pos += 1;
            case '-' -> {
                pos += 1;
                sign = -sign;
            }
            default -> logger.info("Integer has no sign");
        }

        long value = 0;

        while (data[pos] != '\r') {
            value = value * 10 + (data[pos] - '0');
            pos += 1;
        }

        return value == 0? new Ct.RESPLong(value, pos + 2): new Ct.RESPLong(value * sign, pos + 2);
    }

    private static Ct.RESPSimpleString readSimpleString(final byte[] data, int pos) {
        while (data[pos] != '\r') pos++;
        return new Ct.RESPSimpleString(new String(data, 1, pos - 1),pos + 2);
    }

    private static Ct.RESPError  readError(byte[] data, final int pos) {
        final Ct.RESPSimpleString parseError = readSimpleString(data, pos);
        return new Ct.RESPError(parseError.val, parseError.pos);
    }

    private static Ct.RESPBulkString readBulkString(final byte[] data, final int pos) {
        final var out = readLen(data, pos);
        return new Ct.RESPBulkString(new String(data, out.t2(), out.t1()), out.t2() + out.t1() + 2);
    }

    private static Ct.RESPArray readArray(final byte[] data, final int pos) throws IllegalStateException {
        final var out = readLen(data, pos);
        final var arr = new Ct.RESPTypes[out.t1()];
        int r = out.t2();
        for(int i = 0; i< out.t1(); i++){
            arr[i] = decodeOne(data, r);;
            r = arr[i].pos;
        }return new Ct.RESPArray(arr, r);
    }

    private static Ct.RESPBoolean readBoolean(final byte[] data, final int pos) {return new Ct.RESPBoolean(1);}
    private static Ct.RESPNull readNull(final byte[] data, final int pos) {return new Ct.RESPNull(1);}
    private static Ct.RESPMap readMap(final byte[] data, final int pos) {return new Ct.RESPMap(1);}
    private static Ct.RESPSet readSet(final byte[] data, final int pos) {return new Ct.RESPSet(1);}
    private static Ct.RESPDouble readDouble(final byte[] data, final int pos) {return new Ct.RESPDouble(1);}


    private static Ct.RESPTypes decodeOne(final byte[] data, final int pos) throws IllegalStateException {
        return switch (data[pos]) {
            case '+' -> readSimpleString(data, pos + 1);
            case '-' -> readError(data, pos + 1);
            case ':' -> readLong(data, pos + 1);
            case '$' -> readBulkString(data, pos + 1);
            case '*' -> readArray(data, pos + 1);
            case '_' -> readNull(data, pos + 1);
            case '#' -> readBoolean(data, pos + 1);
            case '%' -> readMap(data, pos + 1);
            case '~' -> readSet(data, pos + 1);
            case ',' -> readDouble(data, pos + 1);
            default ->  throw new IllegalStateException(format("Invalid RESP data type char -> %c", data[pos]));
        };
    }

    public static Ct.RESPTypes decode(final byte[] data) throws Exception {
        if (data.length == 0) throw new Exception("no data");
        return decodeOne(data, 0);
    }

}
