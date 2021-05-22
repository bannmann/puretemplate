package com.github.bannmann.puretemplate.compiler;

import org.antlr.runtime.Token;

/**
 * Represents the name of a formal argument defined in a template:
 * <pre>
 *  test(a,b,x=defaultvalue) ::= "&lt;a&gt; &lt;n&gt; &lt;x&gt;"
 * </pre> Each template has a set of these formal arguments or sets
 * {@link CompiledST#hasFormalArgs} to {@code false} (indicating that no arguments were specified such as when we create
 * a template with {@code new ST(...)}).
 *
 * <p>
 * Note: originally, I tracked cardinality as well as the name of an attribute. I'm leaving the code here as I suspect
 * something may come of it later. Currently, though, cardinality is not used.</p>
 */
public class FormalArgument
{
/*
    // the following represent bit positions emulating a cardinality bitset.
    public static final int OPTIONAL = 1;     // a?
    public static final int REQUIRED = 2;     // a
    public static final int ZERO_OR_MORE = 4; // a*
    public static final int ONE_OR_MORE = 8;  // a+
    public static final String[] suffixes = {
        null,
        "?",
        "",
        null,
        "*",
        null,
        null,
        null,
        "+"
    };
    protected int cardinality = REQUIRED;
     */

    public String name;

    public int index; // which argument is it? from 0..n-1

    /**
     * If they specified default value {@code x=y}, store the token here
     */
    public Token defaultValueToken;
    public Object defaultValue; // x="str", x=true, x=false
    public CompiledST compiledDefaultValue; // x={...}

    public FormalArgument(String name)
    {
        this.name = name;
    }

    public FormalArgument(String name, Token defaultValueToken)
    {
        this.name = name;
        this.defaultValueToken = defaultValueToken;
    }

    /*
    public static String getCardinalityName(int cardinality) {
        switch (cardinality) {
            case OPTIONAL : return "optional";
            case REQUIRED : return "exactly one";
            case ZERO_OR_MORE : return "zero-or-more";
            case ONE_OR_MORE : return "one-or-more";
            default : return "unknown";
        }
    }
    */

    @Override
    public int hashCode()
    {
        return name.hashCode() + defaultValueToken.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof FormalArgument))
        {
            return false;
        }
        FormalArgument other = (FormalArgument) o;
        if (!this.name.equals(other.name))
        {
            return false;
        }
        // only check if there is a default value; that's all
        return !(
            (this.defaultValueToken != null && other.defaultValueToken == null) ||
                (this.defaultValueToken == null && other.defaultValueToken != null));
    }

    @Override
    public String toString()
    {
        if (defaultValueToken != null)
        {
            return name + "=" + defaultValueToken.getText();
        }
        return name;
    }
}
