package com.github.shynixn.blockball.business.logic.game;

import com.github.shynixn.blockball.api.entities.*;
import com.github.shynixn.blockball.business.bukkit.BlockBallPlugin;
import com.github.shynixn.blockball.business.Config;
import com.github.shynixn.blockball.business.Language;
import com.github.shynixn.blockball.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.blockball.lib.*;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HelperGameEntity extends GameEntity implements MiniGame {
    private final List<Player> lobby = new ArrayList<>();
    private GameStage stage = GameStage.DISABLED;
    private int countdown;
    private boolean isStarting;
    private final Map<Player, Team> preSelection = new HashMap<>();
    private final LightSound blingsound = new FastSound("NOTE_PLING", 1.0, 2.0);

    private int bumeper = 40;
    private int secondbumper = 20;
    private final Map<Player, Integer> bumpers = new HashMap<>();

    HelperGameEntity(Arena arena) {
        super(arena);
        if (arena.isEnabled())
            this.stage = GameStage.ENABLED;
        this.countdown = arena.getLobbyMeta().getCountDown();
    }

    @Override
    public synchronized boolean leave(Player player) {
        if (this.lobby.contains(player))
            this.lobby.remove(player);
        if (this.preSelection.containsKey(player))
            this.preSelection.remove(player);
        return super.leave(player);
    }

    public synchronized boolean joinLobby(Player player) {
        if (this.canJoinLobby(player)) {
            this.lobby.add(player);
            this.storeTemporaryInventory(player);
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().clear();
            player.updateInventory();
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setExp(0);
            player.setLevel(0);
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(this.getArena().getLobbyMeta().getLobbySpawn());
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean join(Player player, Team team) {
        if (team == Team.RED) {
            this.redTeam.add(player);
            if (this.arena.getTeamMeta().getRedSpawnPoint() != null)
                player.teleport(this.arena.getTeamMeta().getRedSpawnPoint());
            else
                player.teleport(this.arena.getBallSpawnLocation());
            if (this.getHologram() != null) {
                this.getHologram().show(player);
            }
            return true;
        } else if (team == Team.BLUE) {
            this.blueTeam.add(player);
            if (this.arena.getTeamMeta().getBlueSpawnPoint() != null)
                player.teleport(this.arena.getTeamMeta().getBlueSpawnPoint());
            else
                player.teleport(this.arena.getBallSpawnLocation());
            if (this.getHologram() != null) {
                this.getHologram().show(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        this.bumpers.clear();
        super.reset(false);
        this.stage = GameStage.ENABLED;
        this.countdown = this.arena.getLobbyMeta().getCountDown();
        this.isStarting = false;
    }

    public void startGame() {
        this.countdown = this.arena.getLobbyMeta().getGameTime();
        this.stage = GameStage.RUNNING;
        for (final Player player : this.lobby) {
            if (this.preSelection.containsKey(player) &&
                    ((this.preSelection.get(player) == Team.RED && this.getRedTeamPlayers().length < this.arena.getTeamMeta().getTeamMaxSize()) ||
                            (this.preSelection.get(player) == Team.BLUE && this.getBlueTeamPlayers().length < this.arena.getTeamMeta().getTeamMaxSize())))
                this.join(player, this.preSelection.get(player));
            else {
                Team team = Team.RED;
                if (this.blueTeam.size() < this.redTeam.size()) {
                    team = Team.BLUE;
                }
                this.setTeam(player, team);
                this.join(player, team);
            }
        }
        this.lobby.clear();
        this.preSelection.clear();
    }

    void setTeam(Player player, Team team) {
        if (!this.lobby.contains(player))
            return;
        if (this.getAmountFromTeam(team) >= this.arena.getTeamMeta().getTeamMaxSize())
            return;
        if (team == Team.RED) {
            player.getInventory().setArmorContents(this.arena.getTeamMeta().getRedItems());
        } else {
            player.getInventory().setArmorContents(this.arena.getTeamMeta().getBlueItems());
        }
        this.preSelection.put(player, team);
    }

    private boolean endgame;

    public void endGame() {
        this.endgame = true;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            HelperGameEntity.this.reset();
            HelperGameEntity.this.endgame = false;
        }, 20 * 5);
    }

    @Override
    public void run() {
        if (!this.arena.isEnabled())
            return;
        this.secondbumper--;
        if (this.secondbumper <= 0) {
            this.secondbumper = 20;
            this.updateSigns();
            this.fixCachedRangePlayers();
            if (this.stage == GameStage.RUNNING) {
                if (this.getPlayers().isEmpty()) {
                    this.reset();
                } else if (this.getPlayers().size() < this.arena.getLobbyMeta().getMinPlayers()) {
                    this.sendMessageToPlayers(Language.GAME_DRAW_TITLE, Language.GAME_DRAW_SUBTITLE);
                    this.endGame();
                }
                this.countdown--;
                for (final Player player : this.getPlayers()) {
                    player.setLevel(this.countdown);
                }
                if (this.arena.getTeamMeta().isSpectatorMessagesEnabled()) {
                    for (final Player player : this.getPlayersInRange()) {
                        if (!this.playData.contains(player))
                            this.playData.add(player);
                    }
                    this.arena.getTeamMeta().getScoreboard().play(this.countdown, this.redGoals, this.blueGoals, this.getPlayersInRange());
                } else {
                    this.arena.getTeamMeta().getScoreboard().play(this.countdown, this.redGoals, this.blueGoals, this.getPlayers());
                }
                if (this.countdown <= 10 && !this.endgame) {
                    try {
                        this.blingsound.play(this.getPlayers().toArray(new Player[this.getPlayers().size()]));
                    } catch (final InterPreter19Exception e) {
                        Bukkit.getServer().getConsoleSender().sendMessage(BlockBallPlugin.PREFIX_CONSOLE + ChatColor.RED + "Invalid 1.8/1.9 sound. [BlingSound]");
                    }
                }
                if (this.countdown == 0) {
                    NMSRegistry.addMoney(this.arena.getTeamMeta().getRewardGames(), this.blueTeam.toArray(new Player[this.blueTeam.size()]));
                    NMSRegistry.addMoney(this.arena.getTeamMeta().getRewardGames(), this.redTeam.toArray(new Player[this.redTeam.size()]));
                    this.executeCommand(this.arena.getTeamMeta().getGamendCommand(), this.getPlayers());
                    this.getArena().getTeamMeta().getScoreboard().remove();
                    if (this.redGoals > this.blueGoals) {
                        this.executeCommand(this.arena.getTeamMeta().getWinCommand(), this.redTeam);
                        NMSRegistry.addMoney(this.arena.getTeamMeta().getRewardWinning(), this.redTeam.toArray(new Player[this.redTeam.size()]));
                        this.sendMessageToPlayers(this.decryptText(this.arena.getTeamMeta().getRedwinnerTitleMessage()), this.decryptText(this.arena.getTeamMeta().getRedwinnerSubtitleMessage()));
                        this.endGame();
                    } else if (this.redGoals < this.blueGoals) {
                        this.executeCommand(this.arena.getTeamMeta().getWinCommand(), this.blueTeam);
                        NMSRegistry.addMoney(this.arena.getTeamMeta().getRewardWinning(), this.blueTeam.toArray(new Player[this.blueTeam.size()]));
                        this.sendMessageToPlayers(this.decryptText(this.arena.getTeamMeta().getBluewinnerTitleMessage()), this.decryptText(this.arena.getTeamMeta().getBluewinnerSubtitleMessage()));
                        this.endGame();
                    } else {
                        this.sendMessageToPlayers(Language.GAME_DRAW_TITLE, Language.GAME_DRAW_SUBTITLE);
                        this.endGame();
                    }
                }
            }
            if (this.stage == GameStage.ENABLED) {
                if (!this.lobby.isEmpty()
                        && this.lobby.size() >= this.arena.getLobbyMeta().getMinPlayers()
                        && (!this.arena.getTeamMeta().isForceEvenTeamsEnabled() || this.lobby.size() % 2 == 0)) {
                    if (!this.isStarting) {
                        this.isStarting = true;
                    } else {
                        this.countdown--;
                        for (final Player player : this.lobby) {
                            player.setLevel(this.countdown);
                        }
                        if (this.countdown <= 10) {
                            for (final Player player : this.lobby) {
                                final float exp = ((float) (10 - this.countdown)) / 10;
                                player.setExp(exp);
                            }
                        }
                        if (this.countdown <= 5) {
                            try {
                                this.blingsound.play(this.lobby.toArray(new Player[this.lobby.size()]));
                            } catch (final InterPreter19Exception e) {
                                Bukkit.getServer().getConsoleSender().sendMessage(BlockBallPlugin.PREFIX_CONSOLE + ChatColor.RED + "Invalid 1.8/1.9 sound. [BlingSound]");
                            }
                        }
                        if (this.countdown <= 0) {
                            this.isStarting = false;
                            this.countdown = this.arena.getLobbyMeta().getCountDown();
                            if (this.getHologram() != null)
                                this.getHologram().setText(this.decryptText(this.arena.getTeamMeta().getHologramText()));
                            this.startGame();
                        }
                    }
                } else {
                    if (this.isStarting) {
                        this.countdown = this.arena.getLobbyMeta().getCountDown();
                        this.isStarting = false;
                        for (final Player player : this.lobby) {
                            player.setLevel(0);
                            player.setExp(0.0F);
                        }
                    }
                }
            }
        }
        this.playerForcefield();
        super.run();
    }

    boolean canJoinLobby(Player player) {
        return !this.isLobbyFull() && !this.lobby.contains(player) && this.stage == GameStage.ENABLED;
    }

    boolean isLobbyFull() {
        return (this.arena.getLobbyMeta().getMaxPlayers() * 2) <= this.lobby.size();
    }

    boolean isInLobby(Player player) {
        return this.lobby.contains(player);
    }

    private int getAmountFromTeam(Team teamOther) {
        int i = 0;
        for (final Team team : this.preSelection.values()) {
            if (teamOther == team)
                i++;
        }
        return i;
    }

    @Override
    protected String decryptText(String text) {
        try {
            text = ChatColor.translateAlternateColorCodes('&', text.replace(":countdown", String.valueOf(this.countdown)));
        } catch (final Exception e) {
            this.sendErrorMessage();
        }
        return super.decryptText(text);
    }

    @Override
    public GameStage getStage() {
        return this.stage;
    }

    private void updateSigns() {
        final IPosition[] signPositions = this.arena.getLobbyMeta().getSignPositions().toArray(new IPosition[this.arena.getLobbyMeta().getSignPositions().size()]);
        for (final IPosition position : signPositions) {
            final Location location = position.toLocation();
            if (location.getBlock().getType() == Material.SIGN_POST || location.getBlock().getType() == Material.WALL_SIGN) {
                Config.getInstance().getMinigameSign().updateJoinSignConsideringMaxPlayers((Sign) location.getBlock().getState(), this, this.lobby);
            } else {
                this.arena.getLobbyMeta().removeSignPosition(position);
            }
        }
        final IPosition[] redSignPositions = this.arena.getLobbyMeta().getRedTeamSignPositions().toArray(new IPosition[this.arena.getLobbyMeta().getRedTeamSignPositions().size()]);
        for (final IPosition position : redSignPositions) {
            final Location location = position.toLocation();
            if (location.getBlock().getType() == Material.SIGN_POST || location.getBlock().getType() == Material.WALL_SIGN) {
                Config.getInstance().getTeamSign().updateTeamSignConsideringMinigame((Sign) location.getBlock().getState(), this, Team.RED, this.preSelection);
            } else {
                this.arena.getLobbyMeta().removeRedTeamSignPosition(position);
            }
        }
        final IPosition[] blueSignPositions = this.arena.getLobbyMeta().getBlueTeamSignPositions().toArray(new IPosition[this.arena.getLobbyMeta().getBlueTeamSignPositions().size()]);
        for (final IPosition position : blueSignPositions) {
            final Location location = position.toLocation();
            if (location.getBlock().getType() == Material.SIGN_POST || location.getBlock().getType() == Material.WALL_SIGN) {
                Config.getInstance().getTeamSign().updateTeamSignConsideringMinigame((Sign) location.getBlock().getState(), this, Team.BLUE, this.preSelection);
            } else {
                this.arena.getLobbyMeta().removeBlueTeamSignPosition(position);
            }
        }
    }

    void storeTemporaryInventory(Player player) {
        final TemporaryPlayerStorage storage = new TemporaryPlayerStorage();
        storage.armorContent = player.getInventory().getArmorContents().clone();
        storage.isFlying = player.getAllowFlight();
        storage.inventory = player.getInventory().getContents().clone();
        storage.gameMode = player.getGameMode();
        storage.level = player.getLevel();
        storage.exp = player.getExp();
        storage.health = player.getHealth();
        storage.foodLevel = player.getFoodLevel();
        storage.scoreboard = player.getScoreboard();
        this.temporaryStorage.put(player, storage);
    }

    private void playerForcefield() {
        this.bumeper--;
        if (this.bumeper <= 0) {
            this.bumeper = 40;
            for (final Player player : this.getPlayers()) {
                if (!this.arena.isLocationInArea(player.getLocation()) && !SChatMenuManager.getInstance().isUsing(player)) {
                    if (!this.bumpers.containsKey(player))
                        this.bumpers.put(player, 0);
                    else
                        this.bumpers.put(player, this.bumpers.get(player) + 1);
                    final Vector knockback = this.arena.getBallSpawnLocation().toVector().subtract(player.getLocation().toVector());
                    player.getLocation().setDirection(knockback);
                    player.setVelocity(knockback);
                    final Vector direction = this.arena.getBallSpawnLocation().toVector().subtract(player.getLocation().toVector());
                    player.setVelocity(direction.multiply(0.1));
                    if (this.bumpers.get(player) == 5) {
                        player.teleport(this.arena.getBallSpawnLocation());
                    }
                } else if (this.bumpers.containsKey(player)) {
                    this.bumpers.remove(player);
                }
            }
        }
    }
}
