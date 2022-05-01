package fun.doomteam.raidlimiter;

import fun.doomteam.raidlimiter.RaidLimiter.Mode;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;

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
        if (identifier.equalsIgnoreCase("can_raid")) {
            return this.plugin.getData().isRaidAccess(player.getName()) ? "yes" : "no";
        }
        if (identifier.equalsIgnoreCase("remaining")) {
            return Util.between(LocalDateTime.now(), plugin.getMode().equals(Mode.SERVER) ? this.plugin.getData().getNextTime() : this.plugin.getData().getPlayerNextTime(player.getName()), true);
        }
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
