package org.dice.core;

public final class Ct {

    private Ct() {}

    public record Tuple<T1, T2>(T1 t1, T2 t2){}

    public static abstract sealed class RESPTypes permits
            RESPSimpleString,
            RESPLong,
            RESPBulkString,
            RESPArray,
            RESPError,
            RESPNull,
            RESPBoolean,
            RESPMap,
            RESPSet,
            RESPDouble{

        public final Integer pos;

        private RESPTypes(final Integer pos) {
            this.pos = pos;
        }

    }

    public static final class RESPDouble extends RESPTypes {

        public RESPDouble(final Integer pos) {
            super(pos);
        }
    }

    public static final class RESPSet extends RESPTypes {
        public RESPSet(final Integer pos) {
            super(pos);
        }
    }

    public static final class RESPMap extends RESPTypes {
        public RESPMap(final Integer pos) {
            super(pos);
        }
    }

    public static final class RESPBoolean extends RESPTypes {
        public RESPBoolean(final Integer pos) {
            super(pos);
        }
    }

    public static final class RESPNull extends RESPTypes {
        public RESPNull(final Integer pos) {
            super(pos);
        }
    }

    public static final class RESPArray extends RESPTypes{

        public final RESPTypes[] val;

        public RESPArray(final RESPTypes[] val, final Integer pos) {
            super(pos);
            this.val = val;
        }

    }

    public static final class RESPLong extends RESPTypes{

        public final Long val;

        public RESPLong(final Long val, final Integer pos) {
            super(pos);
            this.val = val;
        }

    }

    public static final class RESPBulkString extends RESPTypes{

        public final String val;

        public RESPBulkString(final String val, final Integer pos) {
            super(pos);
            this.val = val;
        }

    }

    public static final class RESPError extends RESPTypes{

        public final String val;

        public RESPError(final String val, final Integer pos) {
            super(pos);
            this.val = val;
        }

    }

    public static final class RESPSimpleString extends RESPTypes{

        public final String val;

        public RESPSimpleString(final String val, final Integer pos) {
            super(pos);
            this.val = val;
        }

    }


}
