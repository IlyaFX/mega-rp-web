package ru.atlant.roleplay.data.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.atlant.roleplay.data.impl.sql.QueryResult;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class Fraction {

    private final String id;
    @Setter
    private String name;
    private final List<Job> jobs;

    public static Fraction fromSection(QueryResult.SQLSection section) {
        return new Fraction(section.lookupValue("id"), section.lookupValue("name"), new ArrayList<>());
    }

}
