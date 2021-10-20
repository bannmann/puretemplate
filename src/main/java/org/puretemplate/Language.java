package org.puretemplate;

import java.util.Set;

import lombok.experimental.UtilityClass;

import com.google.common.base.CharMatcher;

@UtilityClass
class Language
{
    static final Set<String> PREDEFINED_ANON_SUBTEMPLATE_ATTRIBUTES = Set.of("i", "i0");

    private static final CharMatcher RESERVED_CHARACTERS = CharMatcher.inRange('a', 'z')
        .or(CharMatcher.inRange('A', 'Z'))
        .or(CharMatcher.inRange('0', '9'))
        .or(CharMatcher.anyOf("@-_[]"));

    /**
     * Determines if a specified character may be used as a user-specified delimiter.
     *
     * @param c The character
     *
     * @return {@code true} if the character is reserved by the StringTemplate language; otherwise, {@code false} if the
     * character may be used as a delimiter.
     */
    public static boolean isReservedCharacter(char c)
    {
        return RESERVED_CHARACTERS.matches(c);
    }
}
