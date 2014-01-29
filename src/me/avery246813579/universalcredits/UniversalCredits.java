package me.avery246813579.universalcredits;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.avery246813579.universalcredits.listener.CreditListener;
import me.avery246813579.universalcredits.listener.PlayerListener;
import me.avery246813579.universalcredits.sql.MoneyLoad;
import me.avery246813579.universalcredits.sql.MoneySaveThread;
import me.avery246813579.universalcredits.util.Api;
import me.avery246813579.universalcredits.util.Enchantments;
import me.avery246813579.universalcredits.util.ItemDb;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

public class UniversalCredits extends JavaPlugin implements Listener {
    /** Money Stuff **/
	private ConcurrentHashMap<String, Integer> balance = new ConcurrentHashMap<String, Integer>();
    private ConcurrentLinkedQueue<Transfer> transfers = new ConcurrentLinkedQueue<Transfer>();
    private ConcurrentLinkedQueue<String> refreshers = new ConcurrentLinkedQueue<String>();
    private MoneySaveThread saveThread = new MoneySaveThread(this);
    private MoneyLoad loadThread;

    /** Database stuff **/
    private String SQL_USER = "root";
    private String SQL_PASS = "T1h823S3368c30D";
    private String SQL_DATA = "198.245.49.34";
    private String SQL_HOST = "EyeOfEnder";

    /** Static Classes **/
    private static UniversalCredits instance;
    private static Api api;
    
    /** Item Stuff **/
    private Enchantments enchants = new Enchantments();
    private ItemDb data;


    public void onEnable() {

        saveDefaultConfig();
        instance = this;
        api = new Api(this);
        this.data = new ItemDb();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new CreditListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        loadThread = new MoneyLoad(this);
        loadThread.SQLconnect();

        this.saveThread.start();
    }

    public void onDisable() {
        loadThread.SQLdisconnect();
        while (transfers.size() > 0)
            try {
                System.out.println("Waiting for saveQueue, " + transfers.size() + " left! Save thread dead: " + (!this.saveThread.isAlive()));
                Thread.currentThread();
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        this.saveThread.terminate();
    }
    
	/*************************************
	 * 
	 * 		    Helper Methods
	 * 
	 *************************************/

    public synchronized ArrayList<String> getLogs(String key) {
        ArrayList<String> lines = Lists.newArrayList();
        try {
            FileInputStream fstream = new FileInputStream("Money.log");

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.toLowerCase().contains(key.toLowerCase())) {
                    lines.add(strLine);
                }
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return lines;
    }


    public void sendMessage(Player player, String message){
    	player.sendMessage(ChatColor.BLUE + "< " + ChatColor.GREEN + "Credits " + ChatColor.BLUE + "> " + ChatColor.GRAY + message);
    }

    public void transferComplete(Transfer transfer) {
        if (transfer.getType() == null) return;
        if (transfer.getType().equals(Transfer.PLAYER_PAYMENT)) {
            Player sender = Bukkit.getPlayerExact(transfer.getSender());
            Player receiver = Bukkit.getPlayerExact(transfer.getReceiver());
            if (transfer.getStatus()) {
                String senderName = transfer.getSender();
                String receiverName = transfer.getReceiver();
                if (sender != null) senderName = sender.getDisplayName();
                if (receiver != null) receiverName = receiver.getDisplayName();
                if (transfer.getStatus()) {
                    if (sender != null) sender.sendMessage(ChatColor.GREEN + "$" + transfer.getAmount() + " has been sent to " + receiverName);
                    if (receiver != null) receiver.sendMessage(ChatColor.GREEN + "$" + transfer.getAmount() + " has been received from " + senderName);
                } else if (sender != null) {
                    sender.sendMessage(ChatColor.GREEN + "Your money transfer of " + transfer.getAmount() + " to " + transfer.getReceiver() + " failed! Reason: " + transfer.getError());
                }
            }
        } else {
            if (transfer.getType().equalsIgnoreCase(Transfer.SIGN_PURCHASE)) return;
            if ((transfer.getType().equalsIgnoreCase(Transfer.MONEY_GIVE)) && (transfer.getReceiver() != null)) {
                Player p = Bukkit.getPlayerExact(transfer.getReceiver());
                if (p == null) return;
                if (transfer.getStatus()) p.sendMessage(ChatColor.GREEN + "You were given $" + transfer.getAmount());
            }
        }
    }

    private boolean canFit(Player p, ItemStack[] items) {
        Inventory inv = Bukkit.createInventory(null, p.getInventory().getContents().length);
        for (int i = 0; i < inv.getSize(); i++)
            if ((p.getInventory().getItem(i) != null) && (p.getInventory().getItem(i).getType() != Material.AIR)) {
                inv.setItem(i, p.getInventory().getItem(i).clone());
            }
        for (ItemStack i : items) {
            HashMap<Integer, ItemStack> item = inv.addItem(new ItemStack[] { i });
            if ((item != null) && (!item.isEmpty())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
	public String signPurchase(Player p, Sign sign) {
        if (ChatColor.stripColor(sign.getLine(0)).equals("[Buy]")) {
            int amount = Integer.parseInt(sign.getLine(1));
            ItemStack item = null;
            try {
                item = this.data.get(sign.getLine(2).toLowerCase());
            } catch (IndexOutOfBoundsException e) {
                System.out.println("unknownItemName");
            } catch (Exception e) {
                System.out.println("unknownItemName");
            }
            if (item == null) return ChatColor.RED + "Error, Can't fetch item from database";
            int price = Integer.parseInt(sign.getLine(3).substring(1));
            item.setAmount(amount);
            if (!api.canAfford(p, price)) return ChatColor.RED + "Can't afford to purchase this item!";
            if (canFit(p, new ItemStack[] { item })) {
                item.setAmount(1);
                for (int i = 0; i < amount; i++)
                    p.getInventory().addItem(new ItemStack[] { item });
                api.withdraw(p.getName(), price);
                p.updateInventory();
                return ChatColor.RED + "Item purchased for $" + price;
            }
            return ChatColor.RED + "Can't fit in inventory";
        }
        if ((ChatColor.stripColor(sign.getLine(0)).equals("[Enchant]")) && (sign.getLine(1).equals("Any"))) {
            int price = Integer.parseInt(sign.getLine(3).substring(1));
            String[] wording = sign.getLine(2).split(":");
            if (wording.length > 0) {
                Enchantment enchant = Enchantments.getByName(wording[0]);
                if (enchant == null) return ChatColor.RED + "Bad enchantment detected, Please notify a admin";
                int level = 1;
                if (wording.length > 1) level = Integer.parseInt(wording[1]);
                ItemStack item = p.getItemInHand();
                if ((item == null) || (item.getType() == Material.AIR) || (!enchant.canEnchantItem(item))) {
                    return ChatColor.RED + "Can't enchant item in hand!";
                }
                if (!api.canAfford(p, price)) return ChatColor.RED + "Can't afford enchantment!";
                if ((item.containsEnchantment(enchant)) && (item.getEnchantmentLevel(enchant) <= level)) return ChatColor.RED + "Item is already enchanted!";
                balance.put(p.getName(), Integer.valueOf(((Integer) balance.get(p.getName())).intValue() - price));
                transfers.add(new Transfer(p.getName(), null, price, Transfer.SIGN_PURCHASE));
                item.addUnsafeEnchantment(enchant, level);
                p.updateInventory();
                return ChatColor.RED + "Item enchanted for $" + price;
            }
            return ChatColor.RED + "Unable to enchant";
        }

        return ChatColor.LIGHT_PURPLE + "Money plugin failed somewhere..";
    }

    private boolean getBalance(Player player, String[] args) {
        if (args.length > 0) {
            Player p1 = Bukkit.getPlayer(args[0]);
            if (p1 == null) {
                sendMessage(player, ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Player not found");
                return true;
            }
            api.refresh(p1.getName());
            sendMessage(player, ChatColor.GOLD + p1.getName() + "'s" + ChatColor.GRAY + " money: " + balance.get(p1.getName() + " Credits"));
            return true;
        }
        sendMessage(player, ChatColor.GRAY + "Credits: " + balance.get(player.getName()));
        return true;
    }

    public boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
    	
    	Player player = (Player) sender; 
    	
    	if(CommandLabel.equalsIgnoreCase("bal")){
    		getBalance(player, args);
    	}
    	
    	else if(CommandLabel.equalsIgnoreCase("moneylog") && player.isOp()){
            if (args.length == 0) {
                sendMessage(player, ChatColor.RED + "You need to use a arguement.");
                return true;
            }
            
            ArrayList<String> lines = getLogs(StringUtils.join(args, " "));
            
            if (lines.size() == 0){
            	sendMessage(player, ChatColor.RED + "No money logs found for '" + StringUtils.join(args, " ") + "'");
            }
            
            else {
                for (int i = lines.size() - 1; (i >= 0) && (i >= lines.size() - 20); i--)
                    sendMessage(player, (String) lines.get(i));
            }

    	}
    	
        else if (CommandLabel.equalsIgnoreCase("setbal") && sender.isOp()) {
            if (args.length < 2) {
                sendMessage(player, ChatColor.RED + "Incorrect usage.");
                return false;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sendMessage(player, ChatColor.RED + "Invalid player.");
                return true;
            }

            int money = 0;
            if (isNumeric(args[1])) {
                money = Integer.parseInt(args[1]);
            } else {
                sendMessage(player, ChatColor.RED + "That is not a number");
                return true;
            }

            int diff = money - balance.get(target.getName());

            balance.put(target.getName(), money);
            transfers.add(new Transfer(null, target.getName(), diff, Transfer.MONEY_SET));
            sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s balance to $" + money + ".");
        }
        return true;
    }

	public ConcurrentHashMap<String, Integer> getBalance() {
		return balance;
	}

	public void setBalance(ConcurrentHashMap<String, Integer> balance) {
		this.balance = balance;
	}

	public ConcurrentLinkedQueue<Transfer> getTransfers() {
		return transfers;
	}

	public void setTransfers(ConcurrentLinkedQueue<Transfer> transfers) {
		this.transfers = transfers;
	}

	public ConcurrentLinkedQueue<String> getRefreshers() {
		return refreshers;
	}

	public void setRefreshers(ConcurrentLinkedQueue<String> refreshers) {
		this.refreshers = refreshers;
	}

	public MoneySaveThread getSaveThread() {
		return saveThread;
	}

	public void setSaveThread(MoneySaveThread saveThread) {
		this.saveThread = saveThread;
	}

	public MoneyLoad getLoadThread() {
		return loadThread;
	}

	public void setLoadThread(MoneyLoad loadThread) {
		this.loadThread = loadThread;
	}

	public String getSQL_USER() {
		return SQL_USER;
	}

	public void setSQL_USER(String sQL_USER) {
		SQL_USER = sQL_USER;
	}

	public String getSQL_PASS() {
		return SQL_PASS;
	}

	public void setSQL_PASS(String sQL_PASS) {
		SQL_PASS = sQL_PASS;
	}

	public String getSQL_DATA() {
		return SQL_DATA;
	}

	public void setSQL_DATA(String sQL_DATA) {
		SQL_DATA = sQL_DATA;
	}

	public String getSQL_HOST() {
		return SQL_HOST;
	}

	public void setSQL_HOST(String sQL_HOST) {
		SQL_HOST = sQL_HOST;
	}

	public static UniversalCredits getInstance() {
		return instance;
	}

	public static void setInstance(UniversalCredits instance) {
		UniversalCredits.instance = instance;
	}

	public static Api getApi() {
		return api;
	}

	public static void setApi(Api api) {
		UniversalCredits.api = api;
	}

	public Enchantments getEnchants() {
		return enchants;
	}

	public void setEnchants(Enchantments enchants) {
		this.enchants = enchants;
	}

	public ItemDb getData() {
		return data;
	}

	public void setData(ItemDb data) {
		this.data = data;
	}
}