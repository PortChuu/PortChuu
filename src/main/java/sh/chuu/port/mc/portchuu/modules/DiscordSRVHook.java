package sh.chuu.port.mc.portchuu.modules;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.awt.Color;


public class DiscordSRVHook {

    public void sendChat(String msg) {
        TextChannel chan = DiscordSRV.getPlugin().getMainTextChannel();
        if (chan != null)
            chan.sendMessage(msg).complete();
    }

    @Subscribe
    public void chatEvent(DiscordGuildMessagePostProcessEvent ev) {
        Member u = ev.getMember();
        Color c = u.getColor();
        String color = c == null ? "" : ChatColor.of(c).toString();
        BaseComponent user = TextTemplates.createDiscordTooltip(
                color + u.getEffectiveName(),
                u.getUser().getName() + "#" + u.getUser().getDiscriminator(),
                u.getId(),
                u.getUser().isBot());
        BaseComponent[] msg = new ComponentBuilder("|").color(ChatColor.of("#5865F2")).bold(true).append("Discord").bold(false).append(" ").reset()
                .append(user).append("\u300B ").color(ChatColor.GRAY).append(ev.getMessage().getContentRaw()).reset().create();

        Bukkit.getConsoleSender().sendMessage(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            //noinspection deprecation
            p.sendMessage(ChatMessageType.CHAT, msg);
        }
        ev.setCancelled(true);
    }
}
