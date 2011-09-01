package com.mcbouncer.plugin;

import com.mcbouncer.util.MCBouncerUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;

public class MCBPlayerListener extends PlayerListener {

    private MCBouncer parent;
    private String lastKick;

    public MCBPlayerListener(MCBouncer parent) {
        this.parent = parent;
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getPlayer().getName().equals(this.lastKick)) {
            event.setLeaveMessage(null);
        }
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Thread r = new PlayerJoinThread(event.getPlayer(), this, event.getJoinMessage());
        r.start();
        event.setJoinMessage(null);
    }
    
    private void isBannedLogic(Player player, String message) {
        String playerName = player.getName();
        String IP = player.getAddress().getAddress().getHostAddress();
        MCBouncerUtil.updateUser(playerName, IP);
        if (MCBouncerUtil.isBanned(playerName)) {
            this.lastKick = playerName;
            player.kickPlayer("Banned: " + MCBouncerUtil.getBanReason(playerName));
            MCBouncer.log.info(playerName + " attempted to join with IP " + IP);
            return;
        }
        if (MCBouncerUtil.isIPBanned(IP)) {
            this.lastKick = playerName;
            player.kickPlayer("Banned: " + MCBouncerUtil.getIPBanReason(IP));
            MCBouncer.log.info(playerName + " attempted to join with IP " + IP);
            return;
        }
        int numBans = MCBouncerUtil.getBanCount(playerName, IP);
        int numNotes = MCBouncerUtil.getNoteCount(playerName);
        if (numBans > 0 || numNotes > 0) {
            String response = playerName + " has ";
            if (numNotes == 0) {
                response += numBans + " ban" + ( numBans == 1 ? "." : "s." );
            } else if (numBans == 0) {
                response += numNotes + " note" + ( numNotes == 1 ? "." : "s." );
            } else {
                response += numBans + " ban" + ( numBans == 1 ? "" : "s" ) + " and " + numNotes + " note" + ( numNotes == 1 ? "." : "s." );
            }
            parent.messageMods(ChatColor.GREEN + response);
        }
        player.getServer().broadcastMessage(message);
    }

    public class PlayerJoinThread extends Thread {

        Player player;
        MCBPlayerListener parent;
        String message;

        public PlayerJoinThread(Player player, MCBPlayerListener parent, String message) {
            this.player = player;
            this.parent = parent;
            this.message = message;
        }

        @Override
        public void run() {
            parent.isBannedLogic(player, message);
        }
    }
}