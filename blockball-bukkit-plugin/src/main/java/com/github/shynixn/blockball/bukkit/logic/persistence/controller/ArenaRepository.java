package com.github.shynixn.blockball.bukkit.logic.persistence.controller;

import com.github.shynixn.blockball.api.persistence.controller.ArenaController;
import com.github.shynixn.blockball.api.persistence.entity.Arena;
import com.github.shynixn.blockball.bukkit.BlockBallPlugin;
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.ArenaEntity;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public final class ArenaRepository implements ArenaController {
    private final List<Arena> arenas = new ArrayList<>();
    private Plugin plugin;

    /**
     * Initializes a new arena Repository
     *
     * @param plugin plugin
     */
    private ArenaRepository(Plugin plugin) {
        super();
        this.plugin = plugin;
    }

    /**
     * Creates a new arena with a unique id of this instance
     *
     * @return id
     */
    @Override
    public Arena create() {
        final ArenaEntity arenaEntity = new ArenaEntity();
        arenaEntity.setName(String.valueOf(this.getNewId()));
        return arenaEntity;
    }

    /**
     * Returns the arena by the given id if present
     *
     * @param id id
     * @return arena
     */
    @Override
    public Optional<Arena> getById(String id) {
        for (final Arena arena : this.arenas) {
            if (arena.getName().equals(id)) {
                return Optional.of(arena);
            }
        }
        return Optional.empty();
    }

    /**
     * Stores a new a item in the repository
     *
     * @param item item
     */
    @Override
    public void store(Arena item) {
        if (item != null) {
            if (!this.arenas.contains(item)) {
                this.arenas.add(item);
            }
            if (item != null && item.getName() != null) {
                try {
                    final FileConfiguration configuration = new YamlConfiguration();
                    final File file = new File(this.getFolder(), "arena_" + item.getName() + ".yml");
                    if (file.exists()) {
                        if (!file.delete())
                            throw new IllegalStateException("Cannot delete file!");
                    }
                    if (!file.createNewFile())
                        throw new IllegalStateException("Cannot create file!");
                    configuration.load(file);
                    final ConfigurationSerializable serializable = (ConfigurationSerializable) item;
                    final Map<String, Object> data = serializable.serialize();
                    for (final String key : data.keySet()) {
                        configuration.set("arena." + key, data.get(key));
                    }
                    configuration.save(file);
                } catch (IOException | InvalidConfigurationException ex) {
                    BlockBallPlugin.logger().log(Level.WARNING, "Cannot save arena.", ex);
                }
            }
        }
    }

    /**
     * Removes an item from the repository
     *
     * @param item item
     */
    @Override
    public void remove(Arena item) {
        try {
            final File file = new File(this.getFolder(), "arena_" + item.getName() + ".yml");
            if (file.exists()) {
                if (!file.delete())
                    throw new IllegalStateException("Cannot delete file!");
            }
        } catch (final Exception ex) {
            BlockBallPlugin.logger().log(Level.WARNING, "Cannot delete arena file.", ex);
        }
        this.reload();
    }

    /**
     * Returns the amount of items in the repository
     *
     * @return size
     */
    @Override
    public int size() {
        return this.arenas.size();
    }

    /**
     * Returns all items from the repository as unmodifiableList
     *
     * @return items
     */
    @Override
    public List<Arena> getAll() {
        return Collections.unmodifiableList(this.arenas);
    }

    /**
     * Reloads the content from the fileSystem
     */
    @Override
    public void reload() {
        this.arenas.clear();
        for (int i = 0; (this.getFolder() != null) && (i < this.getFolder().list().length); i++) {
            final String s = this.getFolder().list()[i];
            try {
                if (s.contains("arena_")) {
                    final FileConfiguration configuration = new YamlConfiguration();
                    final File file = new File(this.getFolder(), s);
                    configuration.load(file);
                    final Map<String, Object> data = configuration.getConfigurationSection("arena").getValues(true);
                    final Arena arenaEntity = new ArenaEntity(data, configuration.getStringList("arena.properties.wall-bouncing"));
                    this.arenas.add(arenaEntity);
                }
            } catch (final Exception ex) {
                BlockBallPlugin.logger().log(Level.WARNING, "Cannot read arena file " + s + '.', ex);
            }
        }
    }

    /**
     * Creates a new id
     *
     * @return id
     */
    private int getNewId() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final String s = String.valueOf(i);
            if (this.getById(s) == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the storage folder
     *
     * @return folder
     */
    private File getFolder() {
        if (this.plugin == null)
            throw new IllegalStateException("Plugin cannot be null!");
        final File file = new File(this.plugin.getDataFolder(), "arena");
        if (!file.exists()) {
            if (!file.mkdir())
                throw new IllegalStateException("Cannot create folder!");
        }
        return file;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        this.arenas.clear();
        this.plugin = null;
    }
}
