package ru.atlant.roleplay.data.impl;

import lombok.Getter;
import ru.atlant.roleplay.data.DataManager;
import ru.atlant.roleplay.data.type.Ability;
import ru.atlant.roleplay.data.type.Fraction;
import ru.atlant.roleplay.data.type.Job;

import java.util.*;

@Getter
public class TestDataManager implements DataManager {

    private List<Ability> abilities = new ArrayList<>();
    private List<Fraction> fractions = new ArrayList<>();
    private Map<String, String> config = new HashMap<>();

    public TestDataManager() {
        abilities.add(new Ability("arrest", "Arrest ability", Collections.singletonList("roleplay.arrest")));
        Fraction fraction = new Fraction("testfraction", "Тестовая фракция", new ArrayList<>(1));
        fraction.getJobs().add(new Job("testjob", "Тестовая работа", new ArrayList<>(Collections.singletonList(abilities.get(0).getId()))));
        fractions.add(fraction);
        config.put("time", "120");
    }

    @Override
    public Ability getAbility(String id) {
        return abilities.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void replaceAbility(String id, String description, List<String> permissions) {
        Ability ability = new Ability(id, description, permissions);
        removeAbility(id);
        abilities.add(ability);
    }

    @Override
    public void removeAbility(String id) {
        abilities.removeIf(item -> item.getId().equals(id));
    }

    @Override
    public Fraction getFraction(String id) {
        return fractions.stream().filter(fraction -> fraction.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void replaceJob(String fractionId, String jobId, String name, List<String> abilities) {
        Fraction fraction = fractions.stream().filter(frac -> frac.getId().equals(fractionId)).findFirst().orElse(null);
        Job job = new Job(jobId, name, abilities);
        removeJob(fractionId, jobId);
        fraction.getJobs().add(job);
    }

    @Override
    public void removeJob(String fractionId, String jobId) {
        fractions.stream().filter(fraction -> fraction.getId().equals(fractionId)).forEach(fraction -> {
            fraction.getJobs().removeIf(job -> job.getId().equals(jobId));
        });
    }

    @Override
    public void updateFraction(String fractionId, String fractionName) {
        Fraction fraction = fractions.stream().filter(frac -> frac.getId().equals(fractionId)).findFirst().orElse(null);
        if (fraction == null) {
            fraction = new Fraction(fractionId, fractionName, new ArrayList<>());
            fractions.add(fraction);
        } else fraction.setName(fractionName);
    }

    @Override
    public void removeFraction(String fractionId) {
        fractions.removeIf(fraction -> fraction.getId().equals(fractionId));
    }

    @Override
    public void replaceConfig(String key, String value) {
        config.put(key, value);
    }

    @Override
    public void removeConfig(String key) {
        config.remove(key);
    }

    @Override
    public String getValue(String key) {
        return config.get(key);
    }
}
