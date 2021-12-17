package org.puretemplate;

import org.antlr.runtime.Token;

/**
 * Represents the name of a formal argument defined in a template:
 * <pre>
 *  test(a,b,x=defaultvalue) ::= "&lt;a&gt; &lt;n&gt; &lt;x&gt;"
 * </pre> Each template has a set of these formal arguments or sets
 * {@link CompiledST#hasFormalArgs} to {@code false} (indicating that no arguments were specified such as when we create
 * a template with {@code new ST(...)}).
 */
final class FormalArgument
{
    String name;

    /**
     * which argument is it? from 0..n-1
     */
    int index;

    /**
     * If they specified default value {@code x=y}, store the token here
     */
    Token defaultValueToken;

    /**
     * {@code x="str", x=true, x=false}
     */
    Object defaultValue;

    /**
     * {@code x={...}}
     */
    CompiledST compiledDefaultValue;

    public FormalArgument(String name)
    {
        this.name = name;
    }

    public FormalArgument(String name, Token defaultValueToken)
    {
        this.name = name;
        this.defaultValueToken = defaultValueToken;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() + defaultValueToken.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof FormalArgument))
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
            this.defaultValueToken != null && other.defaultValueToken == null ||
                this.defaultValueToken == null && other.defaultValueToken != null);
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
