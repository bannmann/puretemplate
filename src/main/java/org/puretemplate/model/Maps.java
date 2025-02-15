package org.puretemplate.model;

import lombok.experimental.UtilityClass;

import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
@UtilityClass
public class Maps
{
    /**
     * When we use key as a value in a dictionary, this is how we signify.
     */
    public final String DICT_KEY = "key";

    public final String DEFAULT_KEY = "default";
}
