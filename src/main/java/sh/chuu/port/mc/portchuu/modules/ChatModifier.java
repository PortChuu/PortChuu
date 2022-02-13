package sh.chuu.port.mc.portchuu.modules;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import sh.chuu.port.mc.portchuu.PortChuu;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class ChatModifier implements Listener {
    static class ChatStyle implements ChatRenderer {
        static final Pattern URL_PATTERN = Pattern.compile(
                //  schema (http/s)                  ipv4            OR        namespace                 port     path         ends
                //   |-----------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
                "((?:https?:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
        Pattern.CASE_INSENSITIVE);

        static final Pattern DISCORD_TAG_PATTERN = Pattern.compile(
                //  schema (http/s)                  ipv4            OR        namespace                 port     path         ends
                //   |-----------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
                "(<@\\d+>)"
        );

        private final PortChuu plugin = PortChuu.getInstance();
        private final Component message;
        private final Component displayName;
        private final TextColor rangeColor;
        private final TextColor nameDefaultColor;
        private final Pattern namesPattern;
        private final boolean msgContainsNames;
        private final boolean rare;


        ChatStyle(Player player, Component message, TextColor rangeColor, TextColor nameDefaultColor) {
            this.displayName = createHoverName(player);
            this.rangeColor = rangeColor;
            this.nameDefaultColor = nameDefaultColor;

            this.message = parseDiscordLinks(makeURLClickable(message));

            Collection<? extends Player> online = Bukkit.getOnlinePlayers();

            StringBuilder names = new StringBuilder();
            online.forEach(p -> {
                names.append(p.getName()).append("|");
            });
            int len = names.length();
            names.delete(len-1, len);
            this.namesPattern = Pattern.compile("\\b(%s)\\b".formatted(names.toString()), Pattern.CASE_INSENSITIVE);
            this.msgContainsNames = this.namesPattern.matcher(PlainTextComponentSerializer.plainText().serialize(message)).find();
            this.rare = msgContainsNames && new Random().nextFloat() > 0.95f;
        }

        @Override
        public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
            return Component.empty()
                    .append(Component.text("| ").color(rangeColor).decorate(TextDecoration.BOLD))
                    .append(displayName.color(nameDefaultColor))
                    .append(Component.text("\u300B ").color(NamedTextColor.GRAY))
                    .append(msgContainsNames ? findUsernamePings(this.message, viewer, rare) : this.message);
        }

        public Component makeURLClickable(final Component in) {
            return in.replaceText(TextReplacementConfig.builder().match(URL_PATTERN).replacement(url -> {
                String link = url.content();
                String finalUrl = link.startsWith("https://") || link.startsWith("http://") ? link : "http://" + link;
                return url
                    //.color(TextColor.color(0xDDDDFF))
                    .clickEvent(ClickEvent.openUrl(finalUrl));
            }).build());
        }

        public Component parseDiscordLinks(final Component in) {
            return in.replaceText(TextReplacementConfig.builder().match(DISCORD_TAG_PATTERN).replacement(discordTag -> {
                String tagString = discordTag.content();
                long id = Long.parseLong(tagString.substring(2,tagString.length()-1));
                String name = plugin.getDiscordSRVHook().getMemberName(id);
                return name == null
                        ? discordTag
                        : discordTag.content("@" + name)
                        //.color(TextColor.color(0xFFEEDD))
                ;
            }).build());
        }

        public Component findUsernamePings(final Component in, Audience viewer, boolean rare) {
            return in.replaceText(TextReplacementConfig.builder().match(namesPattern).replacement(name -> {
                Player p = Bukkit.getPlayer(name.content());
                TextColor nameColor = rare ? NamedTextColor.GOLD : TextColor.color(0xFFDDAA);
                if (p == viewer) {
                    playPingSound(p, rare);
                    return name.color(nameColor).decorate(TextDecoration.BOLD);
                }
                return name.color(nameColor);
            }).build());
        }

        public void playPingSound(Player p, boolean rare) {
            //noinspection ConstantConditions
            playNotes(p, 0.53f, rare);
            playNotes(p, 0.79f, rare);
            //noinspection deprecation
            Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
                if (p.isOnline()) {
                    playNotes(p, 0.71f, rare);
                    playNotes(p, 1.06f, rare);
                }
            }, 3);
        }

        private void playNotes(Player p, float pitch, boolean rare) {
            p.playSound(Sound.sound(Key.key(rare ? "ui.toast.challenge_complete" : "block.note_block.bell"), Sound.Source.PLAYER, 1.0f, pitch));
        }

        public static Component createHoverName(Player p) {
            HoverEvent<HoverEvent.ShowEntity> hover = HoverEvent.showEntity(Key.key("minecraft", "player"), p.getUniqueId(), p.name());
            return p.displayName().hoverEvent(hover).clickEvent(ClickEvent.suggestCommand("/tell %s ".formatted(p.getName())));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent ev) {
        Player p = ev.getPlayer();

        List<Component> birth = ev.renderer().render(p, ChatStyle.createHoverName(ev.getPlayer()), ev.message(), p).children();
        TextColor rangeColor = birth.get(0).color();
        TextColor nameDefaultColor = birth.get(1).color();

        ev.renderer(new ChatStyle(p, ev.message(), rangeColor, nameDefaultColor));
    }
}
