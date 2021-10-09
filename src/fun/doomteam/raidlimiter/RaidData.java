package fun.doomteam.raidlimiter;

import java.io.File;
import java.time.LocalDateTime;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fun.doomteam.raidlimiter.RaidLimiter.Mode;

public class RaidData {
	File configFile;
	FileConfiguration config;
	RaidLimiter plugin;
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
		this.config.set("players." + player, time.getYear() + "-" + time.getMonthValue() + "-" + time.getDayOfMonth() 
			+ "-" + time.getHour() + "-" + time.getMinute() + "-" + time.getSecond());
		return this;
	}
	
	public LocalDateTime getPlayerNextTime(String player) {
		String[] timeArray = this.config.getString("players." + player, "").split("-");
		int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;
		if(timeArray.length < 6) return null;
		try {
			year = Integer.parseInt(timeArray[0]);
			month = Integer.parseInt(timeArray[1]);
			day = Integer.parseInt(timeArray[2]);
			hour = Integer.parseInt(timeArray[3]);
			minute = Integer.parseInt(timeArray[4]);
			second = Integer.parseInt(timeArray[5]);
			
			return LocalDateTime.of(year, month, day, hour, minute, second);
		}catch(Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public LocalDateTime getNextTime() {
		String[] timeArray = this.config.getString("server", "").split("-");
		int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;
		if(timeArray.length < 6) return null;
		try {
			year = Integer.parseInt(timeArray[0]);
			month = Integer.parseInt(timeArray[1]);
			day = Integer.parseInt(timeArray[2]);
			hour = Integer.parseInt(timeArray[3]);
			minute = Integer.parseInt(timeArray[4]);
			second = Integer.parseInt(timeArray[5]);
			return LocalDateTime.of(year, month, day, hour, minute, second);
		}catch(Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
	
	public boolean isRaidAccess(String player) {
		LocalDateTime time = plugin.mode.equals(Mode.SERVER) ? this.getNextTime() : this.getPlayerNextTime(player);
		if(time == null) return true;
		return LocalDateTime.now().isAfter(time);
	}
	
	public RaidData reloadConfig() {
		try {
			if(!configFile.exists()) {
				this.config = new YamlConfiguration();
				return this;
			}
			this.config = YamlConfiguration.loadConfiguration(configFile);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
	
	public RaidData saveConfig() {
		try{
			this.config.save(configFile);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
}
