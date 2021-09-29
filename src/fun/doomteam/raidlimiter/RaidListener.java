package fun.doomteam.raidlimiter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidTriggerEvent;

public class RaidListener implements Listener{
	RaidLimiter plugin;
	public RaidListener(RaidLimiter plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onRaidTrigger(RaidTriggerEvent event) {
		Player player = event.getPlayer();
		if(!plugin.getData().isRaidAccess(player.getName())) {
			event.setCancelled(true);
			return;
		}
		Util.runCommands(plugin.runCommands, player);
	}
}
