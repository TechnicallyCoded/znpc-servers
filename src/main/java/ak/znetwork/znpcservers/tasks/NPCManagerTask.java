package ak.znetwork.znpcservers.tasks;

import ak.znetwork.znpcservers.ServersNPC;
import ak.znetwork.znpcservers.npc.NPC;
import ak.znetwork.znpcservers.npc.conversation.ConversationModel;
import ak.znetwork.znpcservers.npc.ToggleType;
import ak.znetwork.znpcservers.configuration.ConfigTypes;
import ak.znetwork.znpcservers.user.ZUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runnable task for handling the {@link NPC}s.
 */
public class NPCManagerTask extends BukkitRunnable {
    /**
     * Creates a new task. This task will handle all the {@link NPC}s.
     *
     * @param serversNPC The plugin instance.
     */
    public NPCManagerTask(ServersNPC serversNPC) {
        this.runTaskTimerAsynchronously(serversNPC, 60L, 1L);
    }

    @Override
    public void run() {
        for (NPC npc : NPC.all()) {
            boolean hasPath = npc.getNpcPath() != null;
            if (hasPath) {
                npc.getNpcPath().handle();
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                ZUser zUser = ZUser.find(player);
                final boolean canSeeNPC = player.getWorld() == npc.getLocation().getWorld() && player.getLocation().distance(npc.getLocation()) <= ConfigTypes.VIEW_DISTANCE;
                if (npc.getNpcViewers().contains(zUser) && !canSeeNPC) // delete the npc for the player if player is not in range
                    npc.delete(zUser, true);
                else if (canSeeNPC) {
                    if (!npc.getNpcViewers().contains(zUser)) {
                        npc.spawn(zUser);
                    }
                    if (ToggleType.isTrue(npc,
                            ToggleType.LOOK) && !hasPath) { // look npc at player
                        npc.lookAt(zUser, player.getLocation(), false);
                    }
                    npc.getHologram().updateNames(zUser);
                    // handle npc conversation
                    ConversationModel conversationStorage = npc.getNpcPojo().getConversation();
                    if (conversationStorage != null && conversationStorage.getConversationType() == ConversationModel.ConversationType.RADIUS) {
                        npc.tryStartConversation(player);
                    }
                }
            }
        }
    }
}
