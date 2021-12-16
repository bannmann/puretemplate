package org.puretemplate.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import lombok.NonNull;

import com.google.common.collect.Streams;

class AggregateBuilderAction implements IAggregateBuilderAction
{
    private String[] names;

    private void setProperties(String... names)
    {
        this.names = names;
    }

    @Override
    public void properties(@NonNull String nameA, @NonNull String nameB)
    {
        setProperties(nameA, nameB);
    }

    @Override
    public void properties(@NonNull String nameA, @NonNull String nameB, @NonNull String nameC)
    {
        setProperties(nameA, nameB, nameC);
    }

    @Override
    public void properties(@NonNull String nameA, @NonNull String nameB, @NonNull String nameC, @NonNull String nameD)
    {
        setProperties(nameA, nameB, nameC, nameD);
    }

    @Override
    public void properties(
        @NonNull String nameA,
        @NonNull String nameB,
        @NonNull String nameC,
        @NonNull String nameD,
        @NonNull String nameE)
    {
        setProperties(nameA, nameB, nameC, nameD, nameE);
    }

    @Override
    public void properties(
        @NonNull String nameA,
        @NonNull String nameB,
        @NonNull String nameC,
        @NonNull String nameD,
        @NonNull String nameE,
        @NonNull String nameF)
    {
        setProperties(nameA, nameB, nameC, nameD, nameE, nameF);
    }

    @Override
    public void properties(
        @NonNull String nameA,
        @NonNull String nameB,
        @NonNull String nameC,
        @NonNull String nameD,
        @NonNull String nameE,
        @NonNull String nameF,
        @NonNull String nameG)
    {
        setProperties(nameA, nameB, nameC, nameD, nameE, nameF, nameG);
    }

    @Override
    public void properties(
        @NonNull String nameA,
        @NonNull String nameB,
        @NonNull String nameC,
        @NonNull String nameD,
        @NonNull String nameE,
        @NonNull String nameF,
        @NonNull String nameG,
        @NonNull String nameH)
    {
        setProperties(nameA, nameB, nameC, nameD, nameE, nameF, nameG, nameH);
    }

    private Aggregate build(Object... values)
    {
        Aggregate result = new Aggregate();
        zipConsume(Arrays.stream(names), Arrays.stream(values), result::put);
        return result;
    }

    private void zipConsume(Stream<String> streamA, Stream<Object> streamB, BiConsumer<String, Object> consumer)
    {
        Streams.zip(streamA, streamB, Map::entry)
            .forEach(entry -> {
                String a = entry.getKey();
                Object b = entry.getValue();
                consumer.accept(a, b);
            });
    }

    @Override
    public Aggregate withValues(Object valueA, Object valueB)
    {
        return build(valueA, valueB);
    }

    @Override
    public Aggregate withValues(Object valueA, Object valueB, Object valueC)
    {
        return build(valueA, valueB, valueC);
    }

    @Override
    public Aggregate withValues(Object valueA, Object valueB, Object valueC, Object valueD)
    {
        return build(valueA, valueB, valueC, valueD);
    }

    @Override
    public Aggregate withValues(Object valueA, Object valueB, Object valueC, Object valueD, Object valueE)
    {
        return build(valueA, valueB, valueC, valueD, valueE);
    }

    @Override
    public Aggregate withValues(
        Object valueA, Object valueB, Object valueC, Object valueD, Object valueE, Object valueF)
    {
        return build(valueA, valueB, valueC, valueD, valueE, valueF);
    }

    @Override
    public Aggregate withValues(
        Object valueA, Object valueB, Object valueC, Object valueD, Object valueE, Object valueF, Object valueG)
    {
        return build(valueA, valueB, valueC, valueD, valueE, valueF, valueG);
    }

    @Override
    public Aggregate withValues(
        Object valueA,
        Object valueB,
        Object valueC,
        Object valueD,
        Object valueE,
        Object valueF,
        Object valueG,
        Object valueH)
    {
        return build(valueA, valueB, valueC, valueD, valueE, valueF, valueG, valueH);
    }
}
