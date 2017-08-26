package com.github.shynixn.blockball.business.logic.arena;

import com.github.shynixn.blockball.api.entities.DoubleJumpMeta;
import com.github.shynixn.blockball.api.entities.IPosition;
import com.github.shynixn.blockball.api.entities.TeamMeta;
import com.github.shynixn.blockball.lib.*;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

class TeamMetaEntity implements TeamMeta, Serializable {
    private static final long serialVersionUID = 1L;

    private final DoubleJumpMetaEntity doubleJumpMetaEntity = new DoubleJumpMetaEntity();

    private String redTeamName = "&cTeam Red";
    private String blueTeamName = "&9Team Blue";
    private int teamMaxSize = 10;
    private int teamMinSize;
    private String redColor = "&c";
    private String blueColor = "&9";

    private boolean disableDamage;

    private String[] blueItems = initalize(Color.BLUE);
    private String[] redItems = initalize(Color.RED);

    private String joinMessage = "You joined the game.";
    private String leaveMessage = "You left the game.";
    private String howToJoinMessage = "Type ':red' to join the :red team or type ':blue' to join the :blue team. 'Cancel' to exit.";
    private String teamFullMessage = "You cannot join. Team is full.";

    private String redtitleScoreMessage = ":redcolor:redscore : :bluecolor:bluescore";
    private String redsubtitleMessage = ":redcolor:player scored for :red";
    private String bluetitleScoreMessage = ":bluecolor:bluescore : :redcolor:redscore";
    private String bluesubtitleMessage = ":bluecolor:player scored for :blue";
    private String redwinnerTitleMessage = ":redcolor:red";
    private String bluewinnerTitleMessage = ":bluecolor:blue";
    private String redwinnerSubtitleMessage = "&a&lWinner";
    private String bluewinnerSubtitleMessage = "&a&lWinner";

    private String bossBarMessage = ":red :redcolor:redscore : :bluecolor:bluescore :blue";
    private boolean bossBarEnabled;

    private LightBossBar bossBarLight = new FastBossBar(":red :redcolor:redscore : :bluecolor:bluescore :blue");

    private SLocation leaveSpawnpoint;

    private int maxScore = 100;
    private boolean autoTeamJoin;
    private boolean fastJoin;
    private boolean emptyReset;

    private boolean forceEvenTeams;

    private IPosition blueSpawnPoint;
    private IPosition redSpawnPoint;

    private boolean specatormessages;
    private int specatorradius = 100;

    private int rewardGoals;
    private int rewardGames;
    private int rewardWinning;

    private String winCommand;
    private String gamendCommand;

    //Scoreboard
    private String scoreboardTitle = "&a&lBlockBall";
    private boolean scoreboardEnabled;
    private String[] scoreboardLines = new String[]{"", "&eTime:", ":countdown", "&m           ", ":red", "&6vs", ":blue", "&m           ", "&aScore:", ":redcolor:redscore &r: :bluecolor:bluescore", "&m           "};

    private String hologramText = "[ :redcolor:redscore : :bluecolor:bluescore ]";
    private IPosition hologramPosition;
    private boolean hologramEnabled;

    private boolean scoreGlowing;
    private int scoreGlowingSeconds = 3;

    private float walkingSpeed = 0.2F;

    TeamMetaEntity() {
        super();
    }

    TeamMetaEntity(Map<String, Object> items) throws Exception {
        super();
        if (items.containsKey("generic.force-even-teams"))
            this.forceEvenTeams = (boolean) items.get("generic.force-even-teams");
        this.maxScore = (int) items.get("generic.max-score");
        this.autoTeamJoin = (boolean) items.get("generic.auto-team-join");
        this.fastJoin = (boolean) items.get("generic.instant-join");
        this.emptyReset = (boolean) items.get("generic.reset-on-empty");
        this.doubleJumpMetaEntity.setEnabled((boolean) items.get("generic.double-jump"));
        this.teamMinSize = (int) items.get("generic.min-size");
        this.teamMaxSize = (int) items.get("generic.max-size");
        this.disableDamage = !(boolean) items.get("generic.take-damage");
        if (items.get("generic.leave-spawnpoint") != null) {
            this.leaveSpawnpoint = new SLocation(((MemorySection) items.get("generic.leave-spawnpoint")).getValues(true));
        }
        if (items.containsKey("generic.walking-speed")) {
            this.walkingSpeed = new Float((Double) items.get("generic.walking-speed"));
        }

        this.redTeamName = (String) items.get("red.name");
        this.redColor = (String) items.get("red.color");
        if (items.get("red.spawnpoint") != null)
            this.redSpawnPoint = new SLocation(((MemorySection) items.get("red.spawnpoint")).getValues(true));
        this.redItems = ((List<String>) items.get("red.armor")).toArray(new String[((List<String>) items.get("red.armor")).size()]);

        this.blueTeamName = (String) items.get("blue.name");
        this.blueColor = (String) items.get("blue.color");
        if (items.get("blue.spawnpoint") != null)
            this.blueSpawnPoint = new SLocation(((MemorySection) items.get("blue.spawnpoint")).getValues(true));
        this.blueItems = ((List<String>) items.get("blue.armor")).toArray(new String[((List<String>) items.get("blue.armor")).size()]);

        this.specatormessages = (boolean) items.get("spectators.enabled");
        this.specatorradius = (int) items.get("spectators.radius");

        this.bossBarLight.setEnabled((Boolean) items.get("bossbar.enabled"));
        this.bossBarLight.setMessage((String) items.get("bossbar.text"));
        this.bossBarLight.setColor((Integer) items.get("bossbar.color"));
        this.bossBarLight.setStyle((Integer) items.get("bossbar.style"));
        this.bossBarLight.setFlag((Integer) items.get("bossbar.flag"));

        this.scoreboardTitle = (String) items.get("scoreboard.title");
        this.scoreboardEnabled = (boolean) items.get("scoreboard.enabled");
        if (items.containsKey("scoreboard.lines")) {
            this.scoreboardLines = ((List<String>) items.get("scoreboard.lines")).toArray(new String[0]);
        }

        this.joinMessage = (String) items.get("messages.join");
        this.leaveMessage = (String) items.get("messages.leave");
        this.howToJoinMessage = (String) items.get("messages.how-to-join");
        this.teamFullMessage = (String) items.get("messages.team-full");

        if (items.containsKey("commands.game-win"))
            this.winCommand = (String) items.get("commands.game-win");
        if (items.containsKey("commands.game-end"))
            this.gamendCommand = (String) items.get("commands.game-end");

        this.redtitleScoreMessage = (String) items.get("messages.red-score-title");
        this.redsubtitleMessage = (String) items.get("messages.red-score-subtitle");
        this.redwinnerTitleMessage = (String) items.get("messages.red-win-title");
        this.redwinnerSubtitleMessage = (String) items.get("messages.red-win-subtitle");
        this.bluetitleScoreMessage = (String) items.get("messages.blue-score-title");
        this.bluesubtitleMessage = (String) items.get("messages.blue-score-subtitle");
        this.bluewinnerTitleMessage = (String) items.get("messages.blue-win-title");
        this.bluewinnerSubtitleMessage = (String) items.get("messages.blue-win-subtitle");

        this.doubleJumpMetaEntity.setParticle(new SParticle(((MemorySection) items.get("double-jump.particle")).getValues(true)));
        this.doubleJumpMetaEntity.setSound(new FastSound(((MemorySection) items.get("double-jump.sound")).getValues(true)));
        if (items.containsKey("double-jump.vertical-strength"))
            this.doubleJumpMetaEntity.setVerticalStrength((Double) items.get("double-jump.vertical-strength"));
        if (items.containsKey("double-jump.horizontal-strength"))
            this.doubleJumpMetaEntity.setHorizontalStrength((Double) items.get("double-jump.horizontal-strength"));

        this.rewardGoals = (int) items.get("dependencies.vault.rewards-per-goal");
        this.rewardGames = (int) items.get("dependencies.vault.rewards-per-game");
        this.rewardWinning = (int) items.get("dependencies.vault.rewards-per-winning-game");
        this.bossBarEnabled = (boolean) items.get("dependencies.bossbarapi.enabled");
        this.bossBarMessage = (String) items.get("dependencies.bossbarapi.text");

        if (items.get("hologram.enabled") != null)
            this.hologramEnabled = (boolean) items.get("hologram.enabled");
        if (items.get("hologram.text") != null)
            this.hologramText = (String) items.get("hologram.text");
        if (items.get("hologram.position") != null)
            this.hologramPosition = new SLocation(((MemorySection) items.get("hologram.position")).getValues(true));

        if (items.get("score-glowing") != null) {
            this.scoreGlowing = (boolean) items.get("score-glowing.enabled");
            this.scoreGlowingSeconds = (int) items.get("score-glowing.time");
        }
    }

    private static String[] initalize(Color color) {
        final String[] itemStacks = new String[4];
        itemStacks[0] = serialize(new ItemStackBuilder(Material.LEATHER_BOOTS).setColor(color).build());
        itemStacks[1] = serialize(new ItemStackBuilder(Material.LEATHER_LEGGINGS).setColor(color).build());
        itemStacks[2] = serialize(new ItemStackBuilder(Material.LEATHER_CHESTPLATE).setColor(color).build());
        return itemStacks;
    }

    public void copy(TeamMetaEntity entity) {
        entity.redTeamName = this.redTeamName;
        entity.blueTeamName = this.blueTeamName;
        entity.teamMaxSize = this.teamMaxSize;
        entity.redColor = this.redColor;
        entity.blueColor = this.blueColor;

        entity.blueItems = this.blueItems.clone();
        entity.redItems = this.redItems.clone();

        entity.joinMessage = this.joinMessage;
        entity.leaveMessage = this.leaveMessage;
        entity.howToJoinMessage = this.howToJoinMessage;
        entity.teamFullMessage = this.teamFullMessage;

        entity.scoreboardTitle = this.scoreboardTitle;
        entity.scoreboardEnabled = this.scoreboardEnabled;
        entity.scoreboardLines = this.scoreboardLines.clone();

        entity.redtitleScoreMessage = this.redtitleScoreMessage;
        entity.redsubtitleMessage = this.redsubtitleMessage;
        entity.bluetitleScoreMessage = this.bluetitleScoreMessage;
        entity.bluesubtitleMessage = this.bluesubtitleMessage;
        entity.redwinnerTitleMessage = this.redwinnerTitleMessage;
        entity.redwinnerSubtitleMessage = this.redwinnerSubtitleMessage;
        entity.bluewinnerTitleMessage = this.bluewinnerTitleMessage;
        entity.bluewinnerSubtitleMessage = this.bluewinnerSubtitleMessage;

        entity.rewardGames = this.rewardGames;
        entity.rewardGoals = this.rewardGoals;
        entity.rewardWinning = this.rewardWinning;
        entity.specatormessages = this.specatormessages;
        entity.specatorradius = this.specatorradius;

        entity.maxScore = this.maxScore;
        entity.autoTeamJoin = this.autoTeamJoin;

        entity.doubleJumpMetaEntity.setEnabled(this.doubleJumpMetaEntity.isEnabled());
        entity.doubleJumpMetaEntity.setHorizontalStrength(this.doubleJumpMetaEntity.getHorizontalStrength());
        entity.doubleJumpMetaEntity.setVerticalStrength(this.doubleJumpMetaEntity.getVerticalStrength());
        entity.doubleJumpMetaEntity.setSound(this.doubleJumpMetaEntity.getSoundEffect().copy());
        entity.doubleJumpMetaEntity.setParticle(this.doubleJumpMetaEntity.getParticleEffect().copy());
    }

    @Override
    public Location getGameEndSpawnpoint() {
        if (this.leaveSpawnpoint != null)
            return this.leaveSpawnpoint.getLocation();
        return null;
    }

    @Override
    public String getWinCommand() {
        return this.winCommand;
    }

    @Override
    public void setWinCommand(String winCommand) {
        this.winCommand = winCommand;
    }

    @Override
    public String getGamendCommand() {
        return this.gamendCommand;
    }

    @Override
    public void setGamendCommand(String gamendCommand) {
        this.gamendCommand = gamendCommand;
    }

    @Override
    public int getSpecatorradius() {
        return this.specatorradius;
    }

    @Override
    public void setSpecatorradius(int specatorradius) {
        this.specatorradius = specatorradius;
    }

    @Override
    public int getRewardGoals() {
        return this.rewardGoals;
    }

    @Override
    public boolean isSpectatorMessagesEnabled() {
        return this.specatormessages;
    }

    @Override
    public void setSpecatorMessages(boolean enabled) {
        this.specatormessages = enabled;
    }

    @Override
    public void setRewardGoals(int rewardGoals) {
        this.rewardGoals = rewardGoals;
    }

    @Override
    public int getRewardGames() {
        return this.rewardGames;
    }

    @Override
    public void setRewardGames(int rewardGames) {
        this.rewardGames = rewardGames;
    }

    @Override
    public int getRewardWinning() {
        return this.rewardWinning;
    }

    @Override
    public void setRewardWinning(int rewardWinning) {
        this.rewardWinning = rewardWinning;
    }

    @Override
    public void setGameEndSpawnpoint(Location location) {
        if (location != null)
            this.leaveSpawnpoint = new SLocation(location);
        else
            this.leaveSpawnpoint = null;
    }

    @Override
    public void resetArmor() {
        this.blueItems = initalize(Color.BLUE);
        this.redItems = initalize(Color.RED);
    }

    @Override
    public void reset() {
        this.redtitleScoreMessage = ":redcolor:redscore : :bluecolor:bluescore";
        this.redsubtitleMessage = ":redcolor:player scored for :red";
        this.bluetitleScoreMessage = ":bluecolor:bluescore : :redcolor:redscore";
        this.bluesubtitleMessage = ":bluecolor:player scored for :blue";
        this.redwinnerTitleMessage = ":redcolor:red";
        this.bluewinnerTitleMessage = ":bluecolor:blue";
        this.redwinnerSubtitleMessage = "&a&lWinner";
        this.bluewinnerSubtitleMessage = "&a&lWinner";
    }

    @Override
    public String getTeamFullMessage() {
        return this.teamFullMessage;
    }

    @Override
    public void setTeamFullMessage(String teamFullMessage) {
        this.teamFullMessage = teamFullMessage;
    }

    @Override
    public String getRedtitleScoreMessage() {
        return this.redtitleScoreMessage;
    }

    @Override
    public void setRedtitleScoreMessage(String redtitleScoreMessage) {
        this.redtitleScoreMessage = redtitleScoreMessage;
    }

    @Override
    public String getRedsubtitleMessage() {
        return this.redsubtitleMessage;
    }

    @Override
    public void setRedsubtitleMessage(String redsubtitleMessage) {
        this.redsubtitleMessage = redsubtitleMessage;
    }

    @Override
    public String getBluetitleScoreMessage() {
        return this.bluetitleScoreMessage;
    }

    @Override
    public void setBluetitleScoreMessage(String bluetitleScoreMessage) {
        this.bluetitleScoreMessage = bluetitleScoreMessage;
    }

    @Override
    public String getBluesubtitleMessage() {
        return this.bluesubtitleMessage;
    }

    @Override
    public void setBluesubtitleMessage(String bluesubtitleMessage) {
        this.bluesubtitleMessage = bluesubtitleMessage;
    }

    @Override
    public String getRedwinnerTitleMessage() {
        return this.redwinnerTitleMessage;
    }

    @Override
    public void setRedwinnerTitleMessage(String redwinnerTitleMessage) {
        this.redwinnerTitleMessage = redwinnerTitleMessage;
    }

    @Override
    public String getBluewinnerTitleMessage() {
        return this.bluewinnerTitleMessage;
    }

    @Override
    public void setBluewinnerTitleMessage(String bluewinnerTitleMessage) {
        this.bluewinnerTitleMessage = bluewinnerTitleMessage;
    }

    @Override
    public String getRedwinnerSubtitleMessage() {
        return this.redwinnerSubtitleMessage;
    }

    @Override
    public void setRedwinnerSubtitleMessage(String redwinnerSubtitleMessage) {
        this.redwinnerSubtitleMessage = redwinnerSubtitleMessage;
    }

    @Override
    public String getBluewinnerSubtitleMessage() {
        return this.bluewinnerSubtitleMessage;
    }

    @Override
    public void setBluewinnerSubtitleMessage(String bluewinnerSubtitleMessage) {
        this.bluewinnerSubtitleMessage = bluewinnerSubtitleMessage;
    }

    /**
     * Forces even teams on both sides. Red and blue team amount has to be the same
     *
     * @param enabled enabled
     */
    @Override
    public void setForceEvenTeams(boolean enabled) {
        this.forceEvenTeams = enabled;
    }

    /**
     * Returns if even teams on both sides is enabled. Red and blue team amount has to be the same to start
     *
     * @return enabled
     */
    @Override
    public boolean isForceEvenTeamsEnabled() {
        return this.forceEvenTeams;
    }

    /**
     * Returns the walkingSpeed of the players
     *
     * @return speed
     */
    @Override
    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    /**
     * Sets the amount of speed for the players
     *
     * @param amount amount
     */
    @Override
    public void setWalkingSpeed(float amount) {
        this.walkingSpeed = amount;
    }

    @Override
    public int getMaxScore() {
        return this.maxScore;
    }

    @Override
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    @Deprecated
    @Override
    public LightParticle getDoubleJumpParticle() {
        return this.doubleJumpMetaEntity.getParticleEffect();
    }

    @Deprecated
    @Override
    public void setDoubleJumpParticle(LightParticle doubleJumpParticle) {
        this.doubleJumpMetaEntity.setParticle(doubleJumpParticle);
    }

    @Deprecated
    @Override
    public LightSound getDoubleJumpSound() {
        return this.doubleJumpMetaEntity.getSoundEffect();
    }

    @Deprecated
    @Override
    public void setDoubleJumpSound(LightSound doubleJumpSound) {
        this.doubleJumpMetaEntity.setSound(doubleJumpSound);
    }

    /**
     * Returns the settings for the double jump
     *
     * @return doubleJumpMeta
     */
    @Override
    public DoubleJumpMeta getDoubleJumpMeta() {
        return this.doubleJumpMetaEntity;
    }

    @Override
    public Location getBlueSpawnPoint() {
        if (this.blueSpawnPoint == null)
            return null;
        return this.blueSpawnPoint.toLocation();
    }

    @Override
    public Location getHologramLocation() {
        if (this.hologramPosition == null)
            return null;
        return this.hologramPosition.toLocation();
    }

    @Override
    public void setHologramLocation(Location location) {
        if (location != null)
            this.hologramPosition = new SLocation(location);
    }

    @Override
    public String getHologramText() {
        if (this.hologramText != null)
            return ChatColor.translateAlternateColorCodes('&', this.hologramText);
        return this.hologramText;
    }

    @Override
    public void setHologramText(String text) {
        this.hologramText = text;
    }

    @Override
    public void setHologramEnabled(boolean enabled) {
        this.hologramEnabled = enabled;
    }

    @Override
    public boolean isHologramEnabled() {
        return this.hologramEnabled;
    }

    @Override
    public void setBlueSpawnPoint(Location blueSpawnPoint) {
        if (blueSpawnPoint != null)
            this.blueSpawnPoint = new SLocation(blueSpawnPoint);
        else
            this.blueSpawnPoint = null;
    }

    @Override
    public Location getRedSpawnPoint() {
        if (this.redSpawnPoint == null)
            return null;
        return this.redSpawnPoint.toLocation();
    }

    @Override
    public void setRedSpawnPoint(Location redSpawnPoint) {
        if (redSpawnPoint != null)
            this.redSpawnPoint = new SLocation(redSpawnPoint);
        else
            this.redSpawnPoint = null;
    }

    @Override
    public String getRedTeamName() {
        return ChatColor.translateAlternateColorCodes('&', this.redTeamName);
    }

    @Override
    public void setRedTeamName(String redTeamName) {
        this.redTeamName = redTeamName;
    }

    @Override
    public String getBlueTeamName() {
        return ChatColor.translateAlternateColorCodes('&', this.blueTeamName);
    }

    @Override
    public void setBlueTeamName(String blueTeamName) {
        this.blueTeamName = blueTeamName;
    }

    @Override
    public int getTeamMaxSize() {
        return this.teamMaxSize;
    }

    @Override
    public void setTeamMaxSize(int teamMaxSize) {
        this.teamMaxSize = teamMaxSize;
    }

    @Override
    public String getRedColor() {
        return ChatColor.translateAlternateColorCodes('&', this.redColor);
    }

    @Override
    public void setRedColor(String redColor) {
        this.redColor = redColor;
    }

    @Override
    public String getBlueColor() {
        return ChatColor.translateAlternateColorCodes('&', this.blueColor);
    }

    @Override
    public void setBlueColor(String blueColor) {
        this.blueColor = blueColor;
    }

    @Override
    public ItemStack[] getBlueItems() {
        final ItemStack[] itemStack = new ItemStack[4];
        itemStack[0] = deserialize(this.blueItems[0]);
        itemStack[1] = deserialize(this.blueItems[1]);
        itemStack[2] = deserialize(this.blueItems[2]);
        itemStack[3] = deserialize(this.blueItems[3]);
        return itemStack;
    }

    @Override
    public boolean isDamageEnabled() {
        return !this.disableDamage;
    }

    @Override
    public void setDamage(boolean enabled) {
        this.disableDamage = !enabled;
    }

    @Override
    public void setBlueItems(ItemStack[] itemStacks) {
        this.blueItems = new String[4];
        this.blueItems[0] = serialize(itemStacks[0]);
        this.blueItems[1] = serialize(itemStacks[1]);
        this.blueItems[2] = serialize(itemStacks[2]);
        this.blueItems[3] = serialize(itemStacks[3]);
    }

    @Override
    public ItemStack[] getRedItems() {
        final ItemStack[] itemStack = new ItemStack[4];
        itemStack[0] = deserialize(this.redItems[0]);
        itemStack[1] = deserialize(this.redItems[1]);
        itemStack[2] = deserialize(this.redItems[2]);
        itemStack[3] = deserialize(this.redItems[3]);
        return itemStack;
    }

    @Override
    public void setRedItems(ItemStack[] itemStacks) {
        this.redItems = new String[4];
        this.redItems[0] = serialize(itemStacks[0]);
        this.redItems[1] = serialize(itemStacks[1]);
        this.redItems[2] = serialize(itemStacks[2]);
        this.redItems[3] = serialize(itemStacks[3]);
    }

    @Override
    public String getJoinMessage() {
        return this.joinMessage;
    }

    @Override
    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }

    @Override
    public String getLeaveMessage() {
        return this.leaveMessage;
    }

    @Override
    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
    }

    @Override
    public void setHowToJoinMessage(String message) {
        this.howToJoinMessage = message;
    }

    @Override
    public String getHowToJoinMessage() {
        return this.howToJoinMessage;
    }

    @Override
    @Deprecated
    public boolean isAllowDoubleJump() {
        return this.doubleJumpMetaEntity.isEnabled();
    }

    @Override
    @Deprecated
    public void setAllowDoubleJump(boolean allowDoubleJump) {
        this.doubleJumpMetaEntity.setEnabled(allowDoubleJump);
    }

    @Override
    public boolean isTeamAutoJoin() {
        return this.autoTeamJoin;
    }

    @Override
    public void setTeamAutoJoin(boolean autoJoin) {
        this.autoTeamJoin = autoJoin;
    }

    @Override
    public boolean isFastJoin() {
        return this.fastJoin;
    }

    @Override
    public void setFastJoin(boolean enable) {
        this.fastJoin = enable;
    }

    @Override
    public int getTeamMinSize() {
        return this.teamMinSize;
    }

    @Override
    public void setTeamMinSize(int teamMinSize) {
        this.teamMinSize = teamMinSize;
    }

    @Override
    public boolean isEmtptyReset() {
        return this.emptyReset;
    }

    @Override
    public void setEmptyReset(boolean enabled) {
        this.emptyReset = enabled;
    }

    @Deprecated
    public String getBossBarMessage() {
        return this.bossBarMessage;
    }

    @Deprecated
    public boolean isBossBarEnabled() {
        return this.bossBarEnabled;
    }

    @Deprecated
    public void setBossBarMessage(String message) {
        this.bossBarMessage = message;
    }

    @Deprecated
    public void setBossBarEnabled(boolean enable) {
        this.bossBarEnabled = enable;
    }

    @Override
    public String getBossBarPluginMessage() {
        return this.bossBarMessage;
    }

    @Override
    public boolean isBossBarPluginEnabled() {
        return this.bossBarEnabled;
    }

    @Override
    public void setBossBarPluginMessage(String message) {
        this.bossBarMessage = message;
    }

    @Override
    public void setBossBarPluginEnabled(boolean enable) {
        this.bossBarEnabled = enable;
    }

    @Override
    public LightBossBar getBossBar() {
        return this.bossBarLight;
    }

    /**
     * Sets the title of the scoreboard
     *
     * @param scoreboardTitle scoreboardTitle
     */
    @Override
    public void setScoreboardTitle(String scoreboardTitle) {
        this.scoreboardTitle = scoreboardTitle;
    }

    /**
     * Returns the title of the scoreboard
     *
     * @return title
     */
    @Override
    public String getScoreboardTitle() {
        if (this.scoreboardTitle == null)
            return null;
        return ChatColor.translateAlternateColorCodes('&', this.scoreboardTitle);
    }

    /**
     * Enables or disables the scoreboard
     *
     * @param enabled scoreboard
     */
    @Override
    public void setScoreboardEnabled(boolean enabled) {
        this.scoreboardEnabled = enabled;
    }

    /**
     * Returns if the scoreboard is enabled
     *
     * @return enabled
     */
    @Override
    public boolean isScoreboardEnabled() {
        return this.scoreboardEnabled;
    }

    /**
     * Sets the lines of the scoreboard
     *
     * @param scoreboardLines scoreboardLines
     */
    @Override
    public void setScoreboardLines(String[] scoreboardLines) {
        this.scoreboardLines = scoreboardLines.clone();
    }

    /**
     * Returns the lines of the scoreboard
     *
     * @return lines
     */
    @Override
    public String[] getScoreboardLines() {
        if (this.scoreboardLines == null)
            return null;
        return this.scoreboardLines.clone();
    }

    @Override
    public void setGoalShooterGlowing(boolean enable) {
        this.scoreGlowing = enable;
    }

    @Override
    public boolean isGoalShooterGlowing() {
        return this.scoreGlowing;
    }

    @Override
    public void setGoalShooterGlowingSeconds(int seconds) {
        this.scoreGlowingSeconds = seconds;
    }

    @Override
    public int getGoalShooterGlowingSeconds() {
        return this.scoreGlowingSeconds;
    }

    public void setBossBarLight(LightBossBar bossBarLight) {
        this.bossBarLight = bossBarLight;
    }

    private static String serialize(ItemStack itemStack) {
        if (itemStack != null) {
            final FileConfiguration configuration = new YamlConfiguration();
            configuration.set("dummy", itemStack);
            return configuration.saveToString();
        }
        return null;
    }

    private static ItemStack deserialize(String text) {
        if (text != null) {
            final FileConfiguration configuration = new YamlConfiguration();
            try {
                configuration.loadFromString(text);
            } catch (final InvalidConfigurationException e) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot deserialize itemsstack.", e);
            }
            return configuration.getItemStack("dummy");
        }
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>(); //Should be replaced by YamlSerializer
        final Map<String, Object> tmp1 = new LinkedHashMap<>();
        final Map<String, Object> tmp2 = new LinkedHashMap<>();
        final Map<String, Object> tmp3 = new LinkedHashMap<>();
        final Map<String, Object> tmp4 = new LinkedHashMap<>();
        final Map<String, Object> tmp5 = new LinkedHashMap<>();
        final Map<String, Object> tmp6 = new LinkedHashMap<>();
        final Map<String, Object> tmp7 = new LinkedHashMap<>();
        final Map<String, Object> tmp8 = new LinkedHashMap<>();
        final Map<String, Object> tmp9 = new LinkedHashMap<>();
        final Map<String, Object> tmp10 = new LinkedHashMap<>();
        final Map<String, Object> tmp12 = new LinkedHashMap<>();
        final Map<String, Object> tmp14 = new LinkedHashMap<>();
        final Map<String, Object> tmp15 = new LinkedHashMap<>();
        final Map<String, Object> tmp0 = new LinkedHashMap<>();

        tmp1.put("max-score", this.maxScore);
        tmp1.put("force-even-teams", this.forceEvenTeams);
        tmp1.put("auto-team-join", this.autoTeamJoin);
        tmp1.put("instant-join", this.fastJoin);
        tmp1.put("reset-on-empty", this.emptyReset);
        tmp1.put("double-jump", this.doubleJumpMetaEntity.isEnabled());
        tmp1.put("min-size", this.teamMinSize);
        tmp1.put("max-size", this.teamMaxSize);
        tmp1.put("take-damage", !this.disableDamage);
        tmp1.put("walking-speed", this.walkingSpeed);
        tmp1.put("leave-spawnpoint", SFileUtils.serialize(this.leaveSpawnpoint));
        map.put("generic", tmp1);

        tmp0.put("game-win", this.winCommand);
        tmp0.put("game-end", this.gamendCommand);
        map.put("commands", tmp0);

        tmp4.put("enabled", this.specatormessages);
        tmp4.put("radius", this.specatorradius);
        map.put("spectators", tmp4);

        tmp5.put("enabled", this.bossBarLight.isEnabled());
        tmp5.put("text", this.bossBarLight.getMessage());
        tmp5.put("color", this.bossBarLight.getColor());
        tmp5.put("style", this.bossBarLight.getStyle());
        tmp5.put("flag", this.bossBarLight.getFlag());
        map.put("bossbar", tmp5);

        tmp6.put("enabled", this.scoreboardEnabled);
        tmp6.put("title", this.scoreboardTitle);
        tmp6.put("lines", this.scoreboardLines);
        map.put("scoreboard", tmp6);

        tmp7.put("join", this.joinMessage);
        tmp7.put("leave", this.leaveMessage);
        tmp7.put("how-to-join", this.howToJoinMessage);
        tmp7.put("team-full", this.teamFullMessage);
        tmp7.put("red-score-title", this.redtitleScoreMessage);
        tmp7.put("red-score-subtitle", this.redsubtitleMessage);
        tmp7.put("red-win-title", this.redwinnerTitleMessage);
        tmp7.put("red-win-subtitle", this.redwinnerSubtitleMessage);
        tmp7.put("blue-score-title", this.bluetitleScoreMessage);
        tmp7.put("blue-score-subtitle", this.bluesubtitleMessage);
        tmp7.put("blue-win-title", this.bluewinnerTitleMessage);
        tmp7.put("blue-win-subtitle", this.bluewinnerSubtitleMessage);
        map.put("messages", tmp7);

        tmp8.put("rewards-per-goal", this.rewardGoals);
        tmp8.put("rewards-per-game", this.rewardGames);
        tmp8.put("rewards-per-winning-game", this.rewardWinning);
        tmp10.put("enabled", this.bossBarEnabled);
        tmp10.put("text", this.bossBarMessage);
        tmp9.put("vault", tmp8);
        tmp9.put("bossbarapi", tmp10);
        map.put("dependencies", tmp9);

        tmp2.put("name", this.redTeamName);
        tmp2.put("color", this.redColor);
        tmp2.put("spawnpoint", SFileUtils.serialize(this.redSpawnPoint));
        tmp2.put("armor", this.redItems);
        map.put("red", tmp2);

        tmp3.put("name", this.blueTeamName);
        tmp3.put("color", this.blueColor);
        tmp3.put("spawnpoint", SFileUtils.serialize(this.blueSpawnPoint));
        tmp3.put("armor", this.blueItems);
        map.put("blue", tmp3);

        tmp12.put("horizontal-strength", this.doubleJumpMetaEntity.getHorizontalStrength());
        tmp12.put("vertical-strength", this.doubleJumpMetaEntity.getVerticalStrength());
        tmp12.put("particle", SFileUtils.serialize(this.doubleJumpMetaEntity.getParticleEffect()));
        tmp12.put("sound", SFileUtils.serialize(this.doubleJumpMetaEntity.getSoundEffect()));
        map.put("double-jump", tmp12);

        tmp14.put("enabled", this.hologramEnabled);
        tmp14.put("text", this.hologramText);
        tmp14.put("position", SFileUtils.serialize(this.hologramPosition));
        map.put("hologram", tmp14);

        tmp15.put("enabled", this.scoreGlowing);
        tmp15.put("time", this.scoreGlowingSeconds);
        map.put("score-glowing", tmp15);

        return map;
    }
}
