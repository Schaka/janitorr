package sun.nio.fs;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

/**
 * Utility methods
 * Only change from Adoptium JDK 23 - use UTF-8 as default charset
 * Otherwise native images are broken.
 * - https://github.com/oracle/graal/issues/9879
 * - https://github.com/paketo-buildpacks/native-image/issues/344
 */

class Util {
    private Util() { }

    private static final Charset jnuEncoding = StandardCharsets.UTF_8;

    /**
     * Returns {@code Charset} corresponding to the sun.jnu.encoding property
     */
    static Charset jnuEncoding() {
        return jnuEncoding;
    }

    /**
     * Encodes the given String into a sequence of bytes using the {@code Charset}
     * specified by the sun.jnu.encoding property.
     */
    static byte[] toBytes(String s) {
        return s.getBytes(jnuEncoding);
    }

    /**
     * Constructs a new String by decoding the specified array of bytes using the
     * {@code Charset} specified by the sun.jnu.encoding property.
     */
    static String toString(byte[] bytes) {
        return new String(bytes, jnuEncoding);
    }


    /**
     * Splits a string around the given character. The array returned by this
     * method contains each substring that is terminated by the character. Use
     * for simple string spilting cases when needing to avoid loading regex.
     */
    static String[] split(String s, char c) {
        int count = 0;
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i) == c)
                count++;
        }
        String[] result = new String[count+1];
        int n = 0;
        int last = 0;
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i) == c) {
                result[n++] = s.substring(last, i);
                last = i + 1;
            }
        }
        result[n] = s.substring(last, s.length());
        return result;
    }

    /**
     * Returns a Set containing the given elements.
     */
    @SafeVarargs
    static <E> Set<E> newSet(E... elements) {
        HashSet<E> set = new HashSet<>();
        for (E e: elements) {
            set.add(e);
        }
        return set;
    }

    /**
     * Returns a Set containing all the elements of the given Set plus
     * the given elements.
     */
    @SafeVarargs
    static <E> Set<E> newSet(Set<E> other, E... elements) {
        HashSet<E> set = new HashSet<>(other);
        for (E e: elements) {
            set.add(e);
        }
        return set;
    }

    /**
     * Returns {@code true} if symbolic links should be followed
     */
    static boolean followLinks(LinkOption... options) {
        boolean followLinks = true;
        for (LinkOption option: options) {
            if (option == LinkOption.NOFOLLOW_LINKS) {
                followLinks = false;
            } else if (option == null) {
                throw new NullPointerException();
            } else {
                throw new AssertionError("Should not get here");
            }
        }
        return followLinks;
    }
}