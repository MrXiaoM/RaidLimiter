package fun.doomteam.raidlimiter;

import fun.doomteam.raidlimiter.RaidLimiter.Mode;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {
    public static final String nms;
    public static final int secondVersion;
    public static final int thirdVersion;

    static {
        nms = Bukkit.getServer().getClass().getPackage().getName().substring(23);
        int first_ = nms.indexOf("_") + 1;
        int second_ = nms.indexOf("_", first_);
        secondVersion = Integer.parseInt(nms.substring(first_, second_));
        thirdVersion = Integer.parseInt(nms.substring(second_ + 2));
    }

    private static boolean isUsePlaceholderAPI = false;

    public static boolean init() {
        return isUsePlaceholderAPI = (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
    }

    public static void sendActionMsg_1_17(Player player, String msg) {
        try {
            Class<?> classPacket = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutChat");
            Class<?> classChatText = Class.forName("net.minecraft.network.chat.ChatComponentText");
            Class<?> classIChatBase = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
            Class<?> classChatMessageType = Class.forName("net.minecraft.network.chat.ChatMessageType");
            Constructor<?> constChatText = classChatText.getDeclaredConstructor(String.class);
            Constructor<?> constPacket = classPacket.getDeclaredConstructor(classIChatBase, classChatMessageType, UUID.class);
            Object type = classChatMessageType.getEnumConstants()[0];
            Object text = constChatText.newInstance(ChatColor.translateAlternateColorCodes('&', msg));
            Object packet = constPacket.newInstance(text, type, player.getUniqueId());
            sendPacket_1_17(player, packet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void sendActionMsg(Player player, String msg) {
        if (secondVersion >= 17) {
            sendActionMsg_1_17(player, msg);
            return;
        }
        try {
            Class<?> classPacket = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutChat");
            Class<?> classChatText = Class.forName("net.minecraft.server." + nms + ".ChatComponentText");
            Class<?> classIChatBase = Class.forName("net.minecraft.server." + nms + ".IChatBaseComponent");
            Constructor<?> constChatText = classChatText.getDeclaredConstructor(String.class);
            Constructor<?> constPacket = classPacket.getDeclaredConstructor(classIChatBase, byte.class);
            Object text = constChatText.newInstance(ChatColor.translateAlternateColorCodes('&', msg));
            Object packet = constPacket.newInstance(text, (byte) 2);
            sendPacket(player, packet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void sendPacket(Player player, Object packet) {
        if (secondVersion >= 17) {
            sendPacket_1_17(player, packet);
            return;
        }
        try {
            Class<?> classCraftPlayer = Class.forName("org.bukkit.craftbukkit." + nms + ".entity.CraftPlayer");
            Class<?> classPlayer = Class.forName("net.minecraft.server." + nms + ".EntityPlayer");
            Class<?> classConnection = Class.forName("net.minecraft.server." + nms + ".PlayerConnection");
            Class<?> classPacket = Class.forName("net.minecraft.server." + nms + ".Packet");
            Method getNMSPlayer = classCraftPlayer.getDeclaredMethod("getHandle");
            Object nmsPlayer = getNMSPlayer.invoke(player);
            Field fieldConnection = classPlayer.getDeclaredField("playerConnection");
            Object conn = fieldConnection.get(nmsPlayer);
            Method sendPacket = classConnection.getDeclaredMethod("sendPacket", classPacket);
            sendPacket.invoke(conn, packet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void sendPacket_1_17(Player player, Object packet) {
        try {
            Class<?> classCraftPlayer = Class.forName("org.bukkit.craftbukkit." + nms + ".entity.CraftPlayer");
            Class<?> classPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");
            Class<?> classConnection = Class.forName("net.minecraft.server.network.PlayerConnection");
            Class<?> classPacket = Class.forName("net.minecraft.network.protocol.Packet");
            Method getNMSPlayer = classCraftPlayer.getDeclaredMethod("getHandle");
            Object nmsPlayer = getNMSPlayer.invoke(player);
            Field fieldConnection = null;
            for (Field f : classPlayer.getDeclaredFields()) {
                if (f.getType().equals(classConnection)) {
                    fieldConnection = f;
                    break;
                }
            }
            Object conn = fieldConnection.get(nmsPlayer);
            Method sendPacket = classConnection.getDeclaredMethod("sendPacket", classPacket);
            sendPacket.invoke(conn, packet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void sendTitle(Player player, int in, int time, int out, String msg) {
        sendTitle(player, "TITLE", in, time, out, msg);
    }

    public static void sendTitle(Player player, String type, int in, int time, int out, String msg) {
        if (secondVersion >= 17) {
            sendTitle_1_17(player, type, in, time, out, msg);
            return;
        }
        try {
            Class<?> classPacket = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutTitle");
            Class<?> classPacketAction = classPacket.getDeclaredClasses()[0];
            Class<?> classIChatBase = Class.forName("net.minecraft.server." + nms + ".IChatBaseComponent");
            Class<?> classChatText = Class.forName("net.minecraft.server." + nms + ".ChatComponentText");
            Constructor<?> constChatText = classChatText.getDeclaredConstructor(String.class);
            Constructor<?> constPacket = classPacket.getDeclaredConstructor(classPacketAction, classIChatBase,
                    int.class, int.class, int.class);
            Object text = constChatText.newInstance(ChatColor.translateAlternateColorCodes('&', msg));
            Method methodValues = classPacketAction.getDeclaredMethod("valueOf", String.class);
            Object value = methodValues.invoke(null, type.toUpperCase());
            Object packet = constPacket.newInstance(value, text, in, time, out);
            sendPacket(player, packet);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void sendTitle_1_17(Player player, String type, int in, int time, int out, String msg) {
        player.sendMessage("The 'title' prefix is not support in 1.17 now. Please use '/title' command instead.");
    }

    private static List<String> handlePlaceholder(Player player, List<String> str) {
        List<String> list = new ArrayList<>();
        for (String s : str) {
            list.add(handlePlaceholder(player, s));
        }
        return list;
    }

    private static String handlePlaceholder(Player player, String str) {
        if (!isUsePlaceholderAPI)
            return ChatColor.translateAlternateColorCodes('&', str.replace("%player%", player.getName()));
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    public static boolean runCommands(List<String> commands, Player player) {
        boolean flag = true;
        if (player == null || commands == null || commands.isEmpty())
            return false;
        List<String> replacedCommands = handlePlaceholder(player, commands);
        for (int i = 0; i < replacedCommands.size(); i++) {
            String cmd = replacedCommands.get(i);
            if (cmd.startsWith("break when count smaller than:")) {
                int target = Util.strToInt(cmd.substring(30), -1);
                boolean server = RaidLimiter.getInstance().getMode().equals(Mode.SERVER);
                if (target < (server ? RaidLimiter.getInstance().getData().getCount()
                        : RaidLimiter.getInstance().getData().getPlayerCount(player.getName()))) {
                    flag = false;
                    break;
                }
            }
            if (cmd.startsWith("console:")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(8));
                continue;
            }
            if (cmd.startsWith("message-all:")) {
                Bukkit.broadcastMessage(cmd.substring(12));
                continue;
            }
            if (player.isOnline()) {
                if (cmd.startsWith("player:")) {
                    Bukkit.dispatchCommand(player, cmd.substring(7));
                    continue;
                }
                if (cmd.startsWith("sound:")) {
                    String s = cmd.substring(6).toUpperCase();
                    String[] a = s.contains(",") ? s.split(",") : new String[]{s};
                    float volume = strToFloat(a.length > 1 ? a[1] : "1.0", 1.0F);
                    float pitch = strToFloat(a.length > 2 ? a[2] : "1.0", 1.0F);
                    player.playSound(player.getLocation(), Sound.valueOf(a[0]), volume, pitch);
                    continue;
                }
                if (cmd.startsWith("message:")) {
                    player.sendMessage(cmd.substring(8));
                    continue;
                }
                if (cmd.startsWith("action:")) {
                    sendActionMsg(player, cmd.substring(7));
                    continue;
                }
                if (cmd.startsWith("title:")) {
                    sendTitle(player, "TITLE", 10, 40, 10, cmd.substring(6));
                    continue;
                }
                if (cmd.startsWith("subtitle:")) {
                    if (i == 0 || !replacedCommands.get(i - 1).startsWith("title:"))
                        sendTitle(player, "TITLE", 10, 40, 10, "");
                    sendTitle(player, "SUBTITLE", 10, 40, 10, cmd.substring(9));
                }
            }
        }
        return flag;
    }

    public static Integer strToInt(String str, Integer nullValue) {
        try {
            return Integer.valueOf(str);
        } catch (Throwable t) {
            return nullValue;
        }
    }

    public static Float strToFloat(String str, Float nullValue) {
        try {
            return Float.valueOf(str);
        } catch (Throwable t) {
            return nullValue;
        }
    }

    /**
     * 计算两个时间距离多久并转成玩家能看懂的中文
     * 两个时间不需要按先后顺序传入参数
     * <p>
     * 计算部分代码来自 DoomsdaySociety 的闭源插件 DoomsdayEssentials 中的 TimeUtil
     *
     * @param one 第一个时间
     * @param two 第二个时间
     * @return 时间文本
     * @author MrXiaoM
     */
    public static String between(LocalDateTime one, LocalDateTime two, boolean ignoreZero) {
        if (one == null || two == null || one.isEqual(two)) return "0秒";
        LocalDateTime timeBefore = one.isBefore(two) ? one : two;
        LocalDateTime timeAfter = one.isAfter(two) ? one : two;

        LocalDateTime between = LocalDateTime.from(timeBefore);
        long years = between.until(timeAfter, ChronoUnit.YEARS);
        between = between.plusYears(years);
        long months = between.until(timeAfter, ChronoUnit.MONTHS);
        between = between.plusMonths(months);
        long days = between.until(timeAfter, ChronoUnit.DAYS);
        between = between.plusDays(days);
        long hours = between.until(timeAfter, ChronoUnit.HOURS);
        between = between.plusHours(hours);
        long minutes = between.until(timeAfter, ChronoUnit.MINUTES);
        between = between.plusMinutes(minutes);
        long seconds = between.until(timeAfter, ChronoUnit.SECONDS);
        String time = seconds + "秒";
        if (!ignoreZero || minutes != 0) time = minutes + "分" + time;
        if (!ignoreZero || hours != 0) time = hours + "时" + time;
        if (!ignoreZero || days != 0) time = days + "天" + time;
        if (!ignoreZero || months != 0) time = months + "月" + time;
        if (!ignoreZero || years != 0) time = years + "年" + time;
        return time;
    }
}
