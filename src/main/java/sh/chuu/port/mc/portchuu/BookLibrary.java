package sh.chuu.port.mc.portchuu;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookLibrary {
    public static ItemStack rules() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta book = (BookMeta) item.getItemMeta();
        book.setAuthor("Port Chuu Staff");
        book.setTitle("Port Chuu Rules");
        ComponentBuilder page1 = new ComponentBuilder("Port ")
                .color(ChatColor.BLUE)
                .bold(true)
                .append("Chuu")
                .color(ChatColor.DARK_AQUA)
                .append(" Info\n")
                .color(ChatColor.BLUE)
                .append("===============\n")
                .color(ChatColor.DARK_AQUA);
        intro(page1);
        footer(page1, 1);
        book.spigot().addPage(page1.create());

        ComponentBuilder page2 = new ComponentBuilder("Links\n")
                .color(ChatColor.BLUE)
                .bold(true)
                .append("===============\n")
                .color(ChatColor.DARK_AQUA);
        addLink(page2, "Website", "https://port.chuu.sh/");
        addLink(page2, "Map", "https://port.chuu.sh/map/");
        addLink(page2, "Discord", "https://discord.gg/hYcphGC9p2");
        addLink(page2, "Subreddit", "https://reddit.com/r/PortChuu/");
        page2.italic(true)
                .append("\n");
        footer(page2, 2);
        book.spigot().addPage(page2.create());

        ComponentBuilder page3 = new ComponentBuilder("Community Rules\n")
                .color(ChatColor.BLUE)
                .bold(true)
                .append("===============\n")
                .color(ChatColor.DARK_AQUA);
        addRule(page3, 1, "Respect the server and its members.");
        addRule(page3, 2, "Don't cause drama.");
        addRule(page3, 3, "About advertising: don't leech on the server.");
        addRule(page3, 4, "Keep server content safe for everyone.");
        addRule(page3, 5, "No mini-modding.");
        footer(page3, 3);
        book.spigot().addPage(page3.create());

        ComponentBuilder page4 = new ComponentBuilder("Minecraft Rules\n")
                .color(ChatColor.BLUE)
                .bold(true)
                .append("===============\n")
                .color(ChatColor.DARK_AQUA);
        addRule(page4, "M1", "Don't cheat on the server.");
        addRule(page4, "M2", "Don't steal or grief anybody.");
        addRule(page4, "M3", "About building...");
        addLink(page4, "Visit the rules page for detailed info.", "https://port.chuu.sh/#rules");
        page4.append("\n");
        footer(page4, 4);
        book.spigot().addPage(page4.create());

        ComponentBuilder page5 = new ComponentBuilder("Staff Team\n")
                .color(ChatColor.BLUE)
                .bold(true)
                .append("===============\n")
                .color(ChatColor.DARK_AQUA);
        page5.append("//TODO: Finish this book, Chuu!\n", ComponentBuilder.FormatRetention.NONE);
        footer(page5, 5);
        book.spigot().addPage(page5.create());

        item.setItemMeta(book);
        return item;
    }

    private static void intro(ComponentBuilder cb) {
        cb
                .append("  Welcome to the Port Chuu!", ComponentBuilder.FormatRetention.NONE)
                .bold(true)
                .append("  There's only so much that can fit in this book.  Check ", ComponentBuilder.FormatRetention.NONE)
                .append("our website")
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://port.chuu.sh/"))
                .color(ChatColor.BLUE)
                .append(" for the full text!\n" +
                        "  Read through the book for more info, and Feel free to ask if you have any questions!\n", ComponentBuilder.FormatRetention.NONE);
    }

    private static void addLink(ComponentBuilder cb, String name, String href) {
        cb
                .append("- ", ComponentBuilder.FormatRetention.NONE)
                .append(name + "\n")
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, href))
                .color(ChatColor.BLUE);
    }

    private static void addRule(ComponentBuilder cb, Object number, String rule) {
        cb
                .append(number + ". ", ComponentBuilder.FormatRetention.NONE)
                .bold(true)
                .append(rule + "\n")
                .bold(false);
    }

    private static void footer(ComponentBuilder cb, int currentPage) {
        cb
                .append("Hm", ComponentBuilder.FormatRetention.NONE);
        if (currentPage != 1) cb
                .color(ChatColor.GRAY)
                .underlined(true)
                .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"));
        cb
                .append(" | ", ComponentBuilder.FormatRetention.NONE)
                .append("Ln", ComponentBuilder.FormatRetention.NONE);
        if (currentPage != 2) cb
                .color(ChatColor.GRAY)
                .underlined(true)
                .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "2"));
        cb
                .append(" | ", ComponentBuilder.FormatRetention.NONE)
                .append("CR", ComponentBuilder.FormatRetention.NONE);
        if (currentPage != 3) cb
                .color(ChatColor.GRAY)
                .underlined(true)
                .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "3"));
        cb
                .append(" | ", ComponentBuilder.FormatRetention.NONE)
                .append("MR", ComponentBuilder.FormatRetention.NONE);
        if (currentPage != 4) cb
                .color(ChatColor.GRAY)
                .underlined(true)
                .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "4"));
        cb
                .append(" | ", ComponentBuilder.FormatRetention.NONE)
                .append("ST", ComponentBuilder.FormatRetention.NONE);
        if (currentPage != 5) cb
                .color(ChatColor.GRAY)
                .underlined(true)
                .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "5"));
    }
}
