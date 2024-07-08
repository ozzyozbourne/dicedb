package org.dice.core;

import java.util.Map;
import java.util.Set;

/**
 * A utility class for various RESP types used in the Dice application.
 */
public final class Ct {

    /**
     * Private constructor to prevent instantiation.
     */
    private Ct() {}

    /**
     * A generic tuple class.
     *
     * @param <T1> the type of the first element
     * @param <T2> the type of the second element
     */
    public record Tuple<T1, T2>(T1 t1, T2 t2){}

    /**
     * An abstract sealed class representing RESP types.
     */
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
            RESPDouble,
            RESPVerbatimString{

        /**
         * The position where the RESP type ends in the buffer plus one.
         */
        public final Integer pos;

        /**
         * Constructor for RESP types.
         *
         * @param pos the position where the RESP type ends in the buffer plus one
         */
        private RESPTypes(final Integer pos) {
            this.pos = pos;
        }
    }

    /**
     * A class representing RESP verbatim strings.
     */
    public static final class RESPVerbatimString extends RESPTypes {

        /**
         * Constructor for RESP verbatim strings.
         *
         * @param pos the position where the RESP verbatim string ends in the buffer plus one
         */
        private RESPVerbatimString(final Integer pos) {
            super(pos);
        }
    }

    /**
     * A class representing RESP doubles.
     */
    public static final class RESPDouble extends RESPTypes {

        /**
         * The value of the RESP double.
         */
        public final Double val;

        /**
         * Constructor for RESP doubles.
         *
         * @param val the value of the RESP double
         * @param pos the position where the RESP double ends in the buffer plus one
         */
        public RESPDouble(final Double val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP sets.
     */
    public static final class RESPSet extends RESPTypes {

        /**
         * The value of the RESP set.
         */
        public final Set<RESPTypes> val;

        /**
         * Constructor for RESP sets.
         *
         * @param val the value of the RESP set
         * @param pos the position where the RESP set ends in the buffer plus one
         */
        public RESPSet(final Set<RESPTypes> val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP maps.
     */
    public static final class RESPMap extends RESPTypes {

        /**
         * The value of the RESP map.
         */
        public final Map<RESPTypes, RESPTypes> val;

        /**
         * Constructor for RESP maps.
         *
         * @param val the value of the RESP map
         * @param pos the position where the RESP map ends in the buffer plus one
         */
        public RESPMap(final Map<RESPTypes, RESPTypes> val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP booleans.
     */
    public static final class RESPBoolean extends RESPTypes {

        /**
         * The value of the RESP boolean.
         */
        public final Boolean val;

        /**
         * Constructor for RESP booleans.
         *
         * @param val the value of the RESP boolean
         * @param pos the position where the RESP boolean ends in the buffer plus one
         */
        public RESPBoolean(final Boolean val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP null values.
     */
    public static final class RESPNull extends RESPTypes {

        /**
         * Constructor for RESP null values.
         *
         * @param pos the position where the RESP null value ends in the buffer plus one
         */
        public RESPNull(final Integer pos) {
            super(pos);
        }
    }

    /**
     * A class representing RESP arrays.
     */
    public static final class RESPArray extends RESPTypes {

        /**
         * The value of the RESP array.
         */
        public final RESPTypes[] val;

        /**
         * Constructor for RESP arrays.
         *
         * @param val the value of the RESP array
         * @param pos the position where the RESP array ends in the buffer plus one
         */
        public RESPArray(final RESPTypes[] val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP long integers.
     */
    public static final class RESPLong extends RESPTypes {

        /**
         * The value of the RESP long integer.
         */
        public final Long val;

        /**
         * Constructor for RESP long integers.
         *
         * @param val the value of the RESP long integer
         * @param pos the position where the RESP long integer ends in the buffer plus one
         */
        public RESPLong(final Long val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP bulk strings.
     */
    public static final class RESPBulkString extends RESPTypes {

        /**
         * The value of the RESP bulk string.
         */
        public final String val;

        /**
         * Constructor for RESP bulk strings.
         *
         * @param val the value of the RESP bulk string
         * @param pos the position where the RESP bulk string ends in the buffer plus one
         */
        public RESPBulkString(final String val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP error messages.
     */
    public static final class RESPError extends RESPTypes {

        /**
         * The value of the RESP error message.
         */
        public final String val;

        /**
         * Constructor for RESP error messages.
         *
         * @param val the value of the RESP error message
         * @param pos the position where the RESP error message ends in the buffer plus one
         */
        public RESPError(final String val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }

    /**
     * A class representing RESP simple strings.
     */
    public static final class RESPSimpleString extends RESPTypes {

        /**
         * The value of the RESP simple string.
         */
        public final String val;

        /**
         * Constructor for RESP simple strings.
         *
         * @param val the value of the RESP simple string
         * @param pos the position where the RESP simple string ends in the buffer plus one
         */
        public RESPSimpleString(final String val, final Integer pos) {
            super(pos);
            this.val = val;
        }
    }
}
