package fun.doomteam.raidlimiter;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;

public class RaidPlaceholder extends PlaceholderExpansion {
	private final RaidLimiter plugin;

	public RaidPlaceholder(RaidLimiter plugin) {
		this.plugin = plugin;
	}

	public String getAuthor() {
		return "mrxiaom";
	}

	public String getIdentifier() {
		return "raidlimiter";
	}

	public String onRequest(OfflinePlayer player, String identifier) {
		// TODO
		return identifier;
	}

	public String getVersion() {
		return this.plugin.getDescription().getVersion();
	}

	public boolean persist() {
		return true;
	}

	public boolean canRegister() {
		return true;
	}
}
