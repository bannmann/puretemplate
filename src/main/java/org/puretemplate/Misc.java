package org.puretemplate;

import java.io.File;
import java.nio.file.Path;

import lombok.experimental.UtilityClass;

@UtilityClass
class Misc
{
    public static final String NEWLINE = System.getProperty("line.separator");

    /**
     * Makes it clear when a comparison is intended as reference equality.
     */
    public static boolean referenceEquals(Object x, Object y)
    {
        return x == y;
    }

    /**
     * Strips the given number of characters from both the start and end of a string.
     *
     * @param s the input string; may not be {@code null}
     * @param n the number of characters
     *
     * @return the result
     *
     * @throws NullPointerException if {@code s} is null
     */
    public static String strip(String s, int n)
    {
        return s.substring(n, s.length() - n);
    }

    /**
     * Strip a single newline character from the front of {@code s}.
     */
    public static String trimOneStartingNewline(String s)
    {
        if (s.startsWith("\r\n"))
        {
            s = s.substring(2);
        }
        else if (s.startsWith("\n"))
        {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * Strip a single newline character from the end of {@code s}.
     */
    public static String trimOneTrailingNewline(String s)
    {
        if (s.endsWith("\r\n"))
        {
            s = s.substring(0, s.length() - 2);
        }
        else if (s.endsWith("\n"))
        {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String getUnqualifiedName(Path filePath)
    {
        return filePath.getFileName()
            .toString();
    }

    public static String getUnqualifiedName(String name)
    {
        int slash = name.lastIndexOf('/');
        if (slash < 0)
        {
            return name;
        }
        return name.substring(slash + 1);
    }

    public static String getFileNameNoSuffix(String f)
    {
        if (f == null)
        {
            return null;
        }
        f = getFileName(f);
        return f.substring(0, f.lastIndexOf('.'));
    }

    public static String getFileName(String fullFileName)
    {
        if (fullFileName == null)
        {
            return null;
        }
        File f = new File(fullFileName); // strip to simple name
        return f.getName();
    }

    public static String getParent(String name)
    {
        if (name == null)
        {
            return null;
        }
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash > 0)
        {
            return name.substring(0, lastSlash);
        }
        if (lastSlash == 0)
        {
            return "/";
        }
        return "";
    }

    public static String getPrefix(String name)
    {
        if (name == null)
        {
            return "/";
        }
        String parent = getParent(name);
        String prefix = parent;
        if (!parent.endsWith("/"))
        {
            prefix += '/';
        }
        return prefix;
    }

    public static String replaceEscapes(String s)
    {
        s = s.replaceAll("\n", "\\\\n");
        s = s.replaceAll("\r", "\\\\r");
        s = s.replaceAll("\t", "\\\\t");
        return s;
    }

    /**
     * Replace &gt;\&gt; with &gt;&gt; in s.
     * <p>
     * Replace \&gt; with &gt; in s, unless prefix of \&gt;&gt;&gt;.
     * <p>
     * Do NOT replace if it's &lt;\\&gt;
     */
    public static String replaceEscapedRightAngle(String s)
    {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        while (i < s.length())
        {
            char c = s.charAt(i);
            if (c == '<' && s.startsWith("<\\\\>", i))
            {
                buf.append("<\\\\>");
                i += "<\\\\>".length();
                continue;
            }
            if (c == '>' && s.startsWith(">\\>", i))
            {
                buf.append(">>");
                i += ">\\>".length();
                continue;
            }
            if (c == '\\' && s.startsWith("\\>", i) && !s.startsWith("\\>>>", i))
            {
                buf.append(">");
                i += "\\>".length();
                continue;
            }
            buf.append(c);
            i++;
        }
        return buf.toString();
    }
}
