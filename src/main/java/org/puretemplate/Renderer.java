package org.puretemplate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
@SuppressWarnings("java:S1939")
public class Renderer extends Renderer0 implements IRenderer
{
    // Note: we only implement IRenderer explicitly so that Javadoc copies the api comments to our methods

    Renderer(IRendererAction action)
    {
        super(action);
    }
}
