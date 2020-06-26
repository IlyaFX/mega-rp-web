package ru.atlant.roleplay.data.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.atlant.roleplay.data.impl.sql.QueryResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public class Job {

    private final String id;
    private final String name;
    private final List<String> abilities;

    public static Job fromSection(QueryResult.SQLSection section) {
        return new Job(section.lookupValue("id"), section.lookupValue("name"), Stream.of(((String) section.lookupValue("abilities")).split(";;;")).collect(Collectors.toList()));
    }

}
