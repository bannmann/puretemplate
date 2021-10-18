package org.puretemplate;

import java.util.function.Supplier;

class TemplateImpl implements Template
{
    private final Supplier<ST> stSupplier;

    public TemplateImpl(Supplier<ST> stSupplier)
    {
        // We test the supplier once to ensure immediate feedback in case of invalid template names
        stSupplier.get();

        this.stSupplier = stSupplier;
    }

    @Override
    public Context createContext()
    {
        return new ContextImpl(stSupplier.get());
    }
}
