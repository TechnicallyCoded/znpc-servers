package ak.znetwork.znpcservers.commands.list;

import ak.znetwork.znpcservers.ServersNPC;
import ak.znetwork.znpcservers.commands.Command;
import ak.znetwork.znpcservers.commands.CommandInformation;
import ak.znetwork.znpcservers.commands.CommandSender;
import ak.znetwork.znpcservers.commands.list.inventory.ConversationGUI;
import ak.znetwork.znpcservers.configuration.Config;
import ak.znetwork.znpcservers.configuration.ConfigValue;
import ak.znetwork.znpcservers.npc.*;
import ak.znetwork.znpcservers.npc.conversation.Conversation;
import ak.znetwork.znpcservers.npc.conversation.ConversationModel;
import ak.znetwork.znpcservers.npc.ToggleType;
import ak.znetwork.znpcservers.configuration.ConfigTypes;
import ak.znetwork.znpcservers.user.ZUser;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static ak.znetwork.znpcservers.npc.NPCPath.AbstractTypeWriter.AbstractTypeWriter;
import static ak.znetwork.znpcservers.npc.NPCPath.AbstractTypeWriter.TypeWriter;

/**
 * @inheritDoc
 */
public class DefaultCommand extends Command {
    /**
     * A string whitespace.
     */
    private static final String WHITESPACE = " ";

    /**
     * Creates a new splitter instance for a whitespace (' ').
     */
    private static final Splitter SPACE_SPLITTER = Splitter.on(WHITESPACE);

    /**
     * Creates a new joiner instance for a whitespace (' ').
     */
    private static final Joiner SPACE_JOINER = Joiner.on(WHITESPACE);

    /**
     * Creates a new command.
     */
    public DefaultCommand() {
        super("znpcs");
    }

    @CommandInformation(arguments = {}, name = "", permission = "")
    public void defaultCommand(CommandSender sender, Map<String, String> args) {
        sender.sendMessage("&6&m------------------------------------------");
        sender.sendMessage("&b&lZNPCS &8» &7ZNetwork");
        sender.sendMessage("&6https://www.spigotmc.org/resources/znpcs-1-8-1-16-bungeecord-serversnpcs-open-source.80940/");
        getCommands().forEach(sender::sendMessage);
        sender.sendMessage("&6&m------------------------------------------");
    }

    @CommandInformation(
            arguments = {"id", "type", "name"},
            name = "create",
            permission = "znpcs.cmd.create",
            help = {
                    " &f&l* &e/znpcs create <npc_id> PLAYER Qentin"
            }
    )
    public void createNPC(CommandSender sender, Map<String, String> args) {
        if (args.size() < 3) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        boolean foundNPC = ConfigTypes.NPC_LIST.stream().anyMatch(npc -> npc.getId() == id);

        if (foundNPC) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_FOUND);
            return;
        }

        String name = args.get("name").trim();

        if (name.length() < 3 || name.length() > 16) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NAME_LENGTH);
            return;
        }

        NPC npc = ServersNPC.createNPC(id, NPCType.valueOf(args.get("type").toUpperCase()), sender.getPlayer().getLocation(), name);
        NPCSkin.forName(args.get("name"), (values) ->
                npc.changeSkin(NPCSkin.forValues(values)));
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {"id"},
            name = "delete",
            permission = "znpcs.cmd.delete",
            help = {
                    " &f&l* &e/znpcs delete <npc_id>"
            }
    )
    public void deleteNPC(CommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        ServersNPC.deleteNPC(id);
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {},
            name = "list",
            permission = "znpcs.cmd.list"
    )
    public void list(CommandSender sender, Map<String, String> args) {
        if (ConfigTypes.NPC_LIST.isEmpty()) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NO_NPC_FOUND);
        } else {
            ConfigTypes.NPC_LIST.forEach(npc ->
                    sender.sendMessage("&f&l * &a" + npc.getId() + " " + npc.getHologramLines().toString()
                            + " &7(&e" + npc.getLocation().getWorldName() + " " + npc.getLocation().getX()
                            + " " + npc.getLocation().getY() + " " + npc.getLocation().getZ() + "&7)"));
        }
    }

    @CommandInformation(
            arguments = {"id", "skin"},
            name = "skin",
            permission = "znpcs.cmd.skin",
            help = {
                    " &f&l* &e/znpcs skin <npc_id> Notch"
            }
    )
    public void setSkin(CommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        NPCSkin.forName(args.get("skin"), (values) -> {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
            foundNPC.changeSkin(NPCSkin.forValues(values));
        });
    }

    @CommandInformation(
            arguments = {"id", "slot"},
            name = "equip",
            permission = "znpcs.cmd.equip",
            help = {
                    " &f&l* &e/znpcs equip <npc_id> [HAND,OFFHAND,HELMET,CHESTPLATE,LEGGINGS,BOOTS]",
                    "&8(You need to have the item in your hand.)"
            }
    )
    public void equip(CommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        foundNPC.updateEquipment(ItemSlot.valueOf(args.get("slot").toUpperCase()), sender.getPlayer().getInventory().getItemInHand());
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {"id", "lines"},
            name = "lines",
            permission = "znpcs.cmd.lines",
            help = {
                    " &f&l* &e/znpcs lines <npc_id> First Second Third-Space"
            }
    )
    public void changeLines(CommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        foundNPC.getNpcPojo().setHologramLines(Lists.reverse(SPACE_SPLITTER.splitToList(args.get("lines"))));
        foundNPC.getHologram().createHologram(); // update
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {"id"},
            name = "move",
            permission = "znpcs.cmd.move",
            help = {
                    " &f&l* &e/znpcs move <npc_id>"
            }
    )
    public void move(CommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        foundNPC.setLocation(sender.getPlayer().getLocation());
        foundNPC.setLastMove(System.nanoTime());
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {"id", "type"},
            name = "type",
            permission = "znpcs.cmd.type",
            help = {
                    " &f&l* &e/znpcs type <npc_id> ZOMBIE"
            }
    )
    public void changeType(CommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        NPCType npcType = NPCType.valueOf(args.get("type").toUpperCase());

        if (npcType != NPCType.PLAYER && npcType.getConstructor() == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.UNSUPPORTED_ENTITY);
            return;
        }

        foundNPC.changeType(npcType);
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {"add", "remove", "cooldown", "list"},
            name = "action",
            isMultiple = true,
            permission = "znpcs.cmd.action",
            help = {
                    " &f&l* &e/znpcs action add <npc_id> SERVER skywars",
                    " &f&l* &e/znpcs action add <npc_id> CMD spawn",
                    " &f&l* &e/znpcs action remove <npc_id> <action_id>",
                    " &f&l* &e/znpcs action cooldown <npc_id> <action_id> <delay_in_seconds>",
                    " &f&l* &e/znpcs action list <npc_id>"
            }
    )
    public void action(CommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }
        if (args.containsKey("add")) {
            List<String> split = SPACE_SPLITTER.splitToList(args.get("add"));

            if (split.size() < 3) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.ACTION_ADD_INCORRECT_USAGE);
                return;
            }

            Integer id = Ints.tryParse(split.get(0));

            if (id == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
                return;
            }

            NPC foundNPC = NPC.find(id);

            if (foundNPC == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
                return;
            }

            foundNPC.getNpcPojo().getClickActions().add(new NPCAction(split.get(1).toUpperCase(), SPACE_JOINER.join(Iterables.skip(split, 2))));
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
        } else if (args.containsKey("remove")) {
            List<String> split = SPACE_SPLITTER.splitToList(args.get("remove"));

            Integer id = Ints.tryParse(split.get(0));

            if (id == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
                return;
            }

            NPC foundNPC = NPC.find(id);

            if (foundNPC == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
                return;
            }

            Integer actionId = Ints.tryParse(split.get(1));

            if (actionId == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            } else {
                if (actionId >= foundNPC.getNpcPojo().getClickActions().size()) {
                    Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NO_ACTION_FOUND);
                    return;
                }
                foundNPC.getNpcPojo().getClickActions().remove(actionId.intValue());
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
            }
        } else if (args.containsKey("cooldown")) {
            List<String> split = SPACE_SPLITTER.splitToList(args.get("cooldown"));

            if (split.size() < 2) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.ACTION_DELAY_INCORRECT_USAGE);
                return;
            }

            Integer id = Ints.tryParse(split.get(0));

            if (id == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
                return;
            }

            NPC foundNPC = NPC.find(id);

            if (foundNPC == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
                return;
            }

            Integer actionId = Ints.tryParse(split.get(1));
            Integer actionDelay = Ints.tryParse(split.get(2));

            if (actionId == null || id == null || actionDelay == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            } else {
                if (actionId >= foundNPC.getNpcPojo().getClickActions().size()) {
                    Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NO_ACTION_FOUND);
                    return;
                }
                foundNPC.getNpcPojo().getClickActions().get(actionId).setDelay(actionDelay);
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
            }
        } else if (args.containsKey("list")) {
            Integer id = Ints.tryParse(args.get("list"));

            if (id == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
                return;
            }

            NPC foundNPC = NPC.find(id);

            if (foundNPC == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
                return;
            }

            if (foundNPC.getNpcPojo().getClickActions().isEmpty()) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NO_ACTION_FOUND);
            } else {
                foundNPC.getNpcPojo().getClickActions().forEach(s -> sender.sendMessage("&8(&a" + foundNPC.getNpcPojo().getClickActions().indexOf(s) + "&8) &6" + s.toString()));
            }
        }
    }

    @CommandInformation(
            arguments = {"id", "type", "value"},
            name = "toggle",
            permission = "znpcs.cmd.toggle",
            help = {
                    " &f&l* &e/znpcs toggle <npc_id> look"
            }
    )
    public void toggle(CommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        ToggleType.valueOf(args.get("type").toUpperCase()).doToggle(foundNPC, args.get("value"));
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {"id", "customizeValues"},
            name = "customize",
            permission = "znpcs.cmd.customize",
            help = {
                    " &f&l* &e/znpcs customize <npc_id> <customization>",
            }
    )
    public void customize(CommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        NPCType npcType = foundNPC.getNpcPojo().getNpcType();

        List<String> customizeOptions = SPACE_SPLITTER.splitToList(args.get("customizeValues"));
        String methodName = customizeOptions.get(0);

        if (npcType.getCustomizationLoader().contains(methodName)) {
            Method method = npcType.getCustomizationLoader().getMethods().get(methodName);
            Iterable<String> split = Iterables.skip(customizeOptions, 1);
            if (Iterables.size(split) < method.getParameterTypes().length) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.TOO_FEW_ARGUMENTS);
                return;
            }
            final String[] values = Iterables.toArray(split, String.class);
            npcType.updateCustomization(foundNPC, methodName, values);
            foundNPC.getNpcPojo().getCustomizationMap().put(methodName, values);
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
        } else {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.METHOD_NOT_FOUND);
            for (Map.Entry<String, Method> method : npcType.getCustomizationLoader().getMethods().entrySet()) {
                sender.sendMessage(ChatColor.YELLOW + method.getKey() + " " + SPACE_JOINER.join(method.getValue().getParameterTypes()));
            }
        }
    }

    @CommandInformation(
            arguments = {"set", "create", "exit", "path", "list"},
            name = "path",
            isMultiple = true,
            permission = "znpcs.cmd.path",
            help = {
                    " &f&l* &e/znpcs path create name",
                    " &f&l* &e/znpcs path set <npc_id> name",
            }

    )
    public void path(CommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        ZUser znpcUser = ZUser.find(sender.getPlayer());
        if (znpcUser == null) {
            return;
        }

        if (args.containsKey("set")) {
            List<String> split = SPACE_SPLITTER.splitToList(args.get("set"));
            if (split.size() < 2) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.PATH_SET_INCORRECT_USAGE);
                return;
            }

            Integer id = Ints.tryParse(split.get(0));

            if (id == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
                return;
            }

            NPC foundNPC = NPC.find(id);

            if (foundNPC == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
                return;
            }

            foundNPC.setPath(AbstractTypeWriter.find(split.get(1)));
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
        } else if (args.containsKey("create")) {
            String pathName = args.get("create");

            if (pathName.length() < 3 || pathName.length() > 16) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NAME_LENGTH);
                return;
            }

            if (AbstractTypeWriter.find(pathName) != null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.PATH_FOUND);
                return;
            }

            if (znpcUser.isHasPath()) {
                sender.getPlayer().sendMessage(ChatColor.RED + "You already have a path creator active, to remove it use /znpcs path exit.");
                return;
            }

            AbstractTypeWriter.forCreation(pathName, znpcUser, TypeWriter.MOVEMENT);
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.PATH_START);
        } else if (args.containsKey("exit")) {
            znpcUser.setHasPath(false);
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.EXIT_PATH);
            ;
        } else if (args.containsKey("list")) {
            if (AbstractTypeWriter.getPaths().isEmpty()) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NO_PATH_FOUND);
            } else {
                AbstractTypeWriter.getPaths().forEach(path -> sender.getPlayer().sendMessage(ChatColor.GREEN + path.getName()));
            }
        }
    }

    @CommandInformation(
            arguments = {"id"},
            name = "teleport",
            permission = "znpcs.cmd.teleport",
            help = {
                    " &f&l* &e/znpcs teleport <npc_id>",
            }
    )
    public void teleport(CommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        // Teleport player to npc location
        sender.getPlayer().teleport(foundNPC.getLocation());
    }

    @CommandInformation(
            arguments = {"id", "height"},
            name = "height",
            permission = "znpcs.cmd.height",
            help = {
                    " &f&l* &e/znpcs height <npc_id> 2",
                    "&8Add more height to the hologram of the npc"
            }
    )
    public void changeHologramHeight(CommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        NPC foundNPC = NPC.find(id);

        if (foundNPC == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
            return;
        }

        Double givenHeight = Doubles.tryParse(args.get("height"));

        if (givenHeight == null) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
            return;
        }

        foundNPC.getNpcPojo().setHologramHeight(givenHeight);
        foundNPC.getHologram().createHologram(); // Update hologram
        Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
    }

    @CommandInformation(
            arguments = {"create", "remove", "gui", "set"},
            name = "conversation",
            isMultiple = true,
            permission = "znpcs.cmd.conversation",
            help = {
                    " &f&l* &e/znpcs conversation create first",
                    " &f&l* &e/znpcs conversation remove first",
                    " &f&l* &e/znpcs conversation set <npc_id> first [CLICK:RADIUS]",
                    " &f&l* &e/znpcs conversation gui &8(&7Open a gui to manage the conversations&8)",
                    "&8RADIUS: &7it is activated when the player is near the npc",
                    "&8CLICK: &7it is activated when the player interacts with the npc"
            }
    )
    public void conversations(CommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INCORRECT_USAGE);
            return;
        }
        if (args.containsKey("create")) {
            String conversationName = args.get("create");
            if (Conversation.exists(conversationName)) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.CONVERSATION_FOUND);
                return;
            }
            if (conversationName.length() < 3 || conversationName.length() > 16) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NAME_LENGTH);
                return;
            }
            ConfigTypes.NPC_CONVERSATIONS.add(new Conversation(conversationName));
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
        } else if (args.containsKey("remove")) {
            String conversationName = args.get("remove");
            if (!Conversation.exists(conversationName)) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NO_CONVERSATION_FOUND);
                return;
            }
            ConfigTypes.NPC_CONVERSATIONS.remove(Conversation.forName(conversationName));
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
        } else if (args.containsKey("gui")) {
            sender.getPlayer().openInventory(new ConversationGUI(sender.getPlayer()).build());
        } else if (args.containsKey("set")) {
            List<String> split = SPACE_SPLITTER.splitToList(args.get("set"));
            if (split.size() < 2) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.CONVERSATION_SET_INCORRECT_USAGE);
                return;
            }
            Integer id = Ints.tryParse(split.get(0));
            if (id == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.INVALID_NUMBER);
                return;
            }
            NPC foundNPC = NPC.find(id);
            if (foundNPC == null) {
                Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.NPC_NOT_FOUND);
                return;
            }
            String conversationName = split.get(1);
            if (Conversation.exists(conversationName)) {
                foundNPC.getNpcPojo().setConversation(new ConversationModel(conversationName, split.size() > 1 ? split.get(2) : "CLICK"));
            } else {
                foundNPC.getNpcPojo().setConversation(null);
            }
            Config.MESSAGES.sendMessage(sender.getCommandSender(), ConfigValue.SUCCESS);
        }
    }
}
