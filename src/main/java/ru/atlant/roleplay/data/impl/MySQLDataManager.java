package ru.atlant.roleplay.data.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.atlant.roleplay.data.DataManager;
import ru.atlant.roleplay.data.impl.sql.factories.SyncQueryFactory;
import ru.atlant.roleplay.data.type.Ability;
import ru.atlant.roleplay.data.type.Fraction;
import ru.atlant.roleplay.data.type.Job;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MySQLDataManager implements DataManager {

    private static final String CREATE_CONFIG_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `config` (`key` varchar(50) NOT NULL, `val` varchar(100) NOT NULL, PRIMARY KEY(`key`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    private static final String CREATE_ABILITIES_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `abilities` (`id` varchar(50) NOT NULL, `name` varchar(50) NOT NULL, `permissions` TEXT NOT NULL, PRIMARY KEY(`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    private static final String CREATE_FRACTIONS_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `fractions` (`id` varchar(50) NOT NULL, `name` varchar(50) NOT NULL, PRIMARY KEY(`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    private static final String CREATE_JOBS_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `jobs` (`fraction` varchar(50) NOT NULL, `id` varchar(50) NOT NULL, `name` varchar(50) NOT NULL, `abilities` TEXT NOT NULL, PRIMARY KEY(`fraction`,`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    private SyncQueryFactory sync;

    public MySQLDataManager(String host, int port, String user, String database, String password) {
        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(5000);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s" +
                "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", host, port, database));
        config.setUsername(user);
        config.setPassword(password);
        HikariDataSource dataSource = new HikariDataSource(config);
        this.sync = new SyncQueryFactory(dataSource, Throwable::printStackTrace);

        sync.unsafeUpdate(CREATE_CONFIG_TABLE_QUERY);
        sync.unsafeUpdate(CREATE_ABILITIES_TABLE_QUERY);
        sync.unsafeUpdate(CREATE_FRACTIONS_TABLE_QUERY);
        sync.unsafeUpdate(CREATE_JOBS_TABLE_QUERY);
    }

    @Override
    public List<Fraction> getFractions() {
        List<Fraction> fractions = sync.unsafeGet("SELECT * FROM `fractions`").all().stream().map(Fraction::fromSection).collect(Collectors.toList());
        sync.unsafeGet("SELECT * FROM `jobs`").all().forEach(section -> {
            String fraction = section.lookupValue("fraction");
            fractions.stream().filter(frac -> frac.getId().equals(fraction)).findFirst().ifPresent(frac -> frac.getJobs().add(Job.fromSection(section)));
        });
        return fractions;
    }

    @Override
    public Fraction getFraction(String id) {
        return sync.prepareGet("SELECT * FROM `fractions` WHERE `id` = ?", ps -> {
            try {
                ps.setString(1, id);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).all().stream().map(Fraction::fromSection).peek(frac -> {
            frac.getJobs().addAll(sync.prepareGet("SELECT * FROM `jobs` WHERE `fraction` = ?", ps -> {
                try {
                    ps.setString(1, id);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).all().stream().map(Job::fromSection).collect(Collectors.toList()));
        }).findFirst().orElse(null);
    }

    @Override
    public void updateFraction(String fractionId, String fractionName) {
        sync.prepareUpdate("REPLACE INTO `fractions` VALUES (?,?)", ps -> {
            try {
                ps.setString(1, fractionId);
                ps.setString(2, fractionName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void removeFraction(String fractionId) {
        sync.prepareUpdate("DELETE FROM `fractions` WHERE `id` = ?", ps -> {
            try {
                ps.setString(1, fractionId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        sync.prepareUpdate("DELETE FROM `jobs` WHERE `fraction` = ?", ps -> {
            try {
                ps.setString(1, fractionId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void replaceJob(String fractionId, String jobId, String name, List<String> abilities) {
        String abil = String.join(";;;", abilities);
        sync.prepareUpdate("REPLACE INTO `jobs` VALUES (?,?,?,?);", ps -> {
            try {
                ps.setString(1, fractionId);
                ps.setString(2, jobId);
                ps.setString(3, name);
                ps.setString(4, abil);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void removeJob(String fractionId, String jobId) {
        sync.prepareUpdate("DELETE FROM `jobs` WHERE `fraction` = ? AND `id` = ?", ps -> {
            try {
                ps.setString(1, fractionId);
                ps.setString(2, jobId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public Map<String, String> getConfig() {
        return sync.unsafeGet("SELECT * FROM `config`;").all().stream().collect(Collectors.toMap(
                section -> section.lookupValue("key"),
                section -> section.lookupValue("val")
        ));
    }

    @Override
    public String getValue(String key) {
        return sync.prepareGet("SELECT `val` FROM `config` WHERE `key` = ?", ps -> {
            try {
                ps.setString(1, key);
            } catch (Exception ignored) {
            }
        }).all().stream().findFirst().map(sec -> (String) sec.lookupValue("val")).orElse(null);
    }

    @Override
    public void replaceConfig(String key, String value) {
        sync.prepareUpdate("REPLACE INTO `config` VALUES (?,?)", ps -> {
            try {
                ps.setString(1, key);
                ps.setString(2, value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void removeConfig(String key) {
        sync.prepareUpdate("DELETE FROM `config` WHERE `key` = ?", ps -> {
            try {
                ps.setString(1, key);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public List<Ability> getAbilities() {
        return sync.unsafeGet("SELECT * FROM `abilities`").all().stream().map(Ability::fromSection).collect(Collectors.toList());
    }

    @Override
    public Ability getAbility(String id) {
        return sync.prepareGet("SELECT * FROM `abilities` WHERE `id` = ?", ps -> {
            try {
                ps.setString(1, id);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).all().stream().findFirst().map(Ability::fromSection).orElse(null);
    }

    @Override
    public void replaceAbility(String id, String description, List<String> permissions) {
        String perms = String.join(";;;", permissions);
        sync.prepareUpdate("REPLACE INTO `abilities` VALUES (?,?,?)", ps -> {
            try {
                ps.setString(1, id);
                ps.setString(2, description);
                ps.setString(3, perms);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void removeAbility(String id) {
        sync.prepareUpdate("DELETE FROM `abilities` WHERE `id` = ?", ps -> {
            try {
                ps.setString(1, id);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
