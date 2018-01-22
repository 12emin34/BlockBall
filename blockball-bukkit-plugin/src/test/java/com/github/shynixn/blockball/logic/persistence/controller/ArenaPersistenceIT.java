package com.github.shynixn.blockball.logic.persistence.controller;

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
public class ArenaPersistenceIT {

  /*  @BeforeAll
    public static void createFolder() {
        File file = new File("BlockBall");
        if (file.exists())
            file.delete();
        file.mkdir();

        Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(Logger.getAnonymousLogger());
        Bukkit.setServer(server);
        World world = mock(World.class);
        when(world.getName()).thenReturn("TestWorld");
        when(server.getWorld(any(String.class))).thenReturn(world);

        try {
            Field field = BlockBallPlugin.class.getDeclaredField("logger");
            field.setAccessible(true);
            field.set(null, Logger.getGlobal());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noPropertiesArena() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(new File("BlockBall"));

        ArenaController controller = Factory.createArenaController(plugin);
        Arena item = controller.create();

        assertThrows(IllegalStateException.class, () -> controller.store(item));
    }

    @Test
    public void storeAndRestoreMinRequiredPropertiesArena() {
     final Plugin plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(new File("BlockBall"));
        final World world = Bukkit.getWorld("");

        final ArenaController controller = Factory.createArenaController(plugin);
        final Arena item = controller.create();
        item.setCorners(new Location(world, 2, 3, 5.2), new Location(world, 7, 2.1, 8));
        item.setBallSpawnLocation(new Location(world, 8, 9, 2));
        item.getMeta().findByTeam(TeamMeta[].class, Team.RED).get().getGoal()
                .setCorners(new Location(world, 2, 100, 5.2), new Location(world, 7, 200, 8));
        item.getMeta().findByTeam(TeamMeta[].class, Team.BLUE).get().getGoal()
                .setCorners(new Location(world, 2, 400, 5.2), new Location(world, 7, 300, 8));

        try {
            controller.store(item);
            controller.reload();
        } catch (IllegalStateException | NullPointerException ex) {
            ex.printStackTrace();
            Assertions.fail("Arena should be valid.");
        }

        assertNotNull(controller.getAll().get(0));
        final Arena arena = controller.getAll().get(0);
        assertEquals(1L, arena.getId());
        assertEquals("1", arena.getName());
        assertEquals("Arena 1", arena.getDisplayName().get());
        assertEquals(false, arena.isEnabled());
        assertEquals(GameType.HUBGAME, arena.getGameType());
        assertEquals(8, ((Location) arena.getBallSpawnLocation()).getBlockX());
        assertEquals(9, ((Location) arena.getBallSpawnLocation()).getBlockY());
        assertEquals(2, ((Location) arena.getBallSpawnLocation()).getBlockZ());
        assertEquals(2, ((Location) arena.getLowerCorner()).getBlockY());
        assertEquals(3, ((Location) arena.getUpperCorner()).getBlockY());

        final AreaSelection redGoal = arena.getMeta().findByTeam(TeamMeta[].class, Team.RED).get().getGoal();
        assertEquals(200, ((Location) redGoal.getUpperCorner()).getBlockY());
        final AreaSelection blueGoal = arena.getMeta().findByTeam(TeamMeta[].class, Team.BLUE).get().getGoal();
        assertEquals(400, ((Location) blueGoal.getUpperCorner()).getBlockY());
    }

    @Test
    public void storeAndRestoreHologramArena() {
      /*  final Plugin plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(new File("BlockBall"));
        final World world = Bukkit.getWorld("");

        final ArenaController controller = Factory.createArenaController(plugin);
        final Arena item = controller.create();
        ((BlockBallArena)item).setId(2);
        item.setCorners(new Location(world, 2, 3, 5.2), new Location(world, 7, 2.1, 8));
        item.setBallSpawnLocation(new Location(world, 8, 9, 2));
        item.getMeta().findByTeam(TeamMeta[].class, Team.RED).get().getGoal()
                .setCorners(new Location(world, 2, 100, 5.2), new Location(world, 7, 200, 8));
        item.getMeta().findByTeam(TeamMeta[].class, Team.BLUE).get().getGoal()
                .setCorners(new Location(world, 2, 400, 5.2), new Location(world, 7, 300, 8));

        List<HologramMeta> meta = item.getMeta().findList(HologramMeta.class).get();

        meta.add(new HologramBuilder().addLine("simple text")
                .setLocation(new Location(world, 520, 281, 92.20))
        .addLine("another text"));
        meta.add(new HologramBuilder().addLine("and")
                .setLocation(new Location(world, 300, 100, 92.20))
                .addLine("petblocks"));

        try {
            controller.store(item);
            controller.reload();
        } catch (IllegalStateException | NullPointerException ex) {
            ex.printStackTrace();
            Assertions.fail("Arena should be valid.");
        }


        final Arena arena = controller.getAll().get(1);
        assertNotNull(arena);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        meta = arena.getMeta().findList(HologramMeta.class).get();
        assertEquals(2, meta.size());

        assertEquals("simple text", meta.get(0).getLine(0).get());
        assertEquals("another text", meta.get(0).getLine(1).get());
        assertEquals("petblocks", meta.get(1).getLine(1).get());
        assertEquals(520, ((Location)meta.get(0).getLocation().get()).getX());
        assertEquals(92.20, ((Location)meta.get(0).getLocation().get()).getZ());
    }*/
}
