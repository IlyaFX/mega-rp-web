package ru.atlant.roleplay;

import io.javalin.Javalin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.atlant.roleplay.data.DataManager;
import ru.atlant.roleplay.data.impl.MySQLDataManager;
import ru.atlant.roleplay.data.impl.TestDataManager;
import ru.atlant.roleplay.data.type.Ability;
import ru.atlant.roleplay.data.type.Fraction;
import ru.atlant.roleplay.data.type.Job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebService {

    public static void main(String[] args) {
        DataManager dataManager;
        if (System.getenv("TEST") != null)
            dataManager = new TestDataManager();
        else {
            String[] dataInfo = new String[]{
                    System.getenv("DB_HOST"),
                    System.getenv("DB_PORT"),
                    System.getenv("DB_USER"),
                    System.getenv("DB_NAME"),
                    System.getenv("DB_PASS")
            };
            for (String s : dataInfo) {
                if (s == null) {
                    throw new IllegalArgumentException("One of DB env is not setted! DB_HOST, DB_PORT, DB_USER, DB_NAME, DB_PASS");
                }
            }
            dataManager = new MySQLDataManager(dataInfo[0], Integer.parseInt(dataInfo[1]), dataInfo[2], dataInfo[3], dataInfo[4]);
        }
        Map<String, String> header = new HashMap<>(3);
        header.put("Config", "/config");
        header.put("Abilities", "/abilities");
        header.put("Fractions", "/fractions");
        Javalin
                .create()
                .get("/abilities", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("header", header);
                    model.put("abilities", dataManager.getAbilities());
                    ctx.render("/abilities.html", model);
                })
                .get("/fractions", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("header", header);
                    model.put("fractions", dataManager.getFractions());
                    ctx.render("/fractions.html", model);
                })
                .get("/replacefraction", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    String fracId = ctx.queryParam("id");
                    if (fracId != null) {
                        model.put("id", fracId);
                        model.put("name", dataManager.getFraction(fracId).getName());
                    }
                    ctx.render("/replacefrac.html", model);
                })
                .post("/replacefracimpl", ctx -> {
                    dataManager.updateFraction(ctx.formParam("id"), ctx.formParam("name"));
                    ctx.redirect("/fractions");
                })
                .get("/deletefrac", ctx -> {
                    dataManager.removeFraction(ctx.queryParam("id"));
                    ctx.redirect("/fractions");
                })
                .get("/replacejob", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    Map<Ability, Boolean> abilityMap = new HashMap<>();
                    List<Ability> abilities = dataManager.getAbilities();
                    abilities.forEach(ability -> abilityMap.put(ability, false));
                    String fracId = ctx.queryParam("frac");
                    Fraction fraction = dataManager.getFraction(fracId);
                    if (fraction != null) {
                        model.put("fraction", fracId);
                        String jobId = ctx.queryParam("id");
                        Job job = fraction.getJobs().stream().filter(j -> j.getId().equals(jobId)).findFirst().orElse(null);
                        if (job != null) {
                            job.getAbilities().forEach(ability -> {
                                abilities.stream().filter(ab -> ab.getId().equals(ability)).findFirst().ifPresent(ab -> abilityMap.put(ab, true));
                            });
                            model.put("job", job.getId());
                            model.put("name", job.getName());
                        }
                    }
                    model.put("abilities", abilityMap);
                    ctx.render("/replacejob.html", model);
                })
                .get("/deletejob", ctx -> {
                    String fracId = ctx.queryParam("frac");
                    String jobId = ctx.queryParam("id");
                    if (fracId != null && jobId != null)
                        dataManager.removeJob(fracId, jobId);
                    ctx.redirect("/fractions");
                })
                .post("/replacejobimpl", ctx -> {
                    String fracId = ctx.formParam("fraction");
                    String jobId = ctx.formParam("id");
                    if (fracId != null && jobId != null) {
                        String name = ctx.formParam("name");
                        List<String> abilities = dataManager.getAbilities().stream().filter(ability -> ctx.formParamMap().containsKey(ability.getId())).map(Ability::getId).collect(Collectors.toList());
                        dataManager.replaceJob(fracId, jobId, name, abilities);
                    }
                    ctx.redirect("/fractions");
                })
                .get("/replaceability", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    String id = ctx.queryParam("id");
                    Ability ability = dataManager.getAbility(id);
                    model.put("id", id);
                    model.put("name", (ability == null ? null : ability.getName()));
                    model.put("perms", (ability == null ? null : ability.getPermissions()));
                    ctx.render("/replaceability.html", model);
                })
                .post("replaceabilityimpl", ctx -> {
                    String id = ctx.formParam("id");
                    String name = ctx.formParam("name");
                    List<String> formPerms = ctx.formParamMap().entrySet().stream().filter(entry -> entry.getKey().startsWith("perm")).map(entry -> entry.getValue().get(0)).filter(str -> str != null && !str.isEmpty()).collect(Collectors.toList());
                    dataManager.replaceAbility(id, name, new ArrayList<>(formPerms));
                    ctx.redirect("/abilities");
                })
                .get("deleteability", ctx -> {
                    dataManager.removeAbility(ctx.queryParam("id"));
                    ctx.redirect("/abilities");
                })
                .get("/config", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("header", header);
                    model.put("config", dataManager.getConfig());
                    ctx.render("/config.html", model);
                })
                .get("/replaceconfig", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    String key = ctx.queryParam("key");
                    model.put("key", key);
                    if (key != null)
                        model.put("value", dataManager.getValue(key));
                    ctx.render("/replaceconfig.html", model);
                })
                .get("/deleteconfig", ctx -> {
                    dataManager.removeConfig(ctx.queryParam("key"));
                    ctx.redirect("/config");
                })
                .post("/replaceconfigimpl", ctx -> {
                    String key = ctx.formParam("key");
                    if (key != null) {
                        dataManager.replaceConfig(key, ctx.formParam("value"));
                    }
                    ctx.redirect("/config");
                })
                .get("/", ctx -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("header", header);
                    ctx.render("/index.html", model);
                })
                .get("/fetchdata", ctx -> {
                    ctx.json(compileData(dataManager));
                })
                .start(Integer.parseInt(System.getenv("PORT")));
    }

    private static RolePlayData compileData(DataManager dataManager) {
        List<RolePlayData.AbilityData> abilityData = dataManager.getAbilities().stream().map(ability -> new RolePlayData.AbilityData(ability.getId(), ability.getName(), ability.getPermissions())).collect(Collectors.toList());
        List<RolePlayData.FractionData> fractionData = dataManager.getFractions().stream().map(fraction ->
                new RolePlayData.FractionData(fraction.getId(), fraction.getName(), fraction.getJobs().stream().map(job -> new RolePlayData.JobData(job.getId(), job.getName(), job.getAbilities())).collect(Collectors.toList()))).collect(Collectors.toList());
        return new RolePlayData(abilityData, fractionData, dataManager.getConfig());
    }

    @AllArgsConstructor
    @Getter
    public static class RolePlayData {
        private List<AbilityData> abilities;
        private List<FractionData> fractionData;
        private Map<String, String> config;

        @AllArgsConstructor
        @Getter
        public static class AbilityData {

            private String id, name;
            private List<String> permissions;

        }

        @AllArgsConstructor
        @Getter
        public static class FractionData {

            private String id, name;
            private List<JobData> jobs;

        }

        @AllArgsConstructor
        @Getter
        public static class JobData {

            private String id, name;
            private List<String> abilities;

        }
    }

}
