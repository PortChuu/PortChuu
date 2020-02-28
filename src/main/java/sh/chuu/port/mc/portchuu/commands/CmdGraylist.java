package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.track.PromotionResult;
import net.luckperms.api.util.Tristate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import sh.chuu.port.mc.portchuu.PortChuu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CmdGraylist implements TabExecutor {
    private static final String ADD_PERM = "portchuu.command.graylist.add";
    private static final String GRAYLIST_PERM = "portchuu.graylist";
    private static final String ADD = "add";
    private static final String CHECK = "check";
    private final PortChuu plugin = PortChuu.getInstance();
    private final String graylistGroup = plugin.getConfig().getString("graylist.group", "graylist");
    private final String graylistTrack = plugin.getConfig().getString("graylist.track", "graylist");
    private final String graylistGroupNode = "group." + graylistGroup;

    @Override
    // TODO: OML I NEED TO STOP BEING TOO FANCY QWQ
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        sender.sendMessage("Graylist not yet implemented");
        return true;
//
//        if (sender.hasPermission(ADD_PERM)) {
//            if (args.length > 2) {
//                sender.sendMessage(usageAdd());
//            }
//
//            if (args[0].equalsIgnoreCase(ADD)) {
//                addThen(args[1], (ts, name) -> {
//                    if (ts == Tristate.UNDEFINED) {
//                        sender.sendMessage("Couldn't add " + name);
//                    } else if (ts.asBoolean()) {
//                        sender.sendMessage(name + " Success");
//                    } else {
//                        sender.sendMessage(name + " Failure");
//                    }
//                }).thenAcceptAsync(lp -> {
//                    if (!lp)
//                        sender.sendMessage("LP couldn't load");
//                });
//            } else if (args[0].equalsIgnoreCase(CHECK)) {
//                checkThen(args[1], (has, name) -> {
//                    if (has)
//                        sender.sendMessage(name + " has it");
//                    else
//                        sender.sendMessage(name + " does not have it");
//                }).thenAcceptAsync(lp -> {
//                    if (!lp)
//                        sender.sendMessage("LP couldn't load");
//                });
//            }
//            return true;
//        }
//
//        if (args.length == 0) {
//            sender.sendMessage("You're not graylisted yet!");
//        }
//
//        if (args.length == 1) {
//            checkThen(args[0], (has, name) -> {
//                if (has)
//                    sender.sendMessage(name + " has it");
//                else
//                    sender.sendMessage(name + " does not have it");
//            }).thenAcceptAsync(lp -> {
//                if (!lp)
//                    sender.sendMessage("LP couldn't load");
//            });
//        }
//
//        return true;
    }

    private CompletableFuture<Boolean> getUserThen(String name, BiConsumer<LuckPerms, User> then) {
        CompletableFuture<Boolean> ret = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer p;
            Player pl = Bukkit.getPlayerExact(name);
            if (pl == null) {
                p = Bukkit.getOfflinePlayer(name);
                if (!p.hasPlayedBefore()) {
                    then.accept(null, null);
                    return;
                }
            } else {
                p = pl;
            }

            LuckPerms lp = plugin.getLpAPI();
            if (lp != null) {
                lp.getUserManager().loadUser(p.getUniqueId()).thenAcceptAsync(user -> {
                    then.accept(lp, user);
                    lp.getUserManager().saveUser(user);
                });
            }
            ret.complete(lp != null);
        });

        return ret;
    }

    private CompletableFuture<Boolean> getUserThen(UUID uuid, BiConsumer<LuckPerms, User> then) {
        CompletableFuture<Boolean> ret = new CompletableFuture<>();

        LuckPerms lp = plugin.getLpAPI();

        if (lp != null) {
            lp.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                then.accept(lp, user);
                lp.getUserManager().saveUser(user);
            });
        }
        ret.complete(lp != null);
        return ret;
    }


    private CompletableFuture<Boolean> checkThen(String name, BiConsumer<Boolean, String> then) {
        return getUserThen(name, (lp, user) -> {
            Tristate res = user.getCachedData().getPermissionData(QueryOptions.nonContextual()).checkPermission(GRAYLIST_PERM);
            then.accept(res.asBoolean(), user.getUsername());
        });
    }

    private CompletableFuture<Boolean> addThen(String name, BiConsumer<Tristate, String> then) {
        return getUserThen(name, (lp, user) -> {
            Tristate checkPerm = user.getCachedData().getPermissionData(QueryOptions.nonContextual()).checkPermission(GRAYLIST_PERM);
            if (checkPerm.asBoolean()) {
                then.accept(Tristate.UNDEFINED, user.getUsername());
                return;
            }

            PromotionResult result = lp.getTrackManager().getTrack(graylistTrack).promote(user, QueryOptions.nonContextual().context());
            if (result.wasSuccessful())
                then.accept(Tristate.TRUE, user.getUsername());
            else
                then.accept(Tristate.FALSE, user.getUsername());
        });
    }

    private BaseComponent usageAdd() {
        String msg = "Usage: /graylist <add|check> <player>";
        BaseComponent ret = new TextComponent(msg);
        ret.setColor(ChatColor.RED);
        return ret;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission(ADD_PERM)) {
            if (args.length == 1) {
                ArrayList<String> list = new ArrayList<>();
                if (ADD.startsWith(args[0].toLowerCase())) list.add(ADD);
                if (CHECK.startsWith(args[0].toLowerCase())) list.add(CHECK);
                return list;
            }
            if (args.length == 2) {
                return null;
            }
        } else {
            if (args.length == 1) {
                return null;
            }
        }
        return ImmutableList.of();
    }
}
