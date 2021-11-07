package org.puretemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lombok.NonNull;

import org.puretemplate.error.ErrorListener;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

/**
 * An instance of the StringTemplate. It consists primarily of a {@linkplain ST#impl reference} to its implementation
 * (shared among all instances) and a hash table of {@linkplain ST#locals attributes}.  Because of dynamic scoping, we
 * also need a reference to any enclosing instance. For example, in a deeply nested template for an HTML page body, we
 * could still reference the title attribute defined in the outermost page template.
 * <p>
 * To use templates, you create one (usually via {@link STGroup}) and then inject attributes using {@link #add}. To
 * render its attacks, use {@link ST#render()}.</p>
 * <p>
 * TODO: {@link ST#locals} is not actually a hash table like the documentation says.</p>
 */
class ST
{
    /**
     * {@code <@r()>}, {@code <@r>...<@end>}, and {@code @t.r() ::= "..."} defined manually by coder
     */
    enum RegionType
    {
        /**
         * {@code <@r()>}
         */
        IMPLICIT,
        /**
         * {@code <@r>...<@end>}
         */
        EMBEDDED,
        /**
         * {@code @t.r() ::= "..."}
         */
        EXPLICIT
    }

    /**
     * Events during template hierarchy construction (not evaluation)
     */
    static final class DebugState
    {
        /**
         * Record who made us? {@link ConstructionEvent} creates {@link Exception} to grab stack
         */
        public ConstructionEvent newSTEvent;

        /**
         * Track construction-time add attribute "events"; used for ST user-level debugging
         */
        public Multimap<String, AddAttributeEvent> addAttrEvents = MultimapBuilder.linkedHashKeys()
            .arrayListValues()
            .build();
    }

    /**
     * Just an alias for {@link ArrayList}, but this way I can track whether a list is something ST created or it's an
     * incoming list.
     */
    static final class AttributeList extends ArrayList<Object>
    {
        public AttributeList(int size)
        {
            super(size);
        }

        public AttributeList()
        {
            super();
        }
    }

    static final String UNKNOWN_NAME = "anonymous";

    static final Object EMPTY_ATTR = new Object();

    /**
     * When there are no formal args for template t and you map t across some values, t implicitly gets arg "it".  E.g.,
     * "<b>$it$</b>"
     */
    static final String IMPLICIT_ARG_NAME = "it";

    private static boolean isArray(Object object)
    {
        return object != null &&
            object.getClass()
                .isArray();
    }

    /**
     * The implementation for this template among all instances of same template .
     */
    CompiledST impl;

    /**
     * Safe to simultaneously write via {@link #add}, which is synchronized. Reading during exec is, however, NOT
     * synchronized.  So, not thread safe to add attributes while it is being evaluated.  Initialized to {@link
     * #EMPTY_ATTR} to distinguish {@code null} from empty.
     */
    Object[] locals;

    /**
     * Created as instance of which group? We need this to initialize interpreter via render.  So, we create st and then
     * it needs to know which group created it for sake of polymorphism:
     *
     * <pre>
     *  st = skin1.getInstanceOf("searchbox");
     *  result = st.render(); // knows skin1 created it
     *  </pre>
     *
     * Say we have a group {@code g1} with template {@code t} that imports templates {@code t} and {@code u} from
     * another group {@code g2}. {@code g1.getInstanceOf("u")} finds {@code u} in {@code g2} but remembers that {@code
     * g1} created it.  If {@code u} includes {@code t}, it should create {@code g1.t} not {@code g2.t}.
     *
     * <pre>
     *   g1 = {t(), u()}
     *   |
     *   v
     *   g2 = {t()}
     *  </pre>
     */
    STGroup groupThatCreatedThisInstance;

    /**
     * If {@link STGroup#trackCreationEvents}, track creation and add attribute events for each object. Create this
     * object on first use.
     */
    DebugState debugState;

    /**
     * Used by group creation routine, not by users
     */
    ST()
    {
        if (STGroup.trackCreationEvents)
        {
            if (debugState == null)
            {
                debugState = new ST.DebugState();
            }
            debugState.newSTEvent = new ConstructionEvent();
        }
    }

    /**
     * Used to make templates inline in code for simple things like SQL or log records. No formal arguments are set and
     * there is no enclosing instance.
     */
    @Deprecated(forRemoval = true)
    ST(String template)
    {
        this(STGroup.defaultGroup, template);
    }

    /**
     * Create ST using non-default delimiters; each one of these will live in it's own group since you're overriding a
     * default; don't want to alter {@link STGroup#defaultGroup}.
     */
    ST(String template, char delimiterStartChar, char delimiterStopChar)
    {
        this(new LegacyBareStGroup(delimiterStartChar, delimiterStopChar), template);
    }

    ST(STGroup group, String template)
    {
        this();
        groupThatCreatedThisInstance = group;
        impl = groupThatCreatedThisInstance.compile(group.getFileName(), null, null, template, null);
        impl.hasFormalArgs = false;
        impl.name = UNKNOWN_NAME;
        impl.defineImplicitlyDefinedTemplates(groupThatCreatedThisInstance);
    }

    /**
     * Clone a prototype template. Copy all fields minus {@link #debugState}; don't delegate to {@link #ST()}, which
     * creates {@link ConstructionEvent}.
     */
    ST(ST proto)
    {
        try
        {
            // Because add() can fake a formal arg def, make sure to clone impl
            // entire impl so formalArguments list is cloned as well. Don't want
            // further derivations altering previous arg defs. See
            // testRedefOfKeyInCloneAfterAddingAttribute().
            this.impl = proto.impl.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
        if (proto.locals != null)
        {
            this.locals = new Object[proto.locals.length];
            System.arraycopy(proto.locals, 0, this.locals, 0, proto.locals.length);
        }
        else if (impl.formalArguments != null && !impl.formalArguments.isEmpty())
        {
            this.locals = new Object[impl.formalArguments.size()];
            Arrays.fill(this.locals, EMPTY_ATTR);
        }
        this.groupThatCreatedThisInstance = proto.groupThatCreatedThisInstance;
    }

    public synchronized ST add(@NonNull String name, Object value)
    {
        verifyAttributeValue(value);

        if (name.indexOf('.') >= 0)
        {
            throw new IllegalArgumentException("cannot have '.' in attribute names");
        }

        if (STGroup.trackCreationEvents)
        {
            if (debugState == null)
            {
                debugState = new ST.DebugState();
            }
            debugState.addAttrEvents.put(name, new AddAttributeEvent(name, value));
        }

        FormalArgument arg = obtainArgument(name);

        Object curvalue = locals[arg.index];
        if (curvalue == EMPTY_ATTR)
        {
            // new attribute
            locals[arg.index] = value;
            return this;
        }

        // attribute will be multi-valued for sure now
        // convert current attribute to list if not already
        // copy-on-write semantics; copy a list injected by user to add new value
        AttributeList multi = convertToAttributeList(curvalue);
        locals[arg.index] = multi; // replace with list

        // efficiently extract multiple values (objects or primitives); if it's only a single value we get a size 1 list
        AttributeList valuesToAdd = convertToAttributeList(value);

        for (Object object : valuesToAdd)
        {
            verifyAttributeValue(object);
            multi.add(object);
        }

        return this;
    }

    private void verifyAttributeValue(Object value)
    {
        if (value instanceof ST || value instanceof Template)
        {
            throw new IllegalArgumentException("Cannot add template " + value + " as an attribute");
        }
    }

    private FormalArgument obtainArgument(String name)
    {
        FormalArgument arg = null;
        if (impl.hasFormalArgs)
        {
            if (impl.formalArguments != null)
            {
                arg = impl.formalArguments.get(name);
            }
            if (arg == null)
            {
                throw new IllegalArgumentException("no such attribute: " + name);
            }
        }
        else
        {
            // define and make room in locals (a hack to make new ST("simple template") work.)
            if (impl.formalArguments != null)
            {
                arg = impl.formalArguments.get(name);
            }
            if (arg == null)
            {
                // not defined
                arg = new FormalArgument(name);
                impl.addArg(arg);
                if (locals == null)
                {
                    locals = new Object[1];
                }
                else
                {
                    Object[] copy = new Object[impl.formalArguments.size()];
                    System.arraycopy(locals, 0, copy, 0, Math.min(locals.length, impl.formalArguments.size()));
                    locals = copy;
                }
                locals[arg.index] = EMPTY_ATTR;
            }
        }
        return arg;
    }

    /**
     * Remove an attribute value entirely (can't remove attribute definitions).
     */
    public void remove(String name)
    {
        if (impl.formalArguments == null)
        {
            if (impl.hasFormalArgs)
            {
                throw new IllegalArgumentException("no such attribute: " + name);
            }
            return;
        }
        FormalArgument arg = impl.formalArguments.get(name);
        if (arg == null)
        {
            throw new IllegalArgumentException("no such attribute: " + name);
        }
        locals[arg.index] = EMPTY_ATTR; // reset value
    }

    /**
     * Set {@code locals} attribute value when you only know the name, not the index. This is ultimately invoked by
     * calling {@code ST#add} from outside so toss an exception to notify them.
     */
    protected void rawSetAttribute(String name, Object value)
    {
        if (impl.formalArguments == null)
        {
            throw new IllegalArgumentException("no such attribute: " + name);
        }
        FormalArgument arg = impl.formalArguments.get(name);
        if (arg == null)
        {
            throw new IllegalArgumentException("no such attribute: " + name);
        }
        locals[arg.index] = value;
    }

    /**
     * Find an attribute in this template only.
     */
    public Object getAttribute(String name)
    {
        FormalArgument localArg = null;
        if (impl.formalArguments != null)
        {
            localArg = impl.formalArguments.get(name);
        }
        if (localArg != null)
        {
            Object o = locals[localArg.index];
            if (o == ST.EMPTY_ATTR)
            {
                o = null;
            }
            return o;
        }
        return null;
    }

    // FIXME don't mention "curvalue" in var name or comments since we use method for both cur & new now
    protected static AttributeList convertToAttributeList(Object curvalue)
    {
        AttributeList multi;
        if (curvalue instanceof AttributeList)
        {
            // already a list made by ST
            multi = (AttributeList) curvalue;
        }
        else if (curvalue instanceof List)
        {
            // existing attribute is non-ST List
            // must copy to an ST-managed list before adding new attribute
            // (can't alter incoming attributes)
            List<?> listAttr = (List<?>) curvalue;
            multi = new AttributeList(listAttr.size());
            multi.addAll(listAttr);
        }
        else if (curvalue instanceof Object[])
        {
            // copy object array to list (we deal with primitive arrays below)
            Object[] a = (Object[]) curvalue;
            multi = new AttributeList(a.length);
            multi.addAll(Arrays.asList(a)); // asList doesn't copy as far as I can tell
        }
        else if (isArray(curvalue))
        {
            // copy primitive array to list
            int length = Array.getLength(curvalue);
            multi = new AttributeList(length);
            for (int i = 0; i < length; i++)
            {
                multi.add(Array.get(curvalue, i));
            }
        }
        else
        {
            // curvalue nonlist and we want to add an attribute
            // must convert curvalue existing to list
            multi = new AttributeList(); // make list to hold multiple values
            multi.add(curvalue);                 // add previous single-valued attribute
        }
        return multi;
    }

    public String getName()
    {
        return impl.name;
    }

    public boolean isAnonSubtemplate()
    {
        return impl.isAnonSubtemplate;
    }

    @Deprecated(forRemoval = true)
    public int write(STWriter out) throws IOException
    {
        Interpreter interp = groupThatCreatedThisInstance.createInterpreter(Locale.getDefault(),
            impl.nativeGroup.errMgr);
        return interp.exec(this, out);
    }

    @Deprecated(forRemoval = true)
    public int write(STWriter out, Locale locale)
    {
        Interpreter interp = groupThatCreatedThisInstance.createInterpreter(locale, impl.nativeGroup.errMgr);
        return interp.exec(this, out);
    }

    public int write(STWriter out, Locale locale, ErrorListener listener)
    {
        Interpreter interp = groupThatCreatedThisInstance.createInterpreter(locale, listener);
        return interp.exec(this, out);
    }

    public String render()
    {
        return render(Locale.getDefault());
    }

    public String render(int lineWidth)
    {
        return render(Locale.getDefault(), lineWidth);
    }

    public String render(Locale locale)
    {
        return render(locale, STWriter.NO_WRAP);
    }

    public String render(Locale locale, int lineWidth)
    {
        StringWriter out = new StringWriter();
        STWriter wr = new AutoIndentWriter(out);
        wr.setLineWidth(lineWidth);
        write(wr, locale);
        return out.toString();
    }

    // TESTING SUPPORT

    public List<InterpEvent> getEvents()
    {
        return getEvents(Locale.getDefault());
    }

    public List<InterpEvent> getEvents(int lineWidth)
    {
        return getEvents(Locale.getDefault(), lineWidth);
    }

    public List<InterpEvent> getEvents(Locale locale)
    {
        return getEvents(locale, STWriter.NO_WRAP);
    }

    public List<InterpEvent> getEvents(Locale locale, int lineWidth)
    {
        StringWriter out = new StringWriter();
        STWriter wr = new AutoIndentWriter(out);
        wr.setLineWidth(lineWidth);
        Interpreter interp = groupThatCreatedThisInstance.createDebuggingInterpreter(locale);
        interp.exec(this, wr); // render and track events
        return interp.getEvents();
    }

    @Override
    public String toString()
    {
        if (impl == null)
        {
            return "bad-template()";
        }
        String name = impl.name + "()";
        if (this.impl.isRegion)
        {
            name = "@" + STGroup.getUnMangledTemplateName(name);
        }

        return name;
    }

    public String getSourceText()
    {
        return impl.template;
    }

    public Interval getInterval(int ip)
    {
        return impl.sourceMap[ip];
    }

    /**
     * <pre>
     * ST.format("&lt;%1&gt;:&lt;%2&gt;", n, p);
     * </pre>
     */
    public static String format(String template, Object... attributes)
    {
        return format(STWriter.NO_WRAP, template, attributes);
    }

    public static String format(int lineWidth, String template, Object... attributes)
    {
        template = template.replaceAll("%([0-9]+)", "arg$1");
        ST st = new ST(template);
        int i = 1;
        for (Object a : attributes)
        {
            st.add("arg" + i, a);
            i++;
        }
        return st.render(lineWidth);
    }
}
