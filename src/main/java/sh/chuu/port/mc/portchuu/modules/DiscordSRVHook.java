package sh.chuu.port.mc.portchuu.modules;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreBroadcastEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextComponent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.event.ClickEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.event.HoverEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.format.TextColor;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.audience.MessageType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class DiscordSRVHook {

    public void sendChat(String msg) {
        TextChannel chan = DiscordSRV.getPlugin().getMainTextChannel();
        if (chan != null)
            chan.sendMessage(msg).complete();
    }

    public String getMemberName(long id) {
        Member u = DiscordSRV.getPlugin().getMainGuild().getMemberById(id);
        if (u == null) return null;
        return u.getEffectiveName();
    }

    @Subscribe
    public void chatProcessEvent(DiscordGuildMessagePostProcessEvent ev) {
        Member u = ev.getMember();
        Color uColor = u.getColor();
        //BaseComponent[] msg = new ComponentBuilder("|").color(ChatColor.of("#5865F2")).bold(true).append("Discord").bold(false).append(" ").reset()
        //        .append(user).append("\u300B ").color(ChatColor.GRAY).append(ev.getMessage().getContentRaw()).reset().create();

        String senderType = u.getUser().isBot() ? "Bot" : "Member";

        // 0: nothing; 1: |, 2: Discord; 3: Discord Username; 4: >> ; 5: Message (extras);
        List<Component> msgList = new ArrayList<>(ev.getMinecraftMessage().children());
        Component username = (uColor == null ? msgList.get(3) : msgList.get(3).color(TextColor.color(uColor.getRGB())))
                .hoverEvent(HoverEvent.showText(Component
                        .text(u.getUser().getName() + "#" + u.getUser().getDiscriminator() + "\n")
                        .append(Component.translatable("gui.entity_tooltip.type", Component.text(senderType)))
                        .append( Component.text("\n" + u.getId()))
                ))
                .clickEvent(ClickEvent.suggestCommand("<@" + u.getId() + ">"));
        msgList.set(3, username);

        TextComponent newMessage = Component.empty().children(msgList);
        ev.setMinecraftMessage(newMessage);
    }

    @Subscribe (priority = ListenerPriority.HIGHEST)
    public void chatBroadcastEvent(DiscordGuildMessagePreBroadcastEvent ev) {
        net.kyori.adventure.text.Component send = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(GsonComponentSerializer.gson().serialize(ev.getMessage()));

        ev.getRecipients().forEach((rec) -> {
            rec.sendMessage(send, MessageType.CHAT);
        });
        ev.getRecipients().clear();
    }
}
