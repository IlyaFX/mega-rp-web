package ru.atlant.roleplay.data;

import ru.atlant.roleplay.data.type.Ability;
import ru.atlant.roleplay.data.type.Board;
import ru.atlant.roleplay.data.type.Fraction;

import java.util.List;
import java.util.Map;

public interface DataManager {

    List<Ability> getAbilities();

    Ability getAbility(String id);

    void replaceAbility(String id, String description, List<String> permissions);

    void removeAbility(String id);

    List<Fraction> getFractions();

    Fraction getFraction(String id);

    void replaceJob(String fractionId, String jobId, String name, List<String> abilities);

    void removeJob(String fractionId, String jobId);

    void updateFraction(String fractionId, String fractionName);

    void removeFraction(String fractionId);

    Map<String, String> getConfig();

    String getValue(String key);

    void replaceConfig(String key, String value);

    void removeConfig(String key);

    List<Board> getBoards();

    Board getBoard(String id);

    void replaceBoard(String id, String title, List<String> lines);

    void removeBoard(String id);

}
