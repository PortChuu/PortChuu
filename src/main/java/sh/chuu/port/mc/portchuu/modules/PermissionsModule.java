package sh.chuu.port.mc.portchuu.modules;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.track.Track;
import org.bukkit.permissions.Permissible;
import sh.chuu.port.mc.portchuu.PortChuu;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PermissionsModule {
    private final String group;
    private final String track;
    private final String groupNode;

    public PermissionsModule(PortChuu plugin) {
        group = plugin.getConfig().getString("graylist.group", "graylist");
        track = plugin.getConfig().getString("graylist.track", "graylist");
        groupNode = "group." + group;
    }

    private LuckPerms api() {
        return LuckPermsProvider.get();
    }

    private boolean graylist(@Nonnull User user) {
        if (user.getCachedData().getPermissionData(QueryOptions.nonContextual()).checkPermission(groupNode).asBoolean()) {
            // Already graylisted
            return false;
        }
        Track tr = api().getTrackManager().getTrack(track);
        Group gr = api().getGroupManager().getGroup(group);
        if (tr == null) {
            PortChuu.getInstance().getLogger().warning("Track '" + track + "' does not exist!");
            return false;
        }
        if (gr == null) {
            PortChuu.getInstance().getLogger().warning("Group '" + group + "' does not exist!");
            return false;
        }

        // TODO: This kept not working.
        //PromotionResult res = tr.promote(user, QueryOptions.nonContextual().context());
        //boolean success = res.wasSuccessful();

        // Workaround:
        String oldGroup = tr.getPrevious(gr);
        Node oldGrNode = InheritanceNode.builder(oldGroup).build();
        Node grNode = InheritanceNode.builder(gr).build();
        boolean success = user.data().remove(oldGrNode).wasSuccessful() && user.data().add(grNode).wasSuccessful();
        // Workaround end

        if (success) {
            user.setPrimaryGroup(group);
            api().getUserManager().saveUser(user);
        }
        return success;
    }

    public CompletableFuture<UUID> getUUID(String string) {
        return api().getUserManager().lookupUniqueId(string);
    }

    public CompletableFuture<Boolean> graylist(UUID uuid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        api().getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            boolean success = graylist(user);
            future.complete(success);

            if (success) {
                api().getUserManager().saveUser(user);
                api().getMessagingService().ifPresent(ms -> ms.pushUserUpdate(user));
            }
            api().getUserManager().cleanupUser(user);
        });

        return future;
    }

    public boolean isGraylisted(Permissible user) {
        return user.hasPermission(groupNode);
    }

    public CompletableFuture<Boolean> isGraylisted(@Nonnull UUID uuid) {
        CompletableFuture<Boolean> ret = new CompletableFuture<>();
        api().getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            ret.complete(user.getCachedData().getPermissionData(QueryOptions.nonContextual()).checkPermission(groupNode).asBoolean());
            api().getUserManager().cleanupUser(user);
        });

        return ret;
    }

    // gives many pp for osu
    public static void getPP() {
        Object osu = "gud";
        if (osu.equals("git gud"))
            // increase pp
            return;
    }
}