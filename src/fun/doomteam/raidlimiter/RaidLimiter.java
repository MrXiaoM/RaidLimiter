package fun.doomteam.raidlimiter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

public class RaidLimiter extends JavaPlugin{
	public static enum Mode{
		PLAYER, SERVER;
		
		public static Mode getMode(String value) {
			return getMode(value, null);
		}
		public static Mode getMode(String value, Mode nullValue) {
			for(Mode mode : Mode.values()) {
				if(mode.name().toUpperCase().equals(value.toUpperCase())) return mode;
			}
			return nullValue;
		}
	}
	
	RaidListener listener;
	RaidData data;
	RaidPlaceholder papi;
	Mode mode;
	protected List<String> runCommands = new ArrayList<>();
	protected List<String> cooldownCommands = new ArrayList<>();
	public void onEnable() {
		this.saveDefaultConfig();
		this.reloadConfig();
		if(Util.init()) {
			(papi = new RaidPlaceholder(this)).register();
		}
		this.listener = new RaidListener(this);
	}
	
	public void reloadConfig() {
		super.reloadConfig();
		this.mode = Mode.getMode(this.getConfig().getString("mode", "SERVER"), Mode.SERVER);
		this.runCommands.clear();
		if(this.getConfig().contains("commands")) {
			this.runCommands = this.getConfig().getStringList("commands");
		}
		this.cooldownCommands.clear();
		if(this.getConfig().contains("cooldown-commands")) {
			this.cooldownCommands = this.getConfig().getStringList("cooldown-commands");
		}
		if(this.data == null) this.data = new RaidData(this);
		this.data.reloadConfig();
	}
	
	public void saveConfig() {
		this.data.saveConfig();
		if(papi != null && papi.isRegistered()) papi.unregister();
	}
	
	public void onDisable() {
		this.data.saveConfig();
		if(this.papi != null && this.papi.isRegistered()) {
			this.papi.unregister();
		}
	}
	
	public String lang(String key) {
		return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message." + key, "翻译错误: message." + key).replace("\\n", "\n").replace("\\r", "\r"));
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			this.saveDefaultConfig();
			this.reloadConfig();
			sender.sendMessage(lang("reload"));
			return true;
		}
		boolean server = mode.equals(Mode.SERVER);
		if(args.length == (server ? 2 : 3) && args[0].equalsIgnoreCase("set")) {
			boolean now = (server ? args[1] : args[2]).equalsIgnoreCase("now");
			int[] timeArray = now ? null : getTimeFromString(server ? args[1] : args[2]);
			if(!now && timeArray == null) {
				sender.sendMessage(lang("not-integer"));
				return true;
			}
			LocalDateTime time = now ? LocalDateTime.now() : (server ? data.getNextTime() : data.getPlayerNextTime(args[1]));
			if(time == null) time = LocalDateTime.now();
			int year = time.getYear();
			int month = time.getMonthValue();
			int day = time.getDayOfMonth();
			int hour = time.getHour();
			int minute = time.getMinute();
			int second = time.getSecond();
			if(!now) {
				if(timeArray[0] > 0) year = timeArray[0];
				if(timeArray[1] > 0) month = timeArray[1];
				if(timeArray[2] > 0) day = timeArray[2];
				if(timeArray[3] > 0) hour = timeArray[3];
				if(timeArray[4] > 0) minute = timeArray[4];
				if(timeArray[5] > 0) second = timeArray[5];
			}
			LocalDateTime newTime = now ? time : LocalDateTime.of(year, month, day, hour, minute, second);
			if(server) {
				data.setNextDate(newTime).saveConfig();
			}
			else {
				data.setPlayerNextDate(args[1], newTime).saveConfig();
			}

			String type = server ? lang("type-server") : lang("type-player").replace("%player%", args[1]);
			String timeStr = year + "年" + month + "月" + day + "日 " + hour + ":" + minute + ":" + second;
			
			sender.sendMessage(lang("set").replace("%type%", type).replace("%time%", timeStr));
			return true;
		}
		if(args.length == (server ? 2 : 3) && args[0].equalsIgnoreCase("plus")) {
			int[] timeArray = getTimeFromString(server ? args[1] : args[2]);
			if(timeArray == null) {
				sender.sendMessage(lang("not-integer"));
				return true;
			}
			LocalDateTime time = server ? data.getNextTime() : data.getPlayerNextTime(args[1]);
			if(time == null) time = LocalDateTime.now();
			time = time.plusSeconds(timeArray[5])
					.plusMinutes(timeArray[4])
					.plusHours(timeArray[3])
					.plusDays(timeArray[2])
					.plusMinutes(timeArray[1])
					.plusYears(timeArray[0]);
			if(server) {
				data.setNextDate(time).saveConfig();
			}
			else {
				data.setPlayerNextDate(args[1], time).saveConfig();
			}

			String type = server ? lang("type-server") : lang("type-player").replace("%player%", args[1]);
			sender.sendMessage(lang("plus").replace("%type%", type).replace("%time%", timeToChinese(timeArray)));
			return true;
		}
		if(args.length == (server ? 2 : 3) && args[0].equalsIgnoreCase("minus")) {
			int[] timeArray = getTimeFromString(server ? args[1] : args[2]);
			if(timeArray == null) {
				sender.sendMessage(lang("not-integer"));
				return true;
			}
			LocalDateTime time = server ? data.getNextTime() : data.getPlayerNextTime(args[1]);
			if(time == null) time = LocalDateTime.now();
			time = time.minusSeconds(timeArray[5])
					.minusMinutes(timeArray[4])
					.minusHours(timeArray[3])
					.minusDays(timeArray[2])
					.minusMinutes(timeArray[1])
					.minusYears(timeArray[0]);
			if(server) {
				data.setNextDate(time).saveConfig();
			}
			else {
				data.setPlayerNextDate(args[1], time).saveConfig();
			}


			String type = server ? lang("type-server") : lang("type-player").replace("%player%", args[1]);
			sender.sendMessage(lang("minus").replace("%type%", type).replace("%time%", timeToChinese(timeArray)));
			return true;
		}
		if(args.length > 1 && args[0].equalsIgnoreCase("cmd") && (sender instanceof Player)) {
			String command = args[1];
			for(int i = 2; i < args.length; i++) {
				command += " " + args[i];
			}
			Util.runCommands(Lists.newArrayList(command), (Player) sender);
			return true;
		}
		sender.sendMessage(lang("help-mode-" + mode.name().toLowerCase()));
		return true;
	}
	
	public static String timeToChinese(int[] time) {
		if(time.length != 6) return "[length != 6]";
		return (time[0] > 0 ? (time[0] + "年") : "") +
				(time[1] > 0 ? (time[1] + "月") : "") +
				(time[2] > 0 ? (time[2] + "日") : "") +
				(time[3] > 0 ? (time[3] + "时") : "") +
				(time[4] > 0 ? (time[4] + "分") : "") +
				(time[5] > 0 ? (time[5] + "秒") : "");
	}
	
	public static int[] getTimeFromString(String timeStr) {
		int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;
		try {
			String temp = "";
			for(char c : timeStr.toCharArray()) {
				if(Character.isDigit(c)) {
					temp += String.valueOf(c);
				}
				else {
					switch(c) {
				    	case 'y': year = Integer.parseInt(temp); break;
				    	case 'M': month = Integer.parseInt(temp); break;
				    	case 'd': day = Integer.parseInt(temp); break;
				    	case 'h': hour = Integer.parseInt(temp); break;
				    	case 'm': minute = Integer.parseInt(temp); break;
				    	case 's': second = Integer.parseInt(temp); break;
				    	default: break;
					}
					temp = "";
				}
			}
		}catch(Throwable t) {
			return null;
		}
		return new int[] { year, month, day, hour, minute, second};
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public RaidData getData() {
		return data;
	}
}
