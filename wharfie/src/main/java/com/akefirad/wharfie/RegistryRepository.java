package com.akefirad.wharfie;

import com.akefirad.wharfie.utils.Asserts;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

import static com.akefirad.wharfie.utils.Asserts.assertMatch;

public class RegistryRepository {
    private static final String SEGMENT_REGEX = "[a-z0-9]+(?:[._-][a-z0-9]+)*";
    private static final String NAME_REGEX = "([a-z0-9]+(?:[._-][a-z0-9]+)*)+(/[a-z0-9]+(?:[._-][a-z0-9]+)*)*";
    private static final Pattern SEGMENT_PATTERN = Pattern.compile(SEGMENT_REGEX);
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    private final String name;

    public RegistryRepository ( String name ) {
        this(new String[]{ name });
    }

    public RegistryRepository ( String... names ) {
        Asserts.notEmpty(names, "repositories");

        String name = buildRepositoryName(names);

        Asserts.notEmpty(name, "name");
        this.name = name;
    }

    public String getName () {
        return name;
    }

    private String buildRepositoryName ( String[] names ) {
        List<String> list = new ArrayList<>(names.length);
        Arrays.stream(names)
                .filter(name -> name != null && !name.isEmpty())
                .forEach(name -> {
                    List<String> segments = Arrays.asList(StringUtils.split(name, "/"));
                    segments.forEach(segment -> assertMatch(SEGMENT_PATTERN, segment, "name"));
                    list.addAll(segments);
                });

        return StringUtils.join(list, "/");
    }
}
