package org.puretemplate.fs;

import java.util.List;

import lombok.experimental.UtilityClass;

import com.google.common.collect.ImmutableList;

@UtilityClass
class NameLists
{
    public static boolean startsWith(List<String> list, List<String> other)
    {
        return list.size() >= other.size() &&
            other.subList(0, other.size())
                .equals(other);
    }

    public static ImmutableList<String> concat(List<String> list, List<String> other)
    {
        return ImmutableList.<String>builder()
            .addAll(list)
            .addAll(other)
            .build();
    }
}
