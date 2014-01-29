package me.avery246813579.universalcredits.util;

import me.avery246813579.universalcredits.Transfer;
import me.avery246813579.universalcredits.UniversalCredits;

import org.bukkit.entity.Player;

public class Api {
	 private UniversalCredits main;

	    public Api(UniversalCredits main){
	    	this.main = main;
	    }
	    
	    public void pay(String sender, String receiver, int amount, boolean message) {
	        if (sender != null) {
	            withdraw(sender, amount);
	        }

	        if (!main.getBalance().contains(receiver)) refresh(receiver);
	        main.getBalance().put(receiver, main.getBalance().get(receiver) + amount);
	        main.getTransfers().add(new Transfer(sender, receiver, amount, message ? Transfer.MONEY_GIVE : Transfer.SILENT));
	    }

	    public void pay(String player, int amount, boolean message) {
	        pay(null, player, amount, message);
	    }

	    public void withdraw(String player, int amount) {
	        if (!main.getBalance().contains(player)) refresh(player);
	        main.getBalance().put(player, main.getBalance().get(player) - amount);
	        main.getTransfers().add(new Transfer(null, player, -amount, Transfer.SILENT));
	    }

	    public void refresh(String player) {
	        main.getBalance().put(player, 0);
	        main.getLoadThread().refresh(player);
	    }

	    public boolean canAfford(Player player, int amount) {
	        if (!main.getBalance().contains(player.getName())) refresh(player.getName());
	        return main.getBalance().get(player.getName()) - amount > 0;
	    }
	    
	    public int getBalance(String player){
	    	return main.getBalance().get(player);
	    }
}
