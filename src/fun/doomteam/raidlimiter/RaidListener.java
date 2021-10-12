package fun.doomteam.raidlimiter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerLoadEvent;

public class RaidListener implements Listener{
	RaidLimiter plugin;
	public RaidListener(RaidLimiter plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onServerLoad(ServerLoadEvent event) {
		if(plugin.getDescription().getVersion().toLowerCase().contains("beta")) {
			plugin.getLogger().warning("========================================================");
			plugin.getLogger().warning("当前插件版本是测试版，如果发现bug，请到本插件发布贴反馈");
			plugin.getLogger().warning("发布贴地址: https://www.mcbbs.net/thread-1267918-1-1.html");
			plugin.getLogger().warning("如果你是强迫症不想看到这条测试版提示，请确保插件没有被加载的情况下(比如服务器关闭)使用压缩软件打开本插件，编辑 plugin.yml 去掉 version 中的“beta”，保存即可");
			plugin.getLogger().warning("========================================================");
		}
	}
	
	@EventHandler
	public void onRaidTrigger(RaidTriggerEvent event) {
		Player player = event.getPlayer();
		if(!plugin.getData().isRaidAccess(player.getName())) {
			event.setCancelled(true);
			Util.runCommands(plugin.cooldownCommands, player);
			return;
		}
		Util.runCommands(plugin.runCommands, player);
	}
}
