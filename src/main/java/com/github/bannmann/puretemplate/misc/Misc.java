package com.github.bannmann.puretemplate.misc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

public class Misc
{
    public static final String newline = System.getProperty("line.separator");

    /**
     * Makes it clear when a comparison is intended as reference equality.
     */
    public static boolean referenceEquals(Object x, Object y)
    {
        return x == y;
    }

    // Seriously: why isn't this built in to java?
    public static String join(Iterator<?> iter, String separator)
    {
        StringBuilder buf = new StringBuilder();
        while (iter.hasNext())
        {
            buf.append(iter.next());
            if (iter.hasNext())
            {
                buf.append(separator);
            }
        }
        return buf.toString();
    }

    //    public static String join(Object[] a, String separator, int start, int stop) {
    //        StringBuilder buf = new StringBuilder();
    //        for (int i = start; i < stop; i++) {
    //            if ( i>start ) buf.append(separator);
    //            buf.append(a[i].toString());
    //        }
    //        return buf.toString();
    //    }

    public static String strip(String s, int n)
    {
        return s.substring(n, s.length() - n);
    }

    //    public static String stripRight(String s, int n) {
    //        return s.substring(0, s.length()-n);
    //    }

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

    /**
     * Given, say, {@code file:/tmp/test.jar!/org/foo/templates/main.stg} convert to {@code
     * file:/tmp/test.jar!/org/foo/templates}
     */
    public static String stripLastPathElement(String f)
    {
        int slash = f.lastIndexOf('/');
        if (slash < 0)
        {
            return f;
        }
        return f.substring(0, slash);
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

    public static boolean urlExists(URL url)
    {
        try
        {
            /*
             * In Spring Boot context the URL can be like this:
             * jar:file:/path/to/myapp.jar!/BOOT-INF/lib/mylib.jar!/org/foo/templates/g.stg
             * This url cannot be processed using standard URLClassLoader
             */
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            try
            {
                is.close();
            }
            catch (Throwable e)
            {
                /*
                 * Closing the input stream may throw an exception. See bug below. Most probably it was the true reason
                 * for this commit:
                 * https://github.com/antlr/stringtemplate4/commit/21484ed46f1b20b2cdaec49f9d5a626fb26a493c
                 * https://bugs.openjdk.java.net/browse/JDK-8080094
                 */
            }
            return true;
        }
        catch (IOException ioe)
        {
            return false;
        }
    }

    /**
     * Given {@code index} into string {@code s}, compute the line and char position in line.
     */
    public static Coordinate getLineCharPosition(String s, int index)
    {
        int line = 1;
        int charPos = 0;
        int p = 0;
        while (p < index)
        {
            // don't care about s[index] itself; count before
            if (s.charAt(p) == '\n')
            {
                line++;
                charPos = 0;
            }
            else
            {
                charPos++;
            }
            p++;
        }

        return new Coordinate(line, charPos);
    }
}
