package com.github.shynixn.blockball.bukkit.logic.business.commandexecutor;

import com.github.shynixn.blockball.api.business.enumeration.Team;
import com.github.shynixn.blockball.api.persistence.controller.ArenaController;
import com.github.shynixn.blockball.api.persistence.entity.Arena;
import com.github.shynixn.blockball.api.persistence.entity.BallMeta;
import com.github.shynixn.blockball.api.persistence.entity.meta.misc.TeamMeta;
import com.github.shynixn.blockball.bukkit.dependencies.worldedit.WorldEditConnection;
import com.github.shynixn.blockball.bukkit.logic.business.BlockBallManager;
import com.github.shynixn.blockball.bukkit.logic.business.commandexecutor.menu.*;
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.LocationBuilder;
import com.github.shynixn.blockball.bukkit.nms.NMSRegistry;
import com.github.shynixn.blockball.lib.ChatBuilder;
import com.github.shynixn.blockball.lib.ItemStackBuilder;
import com.github.shynixn.blockball.lib.SimpleCommandExecutor;
import net.milkbowl.vault.chat.Chat;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutMount;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftZombie;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Shynixn 2017.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class NewArenaCommandExecutor extends SimpleCommandExecutor.Registered {
    private static final String HEADER_STANDARD = ChatColor.WHITE + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                          BlockBall                         ";
    private static final String FOOTER_STANDARD = ChatColor.WHITE + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌1/1┐                            ";

    private final BlockBallManager blockBallManager;
    private final Map<Player, Object[]> cache = new HashMap<>();

    private final List<Page> pages = new ArrayList<>();

    /**
     * Initializes a new commandExecutor by command, plugin.
     *
     * @param plugin plugin
     */
    public NewArenaCommandExecutor(BlockBallManager blockBallManager, Plugin plugin) {
        super("blockball", (JavaPlugin) plugin);
        this.blockBallManager = blockBallManager;

        this.pages.add(new OpenPage());
        this.pages.add(new MainConfigurationPage());
    }

    /**
     * Can be overwritten to listen to player executed commands
     *
     * @param player player
     * @param args   args
     */
    @Override
    public void onPlayerExecuteCommand(Player player, String[] args) {
        for (int i = 0; i < 20; i++) {
            player.sendMessage("");
        }
        player.sendMessage(HEADER_STANDARD);
        player.sendMessage("\n");
        Object[] cache = null;
        if (!this.cache.containsKey(player)) {
            this.cache.put(player, new Object[2]);
        }
        cache = this.cache.get(player);
        final BlockBallCommand command = BlockBallCommand.from(args);
        System.out.println("COMMAND: " + command);
        if (command == null)
            throw new IllegalArgumentException("Command is not registered!");
        Page usedPage = null;
        for (final Page page : this.pages) {
            if (page.getCommandKey() != null && page.getCommandKey() == command.getKey()) {
                usedPage = page;
                if (command == BlockBallCommand.BACK) {
                    final Page newPage = this.getPageById(Integer.parseInt(args[2]));
                    this.sendMessage(player, newPage.buildPage(cache));
                } else {
                    final CommandResult result = page.execute(player, command, cache, args);
                    if (result == CommandResult.BACK) {
                        player.performCommand("blockball back" + " " + usedPage.getPreviousId());
                        return;
                    }
                    System.out.println("1");
                    if (result != CommandResult.SUCCESS && result != CommandResult.CANCEL_MESSAGE) {
                        new ChatBuilder()
                                .component(ChatColor.WHITE + "" + ChatColor.BOLD + "[" + ChatColor.RED + ChatColor.BOLD + "!" + ChatColor.WHITE + ChatColor.BOLD + "]")
                                .setHoverText(result.getMessage()).builder().sendMessage(player);
                    }
                    if (result != CommandResult.CANCEL_MESSAGE) {
                        this.sendMessage(player, page.buildPage(cache));
                    }
                    System.out.println("2");
                }
                break;
            }
        }
        new ChatBuilder()
                .text(ChatColor.STRIKETHROUGH + "----------------------------------------------------").nextLine()
                .component(" >>Save<< ")
                .setColor(ChatColor.GREEN)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, BlockBallCommand.ARENA_SAVE.getCommand())
                .setHoverText("Saves the current arena if possible.")
                .builder()
                .component(">>Back<<")
                .setColor(ChatColor.RED)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, BlockBallCommand.BACK.getCommand() + " " + usedPage.getPreviousId())
                .setHoverText("Opens the blockball arena configuration.")
                .builder().sendMessage(player);

        player.sendMessage(FOOTER_STANDARD);










     /*   if (args.length == 0) {
            this.printFirstPage(player);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("crna")) {

                ArenaController controller = this.blockBallManager.getGameController().getArenaController();
                final Arena arena = controller.create();

                this.cache.put(player, arena);
                this.printArenaPage(player);
            } else if (args[0].equalsIgnoreCase("cna")) {
                this.printArenaPage(player);
            } else if (args[0].equalsIgnoreCase("set-wecorners") && this.cache.containsKey(player)) {
                final Location left = WorldEditConnection.getLeftSelection(player);
                final Location right = WorldEditConnection.getRightSelection(player);
                if (left != null && right != null) {
                    this.cache.get(player).setCorners(left, right);
                }
                this.printArenaPage(player);
            } else if (args[0].equalsIgnoreCase("set-goalred") && this.cache.containsKey(player)) {
                this.setGoal(player, Team.RED);
            } else if (args[0].equalsIgnoreCase("set-goalblue") && this.cache.containsKey(player)) {
                this.setGoal(player, Team.BLUE);
            } else if (args[0].equalsIgnoreCase("set-ballspawn") && this.cache.containsKey(player)) {
                this.cache.get(player).setBallSpawnLocation(player.getLocation());
                this.printArenaPage(player);
            } else if (args[0].equalsIgnoreCase("page-settings") && this.cache.containsKey(player)) {
                this.printSettingsSelectionPage(player);
            }

        } else if (args.length > 1) {
            if (args[0].equalsIgnoreCase("set-displayname") && this.cache.containsKey(player)) {
                final String name = this.mergeArgs(1, args);
                this.cache.get(player).setDisplayName(name);
                this.printArenaPage(player);
            }
            if (args[0].equalsIgnoreCase("save") && this.cache.containsKey(player)) {
                final Arena arena = this.cache.get(player);
                this.blockBallManager.getGameController().getArenaController().store(arena);
                player.sendMessage("Arena was saved.");
                this.onPlayerExecuteCommand(player, new String[]{args[1]});
            }
        }*/

    }

    private String fullCommand(String[] args) {
        StringBuilder builder = new StringBuilder();
        builder.append("/blockball");
        for (String s : args) {
            builder.append(" ");
            builder.append(s);
        }
        return builder.toString();
    }

    private Page getPageById(int id) {
        for (Page page : this.pages) {
            if (page.getId() == id) {
                return page;
            }
        }
        throw new RuntimeException("Page does not exist!");
    }

    private void setGoal(Player player, Team team) {
      /*  final Location left = WorldEditConnection.getLeftSelection(player);
        final Location right = WorldEditConnection.getRightSelection(player);
        if (left != null && right != null) {
            this.getTeamMeta(player, team).getGoal().setCorners(left, right);
        }
        this.printArenaPage(player);*/
    }

    private void printHologramEditingPage(Player player) {
        if (!this.cache.containsKey(player))
            return;

        this.sendMessage(player, new ChatBuilder()
                .nextLine()
                .component("- Type configuration:").builder()
                .component(" [page..]").setColor(ChatColor.YELLOW)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, "/blockball page-set-types")
                .setHoverText("Opens the type configuration to change the gamemodes.")
                .builder().nextLine()
                .text(ChatColor.STRIKETHROUGH + "--------------------")
                .nextLine()
                .component(">>Save<<")
                .setColor(ChatColor.GREEN)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, "/blockball save")
                .setHoverText("Saves the arena.")
                .builder().text(" ")
                .component(">>Back<<")
                .setColor(ChatColor.RED)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, "/blockball crna")
                .setHoverText("Closes the current page.")
        );
    }

    private void printSettingsSelectionPage(Player player) {
        if (!this.cache.containsKey(player))
            return;

        this.sendMessage(player, new ChatBuilder()
                .nextLine()
                .component("- Add line of text:").builder()
                .component(" [edit..]").setColor(ChatColor.GREEN)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, "/blockball add-holo-text")
                .setHoverText("Add a line of text to the hologram")
                .builder().nextLine()
                .text(ChatColor.STRIKETHROUGH + "--------------------")
                .nextLine()
                .component(">>Save<<")
                .setColor(ChatColor.GREEN)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, "/blockball save")
                .setHoverText("Saves the arena.")
                .builder().text(" ")
                .component(">>Back<<")
                .setColor(ChatColor.RED)
                .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND, "/blockball crna")
                .setHoverText("Closes the current page.")
        );

    }

    private Optional<Arena> getArena(String id) {
        return this.blockBallManager.getGameController().getArenaController().getById(id);
    }

    private void sendMessage(Player player, ChatBuilder builder) {
        builder.sendMessage(player);
    }

    private void sendMessage(Player player, ChatBuilder.Component builder) {
        this.sendMessage(player, builder.builder());
    }

}
