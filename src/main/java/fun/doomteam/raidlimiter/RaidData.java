package fun.doomteam.raidlimiter;

import fun.doomteam.raidlimiter.RaidLimiter.Mode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.LocalDateTime;

public class RaidData {
    FileConfiguration config;
    final File configFile;
    final RaidLimiter plugin;

    public RaidData(RaidLimiter plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public RaidData setNextDate(LocalDateTime time) {
        this.config.set("server", time.getYear() + "-" + time.getMonthValue() + "-" + time.getDayOfMonth()
                + "-" + time.getHour() + "-" + time.getMinute() + "-" + time.getSecond());
        return this;
    }

    public RaidData setPlayerNextDate(String player, LocalDateTime time) {
        this.config.set("players." + player + ".next-time", time.getYear() + "-" + time.getMonthValue() + "-" + time.getDayOfMonth()
                + "-" + time.getHour() + "-" + time.getMinute() + "-" + time.getSecond());
        return this;
    }

    public RaidData setPlayerCount(String player, int count) {
        this.config.set("players." + player + ".count", count);
        return this;
    }

    public int getPlayerCount(String player) {
        return this.config.getInt("players." + player + ".count", 0);
    }

    public RaidData setCount(int count) {
        this.config.set("count", count);
        return this;
    }

    public int getCount() {
        return this.config.getInt("count", 0);
    }

    public LocalDateTime getPlayerNextTime(String player) {
        String[] timeArray = this.config.getString("players." + player + ".next-time", "").split("-");
        int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;
        if (timeArray.length < 6) return null;
        try {
            year = Integer.parseInt(timeArray[0]);
            month = Integer.parseInt(timeArray[1]);
            day = Integer.parseInt(timeArray[2]);
            hour = Integer.parseInt(timeArray[3]);
            minute = Integer.parseInt(timeArray[4]);
            second = Integer.parseInt(timeArray[5]);

            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public LocalDateTime getNextTime() {
        String[] timeArray = this.config.getString("server", "").split("-");
        int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;
        if (timeArray.length < 6) return null;
        try {
            year = Integer.parseInt(timeArray[0]);
            month = Integer.parseInt(timeArray[1]);
            day = Integer.parseInt(timeArray[2]);
            hour = Integer.parseInt(timeArray[3]);
            minute = Integer.parseInt(timeArray[4]);
            second = Integer.parseInt(timeArray[5]);
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public boolean isRaidAccess(String player) {
        LocalDateTime time = plugin.mode.equals(Mode.SERVER) ? this.getNextTime() : this.getPlayerNextTime(player);
        if (time == null) return true;
        return LocalDateTime.now().isAfter(time);
    }

    public RaidData reloadConfig() {
        try {
            if (!configFile.exists()) {
                this.config = new YamlConfiguration();
                this.config.createSection("players");
                return this;
            }
            this.config = YamlConfiguration.loadConfiguration(configFile);
            // 检查配置文件是否需要更新
            ConfigurationSection players = this.config.getConfigurationSection("players");
            if (players == null) players = this.config.createSection("players");
            boolean flagUpdate = false;
            for (String key : players.getKeys(false)) {
                if (this.config.isString("players." + key)) {
                    flagUpdate = true;
                    break;
                }
            }
            if (flagUpdate) {
                plugin.getLogger().config("发现旧的配置文件格式(1.0)，正在兼容到新版插件(1.1+)");
                YamlConfiguration old = (YamlConfiguration) this.config;
                this.config = new YamlConfiguration();
                for (String key : old.getKeys(false)) {
                    if (key.equals("players")) {
                        for (String key1 : players.getKeys(false)) {
                            if (!old.isConfigurationSection("players." + key1)) {
                                this.config.set("players." + key1 + ".next-time", old.get("players." + key1));
                                continue;
                            }
                            this.config.set("players." + key1, old.getConfigurationSection("players." + key1));
                        }
                        continue;
                    }
                    this.config.set(key, old.get(key));
                }
                this.saveConfig();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return this;
    }

    public RaidData saveConfig() {
        try {
            this.config.save(configFile);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return this;
    }
}
