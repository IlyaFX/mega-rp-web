package ru.atlant.roleplay.data.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.atlant.roleplay.data.impl.sql.QueryResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public class Board {

    private final String id;
    private final String title;
    private final List<String> lines;

    public static Board fromSection(QueryResult.SQLSection section) {
        return new Board(section.lookupValue("id"), section.lookupValue("title"), Stream.of(((String) section.lookupValue("lines")).split(";;;")).collect(Collectors.toList()));
    }

}
