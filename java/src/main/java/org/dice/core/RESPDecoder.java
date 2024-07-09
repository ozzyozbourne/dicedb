package org.dice.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

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
        final var sign = switch (data[pos]) {
            case '+' -> {
                pos += 1;
                yield 1;
            }
            case '-' -> {
                pos += 1;
                yield -1;
            }
            default -> 1;
        };
        long value = 0;

        while (data[pos] != '\r') {
            value = value * 10 + (data[pos] - '0');
            pos += 1;
        }

        return value == 0? new Ct.RESPLong(value, pos + 2): new Ct.RESPLong(value * sign, pos + 2);
    }

    private static Ct.RESPBoolean readBoolean(final byte[] data, final int pos) {
        return (data[pos]) == 't'? new Ct.RESPBoolean(true, pos + 3): new Ct.RESPBoolean(false, pos + 3);
    }

    private static Ct.RESPNull readNull(final int pos) {
        return new Ct.RESPNull(pos + 2);
    }

    private static Ct.RESPSimpleString readSimpleString(final byte[] data, int pos) {
        logger.info("Simple String init -> {}", pos);
        final var start = pos;
        while (data[pos] != '\r') pos += 1;
        return new Ct.RESPSimpleString(new String(data, start, pos - start),pos + 2);
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
            arr[i] = decodeOne(data, r);
            r = arr[i].pos;
        }return new Ct.RESPArray(arr, r);
    }

    private static Ct.RESPMap readMap(final byte[] data, final int pos) {
        final var out = readLen(data, pos);
        final var map = new HashMap<Ct.RESPTypes, Ct.RESPTypes>(out.t1());
        int r = out.t2();
        for(int i = 0; i< out.t1(); i++){
            final var key = decodeOne(data, r);
            final var value = decodeOne(data, key.pos);
            map.put(key, value);
            r = value.pos;
        }return new Ct.RESPMap(map, r);
    }

    private static Ct.RESPSet readSet(final byte[] data, final int pos) {
        final var out = readLen(data, pos);
        final var set = new HashSet<Ct.RESPTypes>(out.t1());
        int r = out.t2();
        logger.info("position -> {} times -> {}", r, out.t1());
        for(int i = 0; i < out.t1(); i++){
            final var value = decodeOne(data, r);
            set.add(value);
            logger.info("{}",value);
            r = value.pos;
            logger.info("{}", r);
        }return new Ct.RESPSet(set, r);
    }

    private static Ct.RESPDouble readDouble(final byte[] data, int pos) {

        final Function<Integer, Ct.RESPSimpleString> readUptoPoint = p ->  {
            final var start = p;
            while (data[p] != '.') p++;
            return new Ct.RESPSimpleString(new String(data, start, p - start),p + 1);
        };

        final Function<Integer, Ct.RESPSimpleString> readAfterPoint = p ->  {
            final int start = p;
            while (data[p] != '\r') p++;
            return new Ct.RESPSimpleString(new String(data, start, p - start),p + 2);
        };

        final var sign = switch (data[pos]) {
            case '+' -> {
                pos += 1;
                yield '+';
            }
            case '-' -> {
                pos += 1;
                yield  '-';
            }
            default -> '+';
        };

        final Function<Integer, Optional<Ct.RESPDouble>> checkInfAndNan  = p -> switch (new String(data, p, 3)){
            case "inf" -> sign == '-'? Optional.of(new Ct.RESPDouble(Double.NEGATIVE_INFINITY, p + 5)):
                    Optional.of(new Ct.RESPDouble(Double.POSITIVE_INFINITY, p + 5));
            case "nan" -> Optional.of(new Ct.RESPDouble(Double.NaN, p + 5));
            default -> Optional.empty();
        };

        final var infNanCheck = checkInfAndNan.apply(pos);
        if (infNanCheck.isPresent()) return infNanCheck.get();

        final var n1 = readUptoPoint.apply(pos);
        final var n2 = readAfterPoint.apply(n1.pos);

        return Integer.parseInt(n1.val) == 0 && Integer.parseInt(n2.val) == 0?
                new Ct.RESPDouble(Double.parseDouble(format("%d.%d", 0, 0)), n2.pos):
                new Ct.RESPDouble(Double.parseDouble(format("%c%s.%s", sign, n1.val, n2.val)), n2.pos);
    }


    private static Ct.RESPTypes decodeOne(final byte[] data, final int pos) throws IllegalStateException {
        return switch (data[pos]) {
            case '+' -> readSimpleString(data, pos + 1);
            case '-' -> readError(data, pos + 1);
            case ':' -> readLong(data, pos + 1);
            case '$' -> readBulkString(data, pos + 1);
            case '*' -> readArray(data, pos + 1);
            case '_' -> readNull(pos + 1);
            case '#' -> readBoolean(data, pos + 1);
            case '%' -> readMap(data, pos + 1);
            case '~' -> readSet(data, pos + 1);
            case ',' -> readDouble(data, pos + 1);
            default ->  throw new IllegalStateException(format("Invalid RESP data type char -> %c", data[pos]));
        };
    }

    public static Ct.RESPTypes decode(final byte[] data) throws IllegalStateException {
        if (data.length == 0) throw new IllegalStateException("no data");
        return decodeOne(data, 0);
    }

}
