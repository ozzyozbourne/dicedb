package org.dice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice.core.Ct;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.dice.core.RESPDecoder.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class RESPDecoderTest {

    private static final Logger logger = LogManager.getLogger(RESPDecoderTest.class);

    @Test
    public void simpleStringTest() {
        final var testcases =  Map.of(
                "+OK\r\n",           new Ct.Tuple<>("OK", 5),
                "+PONG\r\n",         new Ct.Tuple<>("PONG", 7),
                "+HELLO\r\n",        new Ct.Tuple<>("HELLO", 8),
                "+WORLD\r\n",        new Ct.Tuple<>("WORLD", 8),
                "+WELCOME\r\n",      new Ct.Tuple<>("WELCOME", 10),
                "+SUCCESS\r\n",      new Ct.Tuple<>("SUCCESS", 10),
                "+READY\r\n",        new Ct.Tuple<>("READY", 8),
                "+CONNECTED\r\n",    new Ct.Tuple<>("CONNECTED", 12),
                "+asdasdaweads\r\n", new Ct.Tuple<>("asdasdaweads", 15),
                "+#$%&&&\r\n",      new Ct.Tuple<>("#$%&&&", 9)
        );
        testcases.forEach((input, expected) -> {
            try {
                final var output = switch(decode(input.getBytes(StandardCharsets.US_ASCII))){
                    case Ct.RESPSimpleString res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t1(), output.val, expected.t1());
                assertEquals(expected.t2(), output.pos, expected.t1());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void simpleIntegerTest() {
        final var testcases = Map.of(
                ":1000\r\n",                 new Ct.Tuple<>(1000L, 7),
                ":0\r\n",                    new Ct.Tuple<>(0L, 4),
                ":-1000\r\n",                new Ct.Tuple<>(-1000L, 8),
                ":+500\r\n",                 new Ct.Tuple<>(500L, 7),
                ":-42\r\n",                  new Ct.Tuple<>(-42L, 6),
                ":-9223372036854775808\r\n", new Ct.Tuple<>(-9223372036854775808L, 23),
                ":2147483647\r\n",           new Ct.Tuple<>(2147483647L, 13),
                ":-99999\r\n",               new Ct.Tuple<>(-99999L, 9),
                ":-2147483648\r\n",          new Ct.Tuple<>(-2147483648L, 14),
                ":+123\r\n",                new Ct.Tuple<>(123L, 7)
        );

        testcases.forEach((input, expected) -> {
            try {
                final var output = switch(decode(input.getBytes(StandardCharsets.US_ASCII))){
                    case Ct.RESPLong res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t1(), output.val, expected.t1().toString());
                assertEquals(expected.t2(), output.pos, expected.t1().toString());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void errorTest() {
        final var testcases = Map.of(
                "-Error message\r\n",                                                     new Ct.Tuple<>("Error message", 16),
                "-ERR unknown command\r\n",                                               new Ct.Tuple<>("ERR unknown command", 22),
                "-WRONGTYPE Operation against a key holding the wrong kind of value\r\n", new Ct.Tuple<>("WRONGTYPE Operation against a key holding the wrong kind of value", 68),
                "-ERR syntax error\r\n",                                                  new Ct.Tuple<>("ERR syntax error", 19),
                "-NOAUTH Authentication required\r\n",                                    new Ct.Tuple<>("NOAUTH Authentication required", 33),
                "-NOPERM Operation not permitted\r\n",                                    new Ct.Tuple<>("NOPERM Operation not permitted", 33),
                "-ERR value is not an integer or out of range\r\n",                       new Ct.Tuple<>("ERR value is not an integer or out of range", 46),
                "-ERR command not supported\r\n",                                         new Ct.Tuple<>("ERR command not supported", 28),
                "-BUSY Redis is busy running a script\r\n",                               new Ct.Tuple<>("BUSY Redis is busy running a script", 38),
                "-NOSCRIPT No matching script. Please use EVAL.\r\n",                    new Ct.Tuple<>("NOSCRIPT No matching script. Please use EVAL.", 48)
        );

        testcases.forEach((input, expected) -> {
            try {
                final var output = switch(decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPError res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t1(), output.val, expected.t1());
                assertEquals(expected.t2(), output.pos, expected.t1());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void bulkStringTest() {
        final var testcases = Map.of(
                "$0\r\n\r\n",             new Ct.Tuple<>("", 6),
                "$3\r\nfoo\r\n",          new Ct.Tuple<>("foo", 9),
                "$11\r\nHello World\r\n", new Ct.Tuple<>("Hello World", 18),
                "$5\r\nh@#lo\r\n",        new Ct.Tuple<>("h@#lo", 11),
                "$4\r\n1234\r\n",         new Ct.Tuple<>("1234", 10),
                "$3\r\n \t \r\n",         new Ct.Tuple<>(" \t ", 9),
                "$4\r\nfoo\n\r\n",        new Ct.Tuple<>("foo\n", 10),
                "$8\r\nfoo\r\nbar\r\n",   new Ct.Tuple<>("foo\r\nbar", 14),
                "$9\r\nh@llo123!\r\n",    new Ct.Tuple<>("h@llo123!", 15),
                "$7\r\n$%^&*()\r\n",     new Ct.Tuple<>("$%^&*()", 13)
        );

        testcases.forEach((input, expected) -> {
            try {
                final var output = switch (decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPBulkString res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t1(), output.val, expected.t1());
                assertEquals(expected.t2(), output.pos, expected.t1());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void booleanTest() {
        final var testcases = Map.of(
                "#t\r\n", new Ct.Tuple<>(true, 4),
                "#f\r\n", new Ct.Tuple<>(false, 4)
        );

        testcases.forEach((input, expected) -> {
            try {
                final var output = switch (decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPBoolean res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t1(), output.val, expected.t1().toString());
                assertEquals(expected.t2(), output.pos, expected.t1().toString());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void nullTest() {
        final var testcases = Map.of("_\r\n", new Ct.Tuple<>(true, 3));

        testcases.forEach((input, expected) -> {
            try {
                final var output = switch (decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPNull res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t2(), output.pos, expected.t1().toString());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void doubleTest1() {
        final var testcases = Map.of(
                ",1.23\r\n",       new Ct.Tuple<>(1.23, 7),
                ",-4.56\r\n",      new Ct.Tuple<>(-4.56, 8),
                ",0.789\r\n",      new Ct.Tuple<>(0.789, 8),
                ",-0.001\r\n",     new Ct.Tuple<>(-0.001, 9),
                ",-101.23123\r\n", new Ct.Tuple<>(-101.23123, 13),
                ",-123.42123\r\n", new Ct.Tuple<>(-123.42123, 13),
                ",123.456\r\n",    new Ct.Tuple<>(123.456, 10),
                ",0.0\r\n",        new Ct.Tuple<>(0.0, 6),
                ",-0.0\r\n",       new Ct.Tuple<>(0.0, 7),
                ",-1123.321\r\n", new Ct.Tuple<>(-1123.321, 12)
        );

        testcases.forEach((input, expected) -> {
            try {
                final var output = switch (decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPDouble res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t1(), output.val, expected.t1().toString());
                assertEquals(expected.t2(), output.pos, expected.t1().toString());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void doubleTest2() {
        final var testcases = Map.of(
                ",inf\r\n",  new Ct.Tuple<>(Double.POSITIVE_INFINITY, 6),
                ",-inf\r\n", new Ct.Tuple<>(Double.NEGATIVE_INFINITY, 7),
                ",+inf\r\n", new Ct.Tuple<>(Double.POSITIVE_INFINITY, 7),
                ",nan\r\n",  new Ct.Tuple<>(Double.NaN, 6)
        );

        testcases.forEach((input, expected) -> {
            try {
                final var output = switch (decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPDouble res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                assertEquals(expected.t1(), output.val, expected.t1().toString());
                assertEquals(expected.t2(), output.pos, expected.t1().toString());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void setTest() {

        final var testcases = Map.of(

                "~0\r\n", new Ct.Tuple<Set<Ct.RESPTypes>, Integer>(Set.of(), 4),

                "~1\r\n+OK\r\n", new Ct.Tuple<Set<Ct.RESPTypes>, Integer>(Set.of(new Ct.RESPSimpleString("OK", 9)), 9),

                "~2\r\n+Hello\r\n+World\r\n", new Ct.Tuple<Set<Ct.RESPTypes>, Integer>(Set.of(
                        new Ct.RESPSimpleString("Hello", 12),
                        new Ct.RESPSimpleString("World", 20)
                ), 20),

                "~3\r\n:1\r\n:2\r\n:3\r\n", new Ct.Tuple<Set<Ct.RESPTypes>, Integer>(Set.of(
                        new Ct.RESPLong(1L, 8),
                        new Ct.RESPLong(2L, 12),
                        new Ct.RESPLong(3L, 16)
                ), 16),

                "~2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n", new Ct.Tuple<Set<Ct.RESPTypes>, Integer>(Set.of(
                        new Ct.RESPBulkString("foo", 13),
                        new Ct.RESPBulkString("bar", 22)
                ), 22),

                "~3\r\n#t\r\n#f\r\n_\r\n", new Ct.Tuple<>(Set.of(
                        new Ct.RESPBoolean(true, 8),
                        new Ct.RESPBoolean(false, 12),
                        new Ct.RESPNull(15)
                ), 15),

                "~6\r\n,inf\r\n,-101.23123\r\n-ERR unknown command\r\n,nan\r\n$3\r\nbar\r\n:1000\r\n", new Ct.Tuple<>(Set.of(
                        new Ct.RESPDouble(Double.POSITIVE_INFINITY, 10),
                        new Ct.RESPDouble(-101.23123, 23),
                        new Ct.RESPError("ERR unknown command", 45),
                        new Ct.RESPDouble(Double.NaN, 51),
                        new Ct.RESPBulkString("bar", 60),
                        new Ct.RESPLong(1000L, 67)
                ), 67)

        );

        testcases.forEach((input, expected) -> {
            try {
                logger.info("input set is -> {}", input);
                final var output = switch (decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPSet res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                //Check Size
                assertEquals(expected.t1().size(), output.val.size(), expected.t1().toString());

                //Check Elements
                expected.t1().forEach(s ->{
                    switch (s){
                        case Ct.RESPSet res            -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPArray res          -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPMap res            -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPVerbatimString res -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPSimpleString res   -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPBoolean res        -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPBulkString res     -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPDouble res         -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPNull res           -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPLong res           -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                        case Ct.RESPError res          -> assertTrue(output.val.contains(res), "Actual Set -> %s\nExpected Value -> %s".formatted(output.val.toString() ,res.toString()));
                    }
                });

                //Check pos
                assertEquals(expected.t2(), output.pos, expected.t1().toString());

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void arrayTest(){
        final var testcases = Map.of(

                "*0\r\n", new Ct.Tuple<>(new Ct.RESPTypes[]{}, 4),

                "*1\r\n+OK\r\n", new Ct.Tuple<>(new Ct.RESPTypes[]{new Ct.RESPSimpleString("OK", 9)}, 9),

                "*7\r\n,inf\r\n,-101.23123\r\n-ERR unknown command\r\n" +
                        "*5\r\n:1\r\n$3\r\nfoo\r\n#t\r\n_\r\n,-101.23123\r\n"+
                        ",nan\r\n$3\r\nbar\r\n:1000\r\n",
                new Ct.Tuple<>(
                        new Ct.RESPTypes[]{
                        new Ct.RESPDouble(Double.POSITIVE_INFINITY, 10),
                        new Ct.RESPDouble(-101.23123, 23),
                        new Ct.RESPError("ERR unknown command", 45),
                        new Ct.RESPArray(new Ct.RESPTypes[]{
                                new Ct.RESPLong(1L, 53),
                                new Ct.RESPBulkString("foo", 62),
                                new Ct.RESPBoolean(true, 66),
                                new Ct.RESPNull(69),
                                new Ct.RESPDouble(-101.23123, 82)
                        },82),
                        new Ct.RESPDouble(Double.NaN, 88),
                        new Ct.RESPBulkString("bar", 97),
                        new Ct.RESPLong(1000L, 104)}
                        , 104)
        );

        testcases.forEach((input, expected) -> {
            try {
                logger.info("input array is -> {}", input);
                final var output = switch (decode(input.getBytes(StandardCharsets.US_ASCII))) {
                    case Ct.RESPArray res -> res;
                    default -> throw new RuntimeException("FAILED PATTERN MATCH");
                };

                //Check Size
                assertEquals(expected.t1().length, output.val.length, Arrays.toString(expected.t1()));

                //Check Elements
                Arrays.stream(expected.t1()).forEach(s ->{
                    switch (s){
                        case Ct.RESPSet res            ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPArray res          ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPMap res            ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPVerbatimString res ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPSimpleString res   ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPBoolean res        ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPBulkString res     ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPDouble res         ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPNull res           ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPLong res           ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                        case Ct.RESPError res          ->
                                assertEquals(1L, Arrays.stream(output.val).filter(a -> a.equals(res)).count(), "Actual Array -> %s\nExpected Value -> %s".formatted(Arrays.toString(output.val), res.toString()));

                    }
                });

                //Check pos
                assertEquals(expected.t2(), output.pos, Arrays.toString(expected.t1()));

            } catch (final Exception e) {
                logger.error(e);
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void mapTest(){

    }
}