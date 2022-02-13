package sh.chuu.port.mc.portchuu;

import com.google.common.collect.ImmutableList;
import github.scarsz.discordsrv.DiscordSRV;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import sh.chuu.port.mc.portchuu.commands.*;
import sh.chuu.port.mc.portchuu.modules.*;

import java.util.List;

public class PortChuu extends JavaPlugin {
    private static PortChuu instance = null;
    private PermissionsModule permissionsModule = null;
    private LuckPerms lpAPI = null;
    private DiscordSRVHook discordSRVHook = null;

    public static PortChuu getInstance() {
        return PortChuu.instance;
    }

    public PermissionsModule getPermissionsModule() {
        return permissionsModule;
    }
    public DiscordSRVHook getDiscordSRVHook() {
        return discordSRVHook;
    }

    public LuckPerms getLpAPI() {
        if (lpAPI != null)
            return lpAPI;

        try {
            Class.forName("net.luckperms.api.LuckPerms");
            RegisteredServiceProvider<LuckPerms> lpProvider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (lpProvider != null) this.lpAPI = lpProvider.getProvider();
        } catch (ClassNotFoundException e) {
            return null;
        }

        return lpAPI;
    }

    public boolean isDiscordSRVLoaded() {
        Plugin pl = getServer().getPluginManager().getPlugin("DiscordSRV");
        return pl != null && pl.isEnabled();
    }

    @Override
    public void onEnable() {
        PortChuu.instance = this;
        saveDefaultConfig();

        String chatFormat = getConfig().getBoolean("chat.reformat") ? getConfig().getString("chat.format") : null;

        if (isDiscordSRVLoaded())
            DiscordSRV.api.subscribe(discordSRVHook = new DiscordSRVHook());
        else
            getLogger().warning("DiscordSRV not enabled!");

        permissionsModule = new PermissionsModule(this);

        getServer().getPluginManager().registerEvents(new ListenerBuildPermission(), this);
        getServer().getPluginManager().registerEvents(new ListenerJoinLeaveMod(), this);
        if (getConfig().getBoolean("chat-tooltip.enable"))
            getServer().getPluginManager().registerEvents(new ChatTooltip(), this);

        PluginCommand cmdFirstSeen = getCommand("firstseen");
        cmdFirstSeen.setExecutor(new CmdFirstSeen());
        PluginCommand cmdGamemode = getCommand("gamemode");
        cmdGamemode.setExecutor(new CmdGamemode());
        PluginCommand cmdGraylist = getCommand("greylist");
        cmdGraylist.setExecutor(new CmdGreylist());
        PluginCommand cmdInfo = getCommand("info");
        cmdInfo.setExecutor(new CmdInfo());
        PluginCommand cmdKill = getCommand("kill");
        cmdKill.setExecutor(new CmdKill());
        PluginCommand cmdPing = getCommand("ping");
        cmdPing.setExecutor(new CmdPing());
        PluginCommand cmdReport = getCommand("report");
        cmdReport.setExecutor(new CmdReport());
        PluginCommand cmdSeen = getCommand("seen");
        cmdSeen.setExecutor(new CmdSeen());
        PluginCommand cmdTeleport = getCommand("teleport");
        cmdTeleport.setExecutor(new CmdTeleport());
        PluginCommand cmdTps = getCommand("tps");
        cmdTps.setExecutor(new CmdTps());

        new TabListStatusModule();

        getConfig();
        reloadConfig();
        saveConfig();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        if (isDiscordSRVLoaded())
            DiscordSRV.api.unsubscribe(discordSRVHook);
        PortChuu.instance = null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        TextComponent msg = new TextComponent("Port Chuu Server Plugin v");
        msg.addExtra(getDescription().getVersion());
        msg.addExtra("\nCoded with <3");
        msg.setColor(ChatColor.GRAY);
        sender.spigot().sendMessage(msg);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return ImmutableList.of();
    }

    public boolean sendToDiscord(String msg) {
        if (isDiscordSRVLoaded()) {
            discordSRVHook.sendChat(msg);
            return true;
        }
        return false;
    }

}
