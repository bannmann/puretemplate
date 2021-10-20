package org.puretemplate;

import java.util.Locale;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.puretemplate.error.ErrorListener;

@RequiredArgsConstructor
class ContextImpl implements Context
{
    @Getter
    @NonNull
    private final ST st;

    private Locale locale = Locale.getDefault(Locale.Category.FORMAT);

    private ErrorListener errorListener;

    @Override
    public Context add(@NonNull String name, Object value)
    {
        st.add(name, value);
        return this;
    }

    @Override
    public Context remove(@NonNull String name)
    {
        st.remove(name);
        return this;
    }

    @Override
    public Context setLocale(Locale locale)
    {
        this.locale = locale;
        return this;
    }

    @Override
    public Context setErrorListener(ErrorListener errorListener)
    {
        this.errorListener = errorListener;
        return this;
    }

    @Override
    public Renderer render()
    {
        return new Renderer(new RendererAction(st, locale, errorListener));
    }

    public Object getAttribute(String name)
    {
        return st.getAttribute(name);
    }
}
