package com.github.shynixn.blockball.bukkit.logic.persistence.controller;

import com.github.shynixn.blockball.api.persistence.entity.PlayerMeta;
import com.github.shynixn.blockball.api.persistence.entity.Stats;
import com.github.shynixn.blockball.bukkit.logic.business.entity.action.ConnectionContextService;
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.StatsData;
import com.github.shynixn.blockball.bukkit.logic.persistence.repository.PlayerSqlRepository;
import com.github.shynixn.blockball.bukkit.logic.persistence.repository.StatsSqlRepository;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"Duplicates", "NestedTryStatement"})
public class StatsSQLiteControllerIT {

    private static Plugin mockPlugin() {
        final YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("sql.enabled", false);
        configuration.set("sql.host", "localhost");
        configuration.set("sql.port", 3306);
        configuration.set("sql.database", "db");
        configuration.set("sql.username", "root");
        configuration.set("sql.password", "");
        final Plugin plugin = mock(Plugin.class);
        final Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(Logger.getGlobal());
        if (Bukkit.getServer() == null)
            Bukkit.setServer(server);
        new File("BlockBall/BlockBall.db").delete();
        when(plugin.getDataFolder()).thenReturn(new File("BlockBall"));
        when(plugin.getConfig()).thenReturn(configuration);
        when(plugin.getResource(any(String.class))).thenAnswer(invocationOnMock -> {
            final String file = invocationOnMock.getArgument(0);
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        });
        return plugin;
    }

    @Test
    public void insertSelectStatsTest() throws ClassNotFoundException {
        final Plugin plugin = mockPlugin();
        final ConnectionContextService connectionContextService = new ConnectionContextService(plugin);

        final UUID uuid = UUID.randomUUID();
        final Player player = mock(Player.class);
        when(player.getName()).thenReturn("Shynixn");
        when(player.getUniqueId()).thenReturn(uuid);
        try (StatsSqlRepository controller = new StatsSqlRepository(connectionContextService)) {
            try (PlayerSqlRepository playerController = new PlayerSqlRepository(connectionContextService)) {
                for (final Stats item : controller.getAll()) {
                    controller.remove(item);
                }
                final Stats meta = controller.create();
                controller.store(meta);
                assertEquals(0, controller.getCount());

                final PlayerMeta playerMeta = playerController.create(player);
                playerController.store(playerMeta);
                ((StatsData)meta).setPlayerId(playerMeta.getId());
                meta.setAmountOfWins(2);

                controller.store(meta);
                assertEquals(0, controller.getCount());

                meta.setAmountOfPlayedGames(2);
                controller.store(meta);

                assertEquals(1, controller.getCount());
                assertEquals(2, controller.getByPlayer(player).get().getAmountOfWins());
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assertions.fail(e);
        }
    }

    @Test
    public void storeLoadPetMetaTest() throws ClassNotFoundException {
        final Plugin plugin = mockPlugin();
        final ConnectionContextService connectionContextService = new ConnectionContextService(plugin);

        final UUID uuid = UUID.randomUUID();
        final Player player = mock(Player.class);
        when(player.getName()).thenReturn("Shynixn");
        when(player.getUniqueId()).thenReturn(uuid);
        try (StatsSqlRepository controller = new StatsSqlRepository(connectionContextService)) {
            try (PlayerSqlRepository playerController = new PlayerSqlRepository(connectionContextService)) {
                for (final Stats item : controller.getAll()) {
                    controller.remove(item);
                }
                Stats stats = controller.create();

                final PlayerMeta playerMeta = playerController.create(player);
                playerController.store(playerMeta);

                ((StatsData)stats).setPlayerId(playerMeta.getId());
                stats.setAmountOfPlayedGames(5);
                stats.setAmountOfWins(2);
                stats.setAmountOfGoals(20);

                controller.store(stats);

                stats = controller.getByPlayer(player).get();
                assertEquals(5, stats.getAmountOfPlayedGames());
                assertEquals(2, stats.getAmountOfWins());
                assertEquals(20, stats.getAmountOfGoals());

                assertEquals(4, stats.getGoalsPerGame());
                assertEquals(0.4, stats.getWinRate());
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assertions.fail(e);
        }
    }
}
