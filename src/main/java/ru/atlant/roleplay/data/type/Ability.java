package ru.atlant.roleplay.data.type;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.atlant.roleplay.data.impl.sql.QueryResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class Ability {

    private final String id;
    private final String name;
    private final List<String> permissions;

    public static Ability fromSection(QueryResult.SQLSection section) {
        return new Ability(section.lookupValue("id"), section.lookupValue("name"), Stream.of(((String) section.lookupValue("permissions")).split(";;;")).collect(Collectors.toList()));
    }

}
