package com.github.shynixn.blockball.bukkit.logic.arena;

import com.github.shynixn.blockball.lib.BlockBallApi;
import com.github.shynixn.blockball.bukkit.Config;
import com.github.shynixn.blockball.bukkit.Language;
import com.github.shynixn.blockball.lib.SCommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@SCommandExecutor.Command(command = "blockballreload")
class BlockBallCommandExecutor extends SCommandExecutor {
    @ConsoleCommand
    public void onConsoleSendCommandEvent(CommandSender sender, String[] args) {
        Config.getInstance().reload();
        BlockBallApi.reloadGames();
        sender.sendMessage(Language.PREFIX + ChatColor.GREEN + "Reloaded BlockBall.");
    }
}
