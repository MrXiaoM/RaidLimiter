package fun.doomteam.raidlimiter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class RaidLimiter extends JavaPlugin{
	RaidListener listener;
	RaidData data;
	RaidPlaceholder papi;
	enum Mode{
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
	protected Mode mode;
	protected List<String> runCommands = new ArrayList<>();
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
		this.data.reloadConfig();
	}
	
	public void saveConfig() {
		this.data.saveConfig();
	}
	
	public void onDisable() {
		this.data.saveConfig();
		if(this.papi != null && this.papi.isRegistered()) {
			this.papi.unregister();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			this.saveDefaultConfig();
			this.reloadConfig();
			sender.sendMessage("§a配置文件已重载");
		}
		boolean server = mode.equals(Mode.SERVER);
		if(args.length >= (server ? 4 : 5) && args[0].equalsIgnoreCase("set")) {
			String player = server ? "" : args[1];
			int offset = server ? 0 : 1;
			int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;
			try {
				year = Integer.parseInt(args[offset + 1]);
				month = Integer.parseInt(args[offset + 2]);
				day = Integer.parseInt(args[offset + 3]);
				if(args.length > offset + 4) {
					hour = Integer.parseInt(args[offset + 4]);
					if(args.length > offset + 5) {
						minute = Integer.parseInt(args[offset + 5]);
						if(args.length > offset + 6) {
							second = Integer.parseInt(args[offset + 6]);
						}
					}
				}
			}catch(Throwable t) {
				sender.sendMessage("§c参数里存在无效的整数");
				return true;
			}
			LocalDateTime time = LocalDateTime.of(year, month, day, hour, minute, second);
			if(server) {
				data.setNextDate(time);
			}
			else {
				data.setPlayerNextDate(player, time);
			}
			sender.sendMessage("§a已设置" + (server ? "全服" : "玩家") +" §e" + player + " §a在 §e" + year + "年" + month + "月" + day + "日 " + hour + ":" + minute + ":" + second + " §a前都不可触发袭击" );
			return true;
		}
		if(args.length == (mode.equals(Mode.SERVER) ? 2 : 3) && args[0].equalsIgnoreCase("set")) {
			String player = args[1];
			int[] timeArray = getTimeFromString(args[2]);
			if(timeArray == null) {
				sender.sendMessage("§c参数里存在无效的整数");
				return true;
			}
			LocalDateTime time = data.getPlayerNextTime(player);
			if(time == null) time = LocalDateTime.now();
			int year = time.getYear();
			int month = time.getMonthValue();
			int day = time.getDayOfMonth();
			int hour = time.getHour();
			int minute = time.getMinute();
			int second = time.getSecond();
			if(timeArray[0] > 0) year = timeArray[0];
			if(timeArray[1] > 0) month = timeArray[1];
			if(timeArray[2] > 0) day = timeArray[2];
			if(timeArray[3] > 0) hour = timeArray[3];
			if(timeArray[4] > 0) minute = timeArray[4];
			if(timeArray[5] > 0) second = timeArray[5];
			LocalDateTime newTime = LocalDateTime.of(year, month, day, hour, minute, second);
			if(server) {
				data.setNextDate(newTime);
			}
			else {
				data.setPlayerNextDate(player, newTime);
			}
			sender.sendMessage("§a已设置" + (server ? "全服" : ("玩家§e " + player + " §a")) + "在 §e" + year + "年" + month + "月" + day + "日 " + hour + ":" + minute + ":" + second + " §a前都不可触发袭击" );
			return true;
		}
		if(args.length == (server ? 2 : 3) && args[0].equalsIgnoreCase("add")) {
			String player = args[1];
			int[] timeArray = getTimeFromString(args[2]);
			if(timeArray == null) {
				sender.sendMessage("§c参数里存在无效的整数");
				return true;
			}
			LocalDateTime time = data.getPlayerNextTime(player);
			if(time == null) time = LocalDateTime.now();
			time.plusSeconds(timeArray[5]);
			time.plusMinutes(timeArray[4]);
			time.plusHours(timeArray[3]);
			time.plusDays(timeArray[2]);
			time.plusMinutes(timeArray[1]);
			time.plusYears(timeArray[0]);
			if(server) {
				data.setNextDate(time);
			}
			else {
				data.setPlayerNextDate(player, time);
			}
			sender.sendMessage("§a已将" + (server ? "全服" : ("玩家§e " + player + " §a")) +"的触发袭击冷却时间增加 §e" + timeToChinese(timeArray));
			return true;
		}
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
					temp = "";
					switch(c) {
				    	case 'y': year = Integer.parseInt(temp); break;
				    	case 'M': month = Integer.parseInt(temp); break;
				    	case 'd': day = Integer.parseInt(temp); break;
				    	case 'h': hour = Integer.parseInt(temp); break;
				    	case 'm': minute = Integer.parseInt(temp); break;
				    	case 's': second = Integer.parseInt(temp); break;
				    	default: break;
					}
				}
			}
		}catch(Throwable t) {
			return null;
		}
		return new int[] { year, month, day, hour, minute, second};
	}
	
	public RaidData getData() {
		return data;
	}
}
