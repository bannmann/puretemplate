package org.puretemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.error.ErrorType;
import org.puretemplate.exception.TemplateException;
import org.puretemplate.model.Aggregate;
import org.puretemplate.model.AggregateModelAdaptor;
import org.puretemplate.model.AttributeRenderer;
import org.puretemplate.model.MapModelAdaptor;
import org.puretemplate.model.ModelAdaptor;
import org.puretemplate.model.ObjectModelAdaptor;

import com.google.common.annotations.VisibleForTesting;

/**
 * A directory or directory tree of {@code .st} template files and/or group files. Individual template files contain
 * formal template definitions. In a sense, it's like a single group file broken into multiple files, one for each
 * template. ST v3 had just the pure template inside, not the template name and header. Name inside must match filename
 * (minus suffix).
 */
@Slf4j
abstract class STGroup
{
    public static final String GROUP_FILE_EXTENSION = ".stg";
    public static final String TEMPLATE_FILE_EXTENSION = ".st";

    protected final Charset charset;

    /**
     * Every group can import templates/dictionaries from other groups. The list must be synchronized (see {@link
     * STGroup#importTemplates}).
     */
    protected final List<STGroup> imports = Collections.synchronizedList(new ArrayList<>());

    char delimiterStartChar;

    char delimiterStopChar;

    /**
     * Maps template name to {@link CompiledST} object. This map is synchronized.
     */
    protected Map<String, CompiledST> templates = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Maps dictionary names to {@link Map} objects representing the dictionaries defined by the user like {@code
     * typeInitMap ::= ["int":"0"]}.
     */
    protected Map<String, Map<String, Object>> dictionaries = Collections.synchronizedMap(new HashMap<>());

    /**
     * A dictionary that allows people to register a renderer for a particular kind of object for any template evaluated
     * relative to this group.  For example, a date should be formatted differently depending on the locale.  You can
     * set {@code Date.class} to an object whose {@code toString(Object)} method properly formats a {@link Date}
     * attribute according to locale.  Or you can have a different renderer object for each locale.
     * <p>
     * Order of addition is recorded and matters.  If more than one renderer works for an object, the first registered
     * has priority.</p>
     * <p>
     * Renderer associated with type {@code t} works for object {@code o} if</p>
     * <pre>
     *  t.isAssignableFrom(o.getClass()) // would assignment t = o work?
     *  </pre>
     * So it works if {@code o} is subclass or implements {@code t}.
     * <p>
     * This structure is synchronized.</p>
     */
    protected Map<Class<?>, AttributeRenderer<?>> renderers;

    private boolean legacyRendering;

    /**
     * A dictionary that allows people to register a model adaptor for a particular kind of object (subclass or
     * implementation). Applies for any template evaluated relative to this group.
     * <p>
     * ST initializes with model adaptors that know how to pull properties out of {@link Object}s, {@link Map}s, and
     * {@link ST}s.</p>
     * <p>
     * The last one you register gets priority; do least to most specific.</p>
     */
    protected final Map<Class<?>, ModelAdaptor<?>> adaptors;

    {
        TypeRegistry<ModelAdaptor<?>> registry = new TypeRegistry<>();
        registry.put(Object.class, new ObjectModelAdaptor<>());
        registry.put(ST.class, new STModelAdaptor());
        registry.put(ContextImpl.class, new ContextImplModelAdaptor());
        registry.put(Map.class, new MapModelAdaptor());
        registry.put(Aggregate.class, new AggregateModelAdaptor());
        adaptors = Collections.synchronizedMap(registry);
    }

    /**
     * Used to indicate that the template doesn't exist. Prevents duplicate group file loads and unnecessary file
     * checks.
     */
    protected static final CompiledST NOT_FOUND_ST = new CompiledST();

    public static final ErrorManager DEFAULT_ERR_MGR = new ErrorManager(ErrorListeners.SYSTEM_ERR);

    /**
     * For debugging with {@link STViz}. Records where in code an {@link ST} was created and where code added
     * attributes.
     */
    public static boolean trackCreationEvents;

    static STGroup defaultGroup = new LegacyBareStGroup();

    /**
     * The {@link ErrorManager} for entire group; all compilations and executions. This gets copied to parsers, walkers,
     * and interpreters.
     */
    ErrorManager errMgr = STGroup.DEFAULT_ERR_MGR;

    @VisibleForTesting
    public STGroup()
    {
        this(StandardCharsets.UTF_8, '<', '>');
    }

    STGroup(char delimiterStartChar, char delimiterStopChar)
    {
        this(StandardCharsets.UTF_8, delimiterStartChar, delimiterStopChar);
    }

    STGroup(@NonNull Charset charset, char delimiterStartChar, char delimiterStopChar)
    {
        this.delimiterStartChar = delimiterStartChar;
        this.delimiterStopChar = delimiterStopChar;
        this.charset = charset;
    }

    /**
     * The primary means of getting an instance of a template from this group. Names must be absolute, fully-qualified
     * names like {@code /a/b}.
     */
    public ST getInstanceOf(String name)
    {
        if (name == null)
        {
            return null;
        }
        log.debug("{}.getInstanceOf({})", getName(), name);
        if (name.charAt(0) != '/')
        {
            name = "/" + name;
        }
        CompiledST c = lookupTemplate(name);
        if (c != null)
        {
            ST st = new ST();
            st.impl = c;
            st.groupThatCreatedThisInstance = this;
            if (c.formalArguments != null)
            {
                st.locals = new Object[c.formalArguments.size()];
                Arrays.fill(st.locals, ST.EMPTY_ATTR);
            }
            return st;
        }
        return null;
    }

    ST obtainInstanceOf(String name)
    {
        ST st = getInstanceOf(name);
        if (st == null)
        {
            throw new IllegalArgumentException(String.format("Invalid template name %s in group %s", name, getName()));
        }
        return st;
    }

    protected ST getEmbeddedInstanceOf(InstanceScope scope, String name)
    {
        String fullyQualifiedName = name;
        if (name.charAt(0) != '/')
        {
            fullyQualifiedName = scope.st.impl.prefix + name;
        }
        log.debug("getEmbeddedInstanceOf({})", fullyQualifiedName);
        ST st = getInstanceOf(fullyQualifiedName);
        if (st == null)
        {
            errMgr.runTimeError(scope.toLocation(), ErrorType.NO_SUCH_TEMPLATE, fullyQualifiedName);
            return createStringTemplateInternally(new CompiledST());
        }
        // this is only called internally. wack any debug ST create events
        if (trackCreationEvents)
        {
            st.debugState.newSTEvent = null; // toss it out
        }
        return st;
    }

    /**
     * Create singleton template for use with dictionary values.
     */
    public ST createSingleton(Token templateToken)
    {
        String template = Misc.strip(templateToken.getText(), Parsing.getTemplateDelimiterSize(templateToken));
        CompiledST impl = compile(getFileName(), null, null, template, templateToken);
        ST st = createStringTemplateInternally(impl);
        st.groupThatCreatedThisInstance = this;
        st.impl.hasFormalArgs = false;
        st.impl.name = ST.UNKNOWN_NAME;
        st.impl.defineImplicitlyDefinedTemplates(this);
        return st;
    }

    /**
     * Is this template defined in this group or from this group below? Names must be absolute, fully-qualified names
     * like {@code /a/b}.
     */
    @VisibleForTesting
    boolean isDefined(String name)
    {
        return lookupTemplate(name) != null;
    }

    /**
     * Look up a fully-qualified name.
     */
    CompiledST lookupTemplate(String name)
    {
        if (name.charAt(0) != '/')
        {
            name = "/" + name;
        }
        log.debug("{}.lookupTemplate({})", getName(), name);
        CompiledST code = rawGetTemplate(name);
        if (code == NOT_FOUND_ST)
        {
            log.debug("{} previously seen as not found", name);
            return null;
        }
        // try to load from disk and look up again
        if (code == null)
        {
            code = load(name);
        }
        if (code == null)
        {
            code = lookupImportedTemplate(name);
        }
        if (code == null)
        {
            log.debug("{} recorded not found", name);
            templates.put(name, NOT_FOUND_ST);
        }
        if (code != null)
        {
            log.debug("{}.lookupTemplate({}) found", getName(), name);
        }
        return code;
    }

    /**
     * Load st from disk if directory or load whole group file if .stg file (then return just one template). {@code
     * name} is fully-qualified.
     */
    protected CompiledST load(String name)
    {
        return null;
    }

    /**
     * Force a load if it makes sense for the group.
     */
    void load()
    {
    }

    protected CompiledST lookupImportedTemplate(String name)
    {
        if (imports.size() == 0)
        {
            return null;
        }
        for (STGroup g : imports)
        {
            log.debug("checking {} for imported {}", g.getName(), name);
            CompiledST code = g.lookupTemplate(name);
            if (code != null)
            {
                log.debug("{}.lookupImportedTemplate({}) found", g.getName(), name);
                return code;
            }
        }
        log.debug("{} not found in {} imports", name, getName());
        return null;
    }

    public CompiledST rawGetTemplate(String name)
    {
        return templates.get(name);
    }

    public Map<String, Object> rawGetDictionary(String name)
    {
        return dictionaries.get(name);
    }

    boolean isDictionary(String name)
    {
        return dictionaries.get(name) != null;
    }

    @VisibleForTesting
    CompiledST defineTemplate(String templateName, String template)
    {
        if (templateName.charAt(0) != '/')
        {
            templateName = "/" + templateName;
        }
        try
        {
            CompiledST impl = defineTemplate(templateName,
                new CommonToken(GroupParser.ID, templateName),
                null,
                template,
                null);
            return impl;
        }
        catch (TemplateException se)
        {
            // we have reported the error; the exception just blasts us
            // out of parsing this template
        }
        return null;
    }

    @VisibleForTesting
    CompiledST defineTemplate(String name, String argsS, String template)
    {
        if (name.charAt(0) != '/')
        {
            name = "/" + name;
        }
        String[] args = argsS.split(",");
        List<FormalArgument> a = new ArrayList<>();
        for (String arg : args)
        {
            a.add(new FormalArgument(arg));
        }
        return defineTemplate(name, new CommonToken(GroupParser.ID, name), a, template, null);
    }

    @VisibleForTesting
    CompiledST defineTemplate(
        String fullyQualifiedTemplateName, Token nameT, List<FormalArgument> args, String template, Token templateToken)
    {
        log.debug("defineTemplate({})", fullyQualifiedTemplateName);
        if (fullyQualifiedTemplateName == null || fullyQualifiedTemplateName.length() == 0)
        {
            throw new IllegalArgumentException("empty template name");
        }
        if (fullyQualifiedTemplateName.indexOf('.') >= 0)
        {
            throw new IllegalArgumentException("cannot have '.' in template names");
        }
        template = Misc.trimOneStartingNewline(template);
        template = Misc.trimOneTrailingNewline(template);
        // compile, passing in templateName as enclosing name for any embedded regions
        CompiledST code = compile(getFileName(), fullyQualifiedTemplateName, args, template, templateToken);
        code.name = fullyQualifiedTemplateName;
        rawDefineTemplate(fullyQualifiedTemplateName, code, nameT);
        code.defineArgDefaultValueTemplates(this);
        code.defineImplicitlyDefinedTemplates(this); // define any anonymous subtemplates

        return code;
    }

    /**
     * Make name and alias for target.  Replace any previous definition of name.
     */
    public CompiledST defineTemplateAlias(Token aliasT, Token targetT)
    {
        String alias = aliasT.getText();
        String target = targetT.getText();
        CompiledST targetCode = rawGetTemplate("/" + target);
        if (targetCode == null)
        {
            errMgr.compileTimeError(ErrorType.ALIAS_TARGET_UNDEFINED, null, aliasT, alias, target);
            return null;
        }
        rawDefineTemplate("/" + alias, targetCode, aliasT);
        return targetCode;
    }

    private CompiledST defineRegion(String enclosingTemplateName, Token regionT, String template, Token templateToken)
    {
        String name = regionT.getText();
        template = Misc.trimOneStartingNewline(template);
        template = Misc.trimOneTrailingNewline(template);
        CompiledST code = compile(getFileName(), enclosingTemplateName, null, template, templateToken);
        String mangled = getMangledRegionName(enclosingTemplateName, name);

        if (lookupTemplate(mangled) == null)
        {
            errMgr.compileTimeError(ErrorType.NO_SUCH_REGION, templateToken, regionT, enclosingTemplateName, name);
            return new CompiledST();
        }
        code.name = mangled;
        code.isRegion = true;
        code.regionDefType = ST.RegionType.EXPLICIT;
        code.templateDefStartToken = regionT;

        rawDefineTemplate(mangled, code, regionT);
        code.defineArgDefaultValueTemplates(this);
        code.defineImplicitlyDefinedTemplates(this);

        return code;
    }

    public void defineTemplateOrRegion(
        String fullyQualifiedTemplateName,
        String regionSurroundingTemplateName,
        Token templateToken,
        String template,
        Token nameToken,
        List<FormalArgument> args)
    {
        try
        {
            if (regionSurroundingTemplateName != null)
            {
                defineRegion(regionSurroundingTemplateName, nameToken, template, templateToken);
            }
            else
            {
                defineTemplate(fullyQualifiedTemplateName, nameToken, args, template, templateToken);
            }
        }
        catch (TemplateException e)
        {
            // after getting syntax error in a template, we emit msg
            // and throw exception to blast all the way out to here.
        }
    }

    public void rawDefineTemplate(String name, CompiledST code, Token defT)
    {
        CompiledST prev = rawGetTemplate(name);
        if (prev != null)
        {
            if (!prev.isRegion)
            {
                errMgr.compileTimeError(ErrorType.TEMPLATE_REDEFINITION, null, defT);
                return;
            }
            else
            {
                if (code.regionDefType != ST.RegionType.IMPLICIT && prev.regionDefType == ST.RegionType.EMBEDDED)
                {
                    errMgr.compileTimeError(ErrorType.EMBEDDED_REGION_REDEFINITION,
                        null,
                        defT,
                        getUnMangledTemplateName(name));
                    return;
                }
                else if (code.regionDefType == ST.RegionType.IMPLICIT || prev.regionDefType == ST.RegionType.EXPLICIT)
                {
                    errMgr.compileTimeError(ErrorType.REGION_REDEFINITION, null, defT, getUnMangledTemplateName(name));
                    return;
                }
            }
        }
        code.nativeGroup = this;
        code.templateDefStartToken = defT;
        templates.put(name, code);
    }

    /**
     * Compile a template.
     */
    CompiledST compile(
        String sourceName,
        String name,
        List<FormalArgument> args,
        String template,
        Token templateToken) // for error location
    {
        Compiler c = new Compiler(this);
        return c.compile(sourceName, name, args, template, templateToken);
    }

    /**
     * The {@code "foo"} of {@code t() ::= "<@foo()>"} is mangled to {@code "/region__/t__foo"}
     */
    public static String getMangledRegionName(String enclosingTemplateName, String name)
    {
        if (enclosingTemplateName.charAt(0) != '/')
        {
            enclosingTemplateName = '/' + enclosingTemplateName;
        }
        return "/region__" + enclosingTemplateName + "__" + name;
    }

    /**
     * Return {@code "t.foo"} from {@code "/region__/t__foo"}
     */
    static String getUnMangledTemplateName(String mangledName)
    {
        String t = mangledName.substring("/region__".length(), mangledName.lastIndexOf("__"));
        String r = mangledName.substring(mangledName.lastIndexOf("__") + 2);
        return t + '.' + r;
    }

    /**
     * Define a map for this group.
     * <p>
     * Not thread safe...do not keep adding these while you reference them.</p>
     */
    public void defineDictionary(String name, Map<String, Object> mapping)
    {
        dictionaries.put(name, mapping);
    }

    /**
     * Make this group import templates/dictionaries from {@code g}.
     */
    void importTemplates(STGroup g)
    {
        if (g == null)
        {
            return;
        }
        imports.add(g);
    }

    /**
     * Import template files, directories, and group files. Priority is given to templates defined in the current group;
     * this, in effect, provides inheritance. Polymorphism is in effect so that if an inherited template references
     * template {@code t()} then we search for {@code t()} in the subgroup first.
     * <p>
     * Templates are loaded on-demand from import dirs.  Imported groups are loaded on-demand when searching for a
     * template.</p>
     * <p>
     * The listener of this group is passed to the import group so errors found while loading imported element are sent
     * to listener of this group.</p>
     * <p>
     * This method is called when processing import statements specified in group files. Use {@link
     * #importTemplates(STGroup)} to import templates 'programmatically'.</p>
     */
    public abstract void importTemplates(Token fileNameToken);

    protected void importTemplates(STGroup g, boolean clearOnUnload)
    {
        if (g == null)
        {
            return;
        }
        imports.add(g);
    }

    /**
     * Load template stream into this group.
     *
     * @param prefix path from group root to {@code unqualifiedFileName} like {@code "/subdir"} if file is in {@code
     * /subdir/a.st}.
     * @param unqualifiedFileName file name without path/prefix, but with extension, e.g. {@code "a.st"}
     */
    CompiledST loadTemplateFile(String prefix, String unqualifiedFileName, CharStream templateStream)
    {
        GroupLexer lexer = new GroupLexer(templateStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GroupParser parser = new GroupParser(tokens);
        parser.group = this;
        lexer.group = this;
        try
        {
            parser.templateDef(prefix);
        }
        catch (RecognitionException re)
        {
            errMgr.groupSyntaxError(ErrorType.SYNTAX_ERROR, unqualifiedFileName, re, re.getMessage());
        }
        String templateName = Misc.getFileNameNoSuffix(unqualifiedFileName);
        if (prefix != null && prefix.length() > 0)
        {
            templateName = prefix + templateName;
        }
        CompiledST impl = rawGetTemplate(templateName);
        impl.prefix = prefix;
        return impl;
    }

    /**
     * Add an adaptor for a kind of object so ST knows how to pull properties from them. Add adaptors in increasing
     * order of specificity. ST adds {@link Object}, {@link Map}, {@link ST}, and {@link Aggregate} model adaptors for
     * you first. Adaptors you add have priority over default adaptors.
     * <p>
     * If an adaptor for type {@code T} already exists, it is replaced by the {@code adaptor} argument.</p>
     * <p>
     * This must invalidate cache entries, so set your adaptors up before calling {@link ST#render} for efficiency.</p>
     */
    <T> void registerModelAdaptor(Class<T> attributeType, ModelAdaptor<? super T> adaptor)
    {
        if (attributeType.isPrimitive())
        {
            throw new IllegalArgumentException("can't register ModelAdaptor for primitive type " +
                attributeType.getSimpleName());
        }

        adaptors.put(attributeType, adaptor);
    }

    <T> ModelAdaptor<? super T> getModelAdaptor(Class<T> attributeType)
    {
        //noinspection unchecked
        return (ModelAdaptor<? super T>) adaptors.get(attributeType);
    }

    <T> void registerRenderer(Class<T> attributeType, AttributeRenderer<? super T> r, boolean recursive)
    {
        if (attributeType.isPrimitive())
        {
            throw new IllegalArgumentException("can't register renderer for primitive type " +
                attributeType.getSimpleName());
        }

        if (renderers == null)
        {
            renderers = Collections.synchronizedMap(new TypeRegistry<AttributeRenderer<?>>());
        }

        renderers.put(attributeType, r);

        if (recursive)
        {
            load(); // make sure imports exist (recursively)
            for (STGroup g : imports)
            {
                g.registerRenderer(attributeType, r, true);
            }
        }
    }

    /**
     * Get renderer for class {@code T} associated with this group.
     * <p>
     * For non-imported groups and object-to-render of class {@code T}, use renderer (if any) registered for {@code T}.
     * For imports, any renderer set on import group is ignored even when using an imported template. You should set the
     * renderer on the main group you use (or all to be sure).  I look at import groups as "helpers" that should give me
     * templates and nothing else. If you have multiple renderers for {@code String}, say, then just make uber combined
     * renderer with more specific format names.</p>
     */
    <T> AttributeRenderer<? super T> getAttributeRenderer(Class<T> attributeType)
    {
        if (renderers == null)
        {
            return null;
        }

        //noinspection unchecked
        return (AttributeRenderer<? super T>) renderers.get(attributeType);
    }

    /**
     * Only used for regions, map operations, and other implicit "new ST" events during rendering.
     */
    ST createStringTemplateInternally(CompiledST impl)
    {
        ST st1 = new ST();
        st1.impl = impl;
        st1.groupThatCreatedThisInstance = this;
        if (impl.formalArguments != null)
        {
            st1.locals = new Object[impl.formalArguments.size()];
            Arrays.fill(st1.locals, ST.EMPTY_ATTR);
        }
        ST st = st1;
        if (trackCreationEvents && st.debugState != null)
        {
            st.debugState.newSTEvent = null; // toss it out
        }
        return st;
    }

    ST createStringTemplateInternally(ST proto)
    {
        return new ST(proto); // no need to wack debugState; not set in ST(proto).
    }

    String getName()
    {
        return "<no name>;";
    }

    public String getFileName()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    String show()
    {
        StringBuilder buf = new StringBuilder();
        if (imports.size() != 0)
        {
            buf.append(" : " + imports);
        }
        for (String name : templates.keySet())
        {
            CompiledST c = rawGetTemplate(name);
            if (c.isAnonSubtemplate || c == NOT_FOUND_ST)
            {
                continue;
            }
            int slash = name.lastIndexOf('/');
            name = name.substring(slash + 1);
            buf.append(name);
            buf.append('(');
            if (c.formalArguments != null)
            {
                buf.append(Misc.join(c.formalArguments.values()
                    .iterator(), ","));
            }
            buf.append(')');
            buf.append(" ::= <<" + Misc.NEWLINE);
            buf.append(c.template + Misc.NEWLINE);
            buf.append(">>" + Misc.NEWLINE);
        }
        return buf.toString();
    }

    protected ErrorListener getListener()
    {
        return errMgr.listener;
    }

    @VisibleForTesting
    public void setListener(ErrorListener listener)
    {
        errMgr = new ErrorManager(listener);
    }

    @VisibleForTesting
    Set<String> getTemplateNames()
    {
        load();
        HashSet<String> result = new HashSet<>();
        for (Map.Entry<String, CompiledST> e : templates.entrySet())
        {
            if (e.getValue() != NOT_FOUND_ST)
            {
                result.add(e.getKey());
            }
        }
        return result;
    }

    void setLegacyRendering(boolean legacyRendering)
    {
        this.legacyRendering = legacyRendering;
    }

    /**
     * @param locale locale or {@code null} to use the default
     * @param listener listener or {@code null} to use the default
     */
    Interpreter createInterpreter(Locale locale, ErrorListener listener)
    {
        ErrorManager errorManager = errMgr;
        if (listener != null)
        {
            errorManager = new ErrorManager(listener);
        }
        return createInterpreterInternal(locale, errorManager, false);
    }

    Interpreter createInterpreter(ErrorManager errorManager)
    {
        return createInterpreterInternal(Locale.ROOT, errorManager, false);
    }

    Interpreter createDebuggingInterpreter()
    {
        return createInterpreterInternal(Locale.ROOT, errMgr, true);
    }

    private Interpreter createInterpreterInternal(@NonNull Locale locale, ErrorManager errorManager, boolean debug)
    {
        if (legacyRendering)
        {
            return new LegacyInterpreter(this, locale, errorManager, debug);
        }
        else
        {
            return new DefaultInterpreter(this, locale, errorManager, debug);
        }
    }
}
