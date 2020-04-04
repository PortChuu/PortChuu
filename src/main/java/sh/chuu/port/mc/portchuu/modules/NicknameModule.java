package sh.chuu.port.mc.portchuu.modules;

import com.google.common.base.Charsets;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.PortChuu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Heccin I gotta put this into another heccin plugin because this itself is very long and other server
 * admins might find a good use for this >w> <w< uwu
 */
public class NicknameModule {
    private PortChuu plugin;
    private File configFile;
    private YamlConfiguration config;
    private Map<UUID, String> nickCache = new HashMap<>(); // Only loads the nickname of the player currently online

    public NicknameModule(PortChuu plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "nicknames.yml");
        if (!configFile.exists()) {
            plugin.saveResource("nicknames.yml", false);
        }

        reload();

        for (Player p : Bukkit.getOnlinePlayers()) initPlayerNick(p);
    }

    public void onDisable() {
        saveConfig();
    }

    public void reload() {
        reloadConfig();
        for (String uuid : config.getKeys(false)) {
            try {
                nickCache.put(UUID.fromString(uuid), config.getString(uuid));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("");
            }
        }
    }

    public String getNick(UUID uid) {
        String ret = nickCache.get(uid);
        if (ret != null) return ret;

        return config.getString(uid.toString());
    }

    public void applyNick(Player p, String nick) {
        p.setDisplayName(nick);

        StringBuilder pln = new StringBuilder();
        LuckPerms lp = plugin.getLpAPI();
        if (lp != null) {
            CachedMetaData md = lp.getUserManager().getUser(p.getUniqueId()).getCachedData().getMetaData(QueryOptions.defaultContextualOptions());
            String pre = md.getPrefix();
            String suf = md.getSuffix();
            if (pre != null)
                pln.append(ChatColor.translateAlternateColorCodes('&', pre));
            pln.append(nick);
            if (suf != null)
                pln.append(ChatColor.translateAlternateColorCodes('&', suf));
        } else {
            pln.append(nick);
        }

        p.setPlayerListName(pln.toString());
    }

    public String setNick(OfflinePlayer p, String nick) {
        if (p.isOnline()) {
            applyNick((Player) p, nick);
        }
        return setNick(p.getUniqueId(), nick);
    }

    public String setNick(UUID uid, String nick) {
        String oldnick;
        if (nickCache.containsKey(uid)) {
            if (nick == null)
                oldnick = nickCache.remove(uid);
            else
                oldnick = nickCache.put(uid, nick);
        } else {
            oldnick = config.getString(uid.toString());
        }
        config.set(uid.toString(), nick);
        return oldnick;
    }

    public void initPlayerNick(Player p) {
        UUID uid = p.getUniqueId();
        String nick = config.getString(uid.toString());
        if (nick == null) return;

        nickCache.put(uid, nick);
        applyNick(p, nick);
    }

    public void unloadPlayerNick(Player p) {
        nickCache.remove(p.getUniqueId());
    }

    // The rest of the snippets are kidna copy-paste from JavaPlugin.java
    private void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        final InputStream defConfigStream = plugin.getResource("nicknames.yml");
        if (defConfigStream == null) {
            return;
        }

        config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save nicknames data to " + configFile, ex);
        }
    }
}
