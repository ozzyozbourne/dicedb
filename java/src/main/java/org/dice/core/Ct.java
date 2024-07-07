package org.dice.core;


public final class Ct {

    private Ct() {}

    public record Tuple<T1, T2>(T1 t1, T2 t2){}

    public static abstract sealed class RESPTypes permits RESPSimpleString, RESPLong, RESPBulkString, RESPArray, RESPError {}
    public static final class RESPArray extends RESPTypes{}

    public static final class RESPLong extends RESPTypes{

        final Long len;
        final Integer pos;

        public RESPLong(final Long len, final Integer pos) {
            this.len = len;
            this.pos = pos;
        }
    }

    public static final class RESPBulkString extends RESPTypes{}

    public static final class RESPError extends RESPTypes{

        final String err;
        final Integer i;

        public RESPError(final String err, final Integer i) {
            this.err = err;
            this.i = i;
        }
    }

    public static final class RESPSimpleString extends RESPTypes{

        final String st;
        final Integer i;

        public RESPSimpleString(final String st, final Integer i) {
            this.st = st;
            this.i = i;
        }
    }


}
