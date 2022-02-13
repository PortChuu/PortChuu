package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CmdTeleport implements TabExecutor {
    private static final String OTHER_PERM = "portchuu.command.teleport.other";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Entity en)) {
            sender.sendMessage("You must be an entity");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(usage(sender));
            return true;
        }

        // heccin this is a mess so Imma start commenting here
        Entity origin, destination;
        Location loc;



        // aye so if there's 1 or 2 args
        if (args.length == 1) {
            origin = en;
            destination = getEntity(args[0]);
            if (destination == null) {
                sender.sendMessage(invalidEntity());
                return true;
            }
            loc = destination.getLocation();
        } else if (args.length == 2 && sender.hasPermission(OTHER_PERM)) {
            origin = getEntity(args[0]);
            destination = getEntity(args[1]);
            if (origin == null || destination == null) {
                sender.sendMessage(invalidEntity());
                return true;
            }
            loc = destination.getLocation();
        } else {
            destination = null;


            // these code tries to parse 3 or 4 args to xyz[world]
            String aw, ax, ay, az;
            double x, y, z;
            World w;
            boolean caught = false;
            Location from = en.getLocation();

            Entity testOther = getEntity(args[0]);
            if (args.length == 5 && sender.hasPermission(OTHER_PERM)) {
                if (testOther == null) {
                    sender.sendMessage(invalidEntity());
                    caught = true;
                }
                origin = testOther;
                aw = args[4];
                ax = args[1];
                ay = args[2];
                az = args[3];
            } else if (args.length == 4 && sender.hasPermission(OTHER_PERM)) {
                if (testOther == null) {
                    origin = en;
                    aw = args[3];
                    ax = args[0];
                    ay = args[1];
                    az = args[2];
                } else {
                    origin = testOther;
                    aw = null;
                    ax = args[1];
                    ay = args[2];
                    az = args[3];
                }
            } else {
                origin = en;
                aw = args.length == 4 ? args[3] : null;
                ax = args[0];
                ay = args[1];
                az = args[2];
            }

            // parse digits individually so errors can be shown for each
            try {
                x = parseRelDouble(ax, from.getX());
            } catch (NumberFormatException e) {
                sender.sendMessage(invalidDouble(ax));
                x = 0d;
                caught = true;
            }
            try {
                y = parseRelDouble(ay, from.getY());
            } catch (NumberFormatException e) {
                sender.sendMessage(invalidDouble(ay));
                y = 0d;
                caught = true;
            }
            try {
                z = parseRelDouble(az, from.getZ());
            } catch (NumberFormatException e) {
                sender.sendMessage(invalidDouble(az));
                z = 0d;
                caught = true;
            }

            if (aw == null) {
                w = en.getWorld();
            } else {
                w = Bukkit.getWorld(aw);
                if (w == null){
                    Component unknownWorld = Component.translatable("argument.dimension.invalid", NamedTextColor.RED, Component.text(aw));
                    sender.sendMessage(unknownWorld);
                    caught = true;
                }
            }
            if (caught)
                return true;

            loc = new Location(w, x, y, z);
        }

        if (!origin.teleport(loc)) {
            sender.sendMessage(Component.text("commands.teleport.invalidPosition", NamedTextColor.RED));
            return true;
        }

        Component msg;
        if (destination == null)
            msg = Component.translatable("commands.teleport.success.location.single",
                    origin.name(),
                    Component.text(loc.getBlockX()),
                    Component.text(loc.getY()),
                    Component.text(loc.getZ()));
        else
            msg = Component.translatable("commands.teleport.success.entity.single", origin.name(), destination.name());
        TextTemplates.adminBroadcast(msg, sender);

        return true;
    }

    private Component usage(CommandSender sender) {
        if (sender.hasPermission(OTHER_PERM))
            return Component.text("Usage: /teleport [entity to move] <entity|location [world]>", NamedTextColor.RED);
        else
            return Component.text("Usage: /teleport <entity|location [world]>", NamedTextColor.RED);
    }

    private double parseRelDouble(String tild, double curr) throws NumberFormatException {
        if (tild.isEmpty())
            throw new NumberFormatException();
        double ret;
        if (tild.charAt(0) == '~') {
            ret = tild.length() == 1 ? curr : Double.parseDouble(tild.substring(1)) + curr;
        } else {
            ret = Double.parseDouble(tild);
        }
        return ret;
    }

    private Entity getEntity(String input) {
        Entity ret;
        ret = Bukkit.getPlayerExact(input);
        if (ret == null) {
            try {
                UUID uuid = UUID.fromString(input);
                ret = Bukkit.getPlayer(uuid);
                if (ret == null) ret = Bukkit.getEntity(uuid);
            } catch (IllegalArgumentException ignore) {}
        }
        return ret;
    }

    private Component invalidDouble(String d) {
        return Component.translatable("parsing.double.invalid", NamedTextColor.RED, Component.text(d));
    }

    private Component invalidEntity() {
        return Component.translatable("argument.entity.invalid", NamedTextColor.RED);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!(sender instanceof Entity)) {
            return ImmutableList.of();
        }

        Location loc = ((Entity) sender).getLocation();

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            String numarg;
            if (args[0].isEmpty()) {
                numarg = String.valueOf(loc.getBlockX());
            } else {
                try {
                    parseRelDouble(args[0], 0d);
                    numarg = args[0];
                } catch (NumberFormatException e) {
                    numarg = null;
                }
            }
            if (numarg != null) {
                list.add(numarg);
                list.add(numarg + ' ' + loc.getBlockY());
                list.add(numarg + ' ' + loc.getBlockY() + ' ' + loc.getBlockZ());
                list.add(numarg + ' ' + loc.getBlockY() + ' ' + loc.getBlockZ() + ' ' + loc.getWorld().getName());
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                String name = p.getName();
                if (name.toLowerCase().startsWith(args[0].toLowerCase())) list.add(name);
            }
            return list;
        }

        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            String numarg = null;
            try {
                if (args[1].isEmpty()) {
                    numarg = String.valueOf(loc.getBlockY());
                } else {
                    parseRelDouble(args[1], 0d);
                    numarg = args[1];
                }
                parseRelDouble(args[0], 0d);
                list.add(numarg);
                list.add(numarg + ' ' + loc.getBlockZ());
                list.add(numarg + ' ' + loc.getBlockZ() + ' ' + loc.getWorld().getName());
            } catch (NumberFormatException ignore) {}

            // name + coords section for teleporting others
            if (sender.hasPermission(OTHER_PERM)) {
                if (numarg != null) {
                    if (args[1].isEmpty()) numarg = String.valueOf(loc.getBlockX());
                    list.add(numarg + ' ' + loc.getBlockY());
                    list.add(numarg + ' ' + loc.getBlockY() + ' ' + loc.getBlockZ());
                    list.add(numarg + ' ' + loc.getBlockY() + ' ' + loc.getBlockZ() + ' ' + loc.getWorld().getName());
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String name = p.getName();
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) list.add(name);
                }
            }
            return list;
        }

        if (args.length == 3) {
            try {
                String numarg;
                // Check if second arg is a valid number.  NFE will catch it below if invalid.
                parseRelDouble(args[1], 0d);

                List<String> list = new ArrayList<>();
                if (args[2].isEmpty()) {
                    numarg = String.valueOf(loc.getBlockZ());
                } else {
                    parseRelDouble(args[2], 0d);
                    numarg = args[2];
                }

                try {
                    parseRelDouble(args[0], 0d);
                    list.add(numarg);
                    list.add(numarg + ' ' + loc.getWorld().getName());
                } catch (NumberFormatException ignore) {}

                // name + coords section for teleporting others
                if (sender.hasPermission(OTHER_PERM)) {
                    String numarg2;
                    if (args[2].isEmpty()) {
                        numarg2 = String.valueOf(loc.getBlockY());
                    } else {
                        numarg2 = numarg;
                    }
                    list.add(numarg2);
                    list.add(numarg2 + ' ' + loc.getBlockZ());
                    list.add(numarg2 + ' ' + loc.getBlockZ() + ' ' + loc.getWorld().getName());
                }
                return list;
            } catch (NumberFormatException e) {
                return ImmutableList.of();
            }
        }

        if (args.length == 4) {
            try {
                // Check if first arg is a valid number.  NFE will catch it below if invalid.
                parseRelDouble(args[1], 0d);
                parseRelDouble(args[2], 0d);

                List<String> list = new ArrayList<>();

                try {
                    parseRelDouble(args[0], 0d);
                    // Worlds this time!
                    for (World w : Bukkit.getWorlds()) {
                        String name = w.getName();
                        if (name.toLowerCase().startsWith(args[3].toLowerCase())) list.add(name);
                    }
                } catch (NumberFormatException ignore) {}

                // name + coords section for teleporting others
                if (sender.hasPermission(OTHER_PERM)) {
                    String numarg;
                    if (args[3].isEmpty()) {
                            numarg = String.valueOf(loc.getBlockZ());
                    } else {
                        try {
                            parseRelDouble(args[3], 0d);
                            numarg = args[3];
                        } catch (NumberFormatException e) {
                            return list;
                        }
                    }
                    list.add(numarg);
                    list.add(numarg + ' ' + loc.getWorld().getName());
                }
                return list;
            } catch (NumberFormatException e) {
                return ImmutableList.of();
            }
        }

        if (args.length == 5 && sender.hasPermission(OTHER_PERM)) {
            try {
                parseRelDouble(args[1], 0d);
                parseRelDouble(args[2], 0d);
                parseRelDouble(args[3], 0d);
                List<String> list = new ArrayList<>();
                // Worlds this time!
                for (World w : Bukkit.getWorlds()) {
                    String name = w.getName();
                    if (name.toLowerCase().startsWith(args[4].toLowerCase())) list.add(name);
                }
                return list;
            } catch (NumberFormatException e) {
                return ImmutableList.of();
            }
        }
        return ImmutableList.of();
    }
}
