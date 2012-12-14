package me.nathanaelps.plugins.scrollbooks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.thespuff.plugins.totems.Totems;

public class ScrollBooks extends JavaPlugin implements Listener {

	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static ScrollBooks plugin;
	private static Totems totems;

	public void onDisable() {
		log("Disabled");
	}

	public void onEnable() {
		pluginName = this.getDescription().getName();
		pluginVersion = this.getDescription().getVersion();
		server = this.getServer();
		plugin = this;

		getServer().getPluginManager().registerEvents(this, this);

		setupTotems();
//		setupSpuffTeleport();
		
		log("Enabled.");
	}

	private void setupTotems() {
		ScrollBooks.totems = (Totems) server.getPluginManager().getPlugin("Totems");

		if (ScrollBooks.totems == null) {
			log("Couldn't load Totems.");
		} else {
			log("Hooked into Totems.");			
		}
	}

//	private void setupSpuffTeleport() {
//		ScrollBooks.spuffTeleport = (SpuffTeleport) server.getPluginManager().getPlugin("SpuffTeleport");
//
//		if (ScrollBooks.spuffTeleport == null) {
//			log("Couldn't load spuffTeleport.");
//		} else {
//			log("Hooked into spuffTeleport.");			
//		}
//	}
//
	public static void log(String in) {
		System.out.println("[" + pluginName + "] " + in);
	}
	public void log(int in) {
		log(String.valueOf(in));
	}

	@EventHandler public void onBlockBreakEvent(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
//		if(block.getType()!=Material.SIGN_POST) { return; }
		if(!(block.getState() instanceof Sign)) { return; }
		if(player.getItemInHand().getType()!=Material.BOOK) { return; }
		Sign sign = (Sign) block.getState();
		if(!(sign.getLine(0).equals("magic"))) { return; }

		int depth;
		try {
			depth = Integer.parseInt(sign.getLine(2));
		} catch(NumberFormatException e) {
			player.sendMessage("Pfft!");
			block.getWorld().createExplosion(block.getLocation(), 0);
			return;
		}

		String pile = "";

		for(int i=0; i<depth; i++) {
			pile = pile + block.getRelative(0, i+1, 0).getTypeId()+ " ";
		}

		String author = "ScrollBook";
		List<String> pages = new ArrayList<String>();
		String title = "";
		
		String spell = pile.trim();
		if(!getConfig().contains("spells."+spell)) {
			player.sendMessage("ZKXY!");
			block.getWorld().createExplosion(block.getLocation(), 0);
			return;
		}

		////Spell creation permissions:
		//		if((getConfig().contains("spells."+spell+".permission")) &&
		//				(!player.hasPermission("scrollbooks.makeScroll."+getConfig().getString("spells."+spell+".permission")))) {
		//			return;
		//		}

		title = this.getConfig().getString("spells."+spell+".title");

		//Start Pages processing
		//process Info page
		if(getConfig().contains("spells."+spell+".pages.info")) {
			String page = getConfig().getString("spells."+spell+".pages.info");
			pages.add(page);
		}// else { log("Cannot find info page!"); }
		
		//process Defaults page
		if(getConfig().contains("spells."+spell+".pages.global")) {
			Set<String> argKeys = getConfig().getConfigurationSection("spells."+spell+".pages.global").getKeys(false);
			if(argKeys.size()>0) {
				String page = "componentNumber=globals";
				for(String argKey:argKeys){
					getConfig().getString("spells."+spell+".pages.global."+argKey);
					page = page+"; "+argKey + "=" + getConfig().getString("spells."+spell+".pages.global."+argKey);
				}
				pages.add(page);
			}
		}// else { log("Cannot find globals page!"); }
		
		//process numbered pages
		if(getConfig().contains("spells."+spell+".pages")){
			Set<String> commandKeyStrings = getConfig().getConfigurationSection("spells."+spell+".pages").getKeys(false);
			List<String> commandKeys = new ArrayList<String>();
			for(String commandKey: commandKeyStrings){
				try{
					Integer.parseInt(commandKey);
					commandKeys.add(commandKey);
				}catch(NumberFormatException e){ }
			}
			Collections.sort(commandKeys);

			if(commandKeys.size()>0) {
				for(String commandKey: commandKeys){
					if(!getConfig().contains("spells."+spell+".pages."+commandKey+".command")) { continue; }
					String page = "componentNumber="+commandKey;
					Set<String> argKeys = this.getConfig().getConfigurationSection("spells."+spell+".pages."+commandKey).getKeys(false);
					if(argKeys.size()==0) { continue; }
					for(String argKey: argKeys){
						page = page+"; "+argKey + "=" + this.getConfig().get("spells."+spell+".pages."+commandKey+"."+argKey);
					}
					pages.add(page);
				}
			}
		}// else { log("Cannot find pages!"); }


		//end Pages processing

		if(title.equals("")) { return; }

		for(int i=0; i<depth; i++) {
			block.getRelative(0, i+1, 0).setType(Material.AIR);
		}

		if(!player.hasPermission("scrollBooks.admin.freeSpells")){
			if(player.getItemInHand().getAmount()==1) {
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
//				player.getItemInHand().setTypeId(0);
			} else {
				player.getItemInHand().setAmount(player.getItemInHand().getAmount()-1);
			}
		}

		block.setTypeId(0);
		event.setCancelled(true);

		Book book = new Book(title, author, pages);
		book.dropBook(block.getLocation());
	}

	@EventHandler public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction()!=Action.LEFT_CLICK_AIR && event.getAction()!=Action.LEFT_CLICK_BLOCK) { return; }
		ItemStack item = event.getPlayer().getItemInHand();
		if(!(item.getType().equals(Material.WRITTEN_BOOK))) { return; }

		event.setCancelled(true);

		Book scroll = new Book(item);
		OfflinePlayer author = server.getOfflinePlayer(scroll.getAuthor());

		if(     (scroll.getAuthor() == null)                          || //If the book has an author that isn't ScrollBook or an OP, then exit.
						(!(server.getOperators().contains(author)) &&
						!(author.getName().equals("ScrollBook")))         
						) { return; }

		List<Block> sightLine = event.getPlayer().getLineOfSight(null, 60);

		int scrollSize = scroll.pages.size();
		HashMap<Integer,String> componentMap = new HashMap<Integer,String>();
		
		int globalPageNo = 0;
		boolean hasCounter = false;
		for(int pageNo = 0; pageNo<scrollSize; pageNo++){
			String componentNumber = scroll.get("componentNumber", pageNo);
			if(componentNumber==null) { continue; }
			if(componentNumber.equalsIgnoreCase("globals")) { globalPageNo = pageNo; }
			componentMap.put(pageNo, componentNumber);
		}
		//need to set up the globals page.
		int dieChance = 0;
		if(globalPageNo>0) {
			if(scroll.getBoolean("requiresOp", globalPageNo) && !event.getPlayer().isOp()) { return; }
			if(scroll.getInt("usesRemaining", globalPageNo)>0) { hasCounter = true; }
			dieChance = scroll.getInt("dieChance", globalPageNo);
		}
		
		//begin page-by-page FOR loop here.
		for(int pageNo = 0; pageNo<scrollSize; pageNo++){
			String command = scroll.get("command", pageNo);
			if(command == null) { continue; }

			dieChance = (dieChance + scroll.getInt("dieChance", pageNo));
			int distance = scroll.getInt("distance", pageNo);
			int radius = scroll.getInt("radius", pageNo);
			String playerAlias = scroll.get("playerAlias", pageNo);
			if(playerAlias==null) { playerAlias = event.getPlayer().getName(); }

//			String title = scroll.getTitle();

			Location target;

			if(sightLine.size()>distance) {
				target = sightLine.get(distance).getLocation();
			} else {
				target = sightLine.get(sightLine.size()-1).getLocation();
			}
			
			
			/*	IF and GOTO commands. Not sure how I want this to go.
			 * 
			 * 	Should we do command=if; test=playerHasPermission; permission=some.permission; true=50; false=70
			 *		(meaning that we go to command ID 50 if player does have the permission, 70 otherwise)?
			 * 
			 * 			} else if(command.equalsIgnoreCase("if")) {
			 *              if(
							newFunc(scroll.getInt("targetPage", pageNo));
			*/
						
			
			if(command.equalsIgnoreCase("build")){
				build(playerAlias, target, scroll.get("schematic", pageNo), scroll.getInt("speed", pageNo));
			} else if(command.equalsIgnoreCase("SeaLevel")) {
				seaLevel(playerAlias, target, radius, scroll.getInt("volume", pageNo));
			} else if(command.equalsIgnoreCase("createHearthBook")) {
				createHearthBook(playerAlias, scroll.getInt("uses", pageNo));
			} else if(command.equalsIgnoreCase("teleport")) {
				teleportTo(playerAlias, scroll.get("world", pageNo), scroll.getFloat("x", pageNo), scroll.getFloat("y", pageNo), scroll.getFloat("z", pageNo));
			} else if(command.equalsIgnoreCase("fly")) {
				fly(playerAlias, scroll.getFloat("speed", pageNo));
			} else if(command.equalsIgnoreCase("permission")) {
				permission(playerAlias, scroll.get("permission", pageNo), scroll.getBoolean("value", pageNo), scroll.getInt("duration", pageNo));
			} else if(command.equalsIgnoreCase("applyPotionEffect")) {
				potionEffect(playerAlias, scroll.get("potionName", pageNo), scroll.getInt("duration", pageNo), scroll.getInt("amplifier", pageNo));
			} else if(command.equalsIgnoreCase("castPotionEffect")) {
				potionEffect(playerAlias, scroll.getInt("distance"), scroll.get("potionName", pageNo), scroll.getInt("duration", pageNo), scroll.getInt("amplifier", pageNo));
			} else if(command.equalsIgnoreCase("ChangeTotemFlag")) {
				changeTotemFlag(playerAlias, target, scroll.get("flag", pageNo), scroll.get("value", pageNo));
			} else if(command.equalsIgnoreCase("DryOut")) {
				dry(playerAlias, target, radius, scroll.getInt("volume", pageNo));
			} else if(command.equalsIgnoreCase("Fireball")) {
				fireball(playerAlias, target, scroll.getFloat("power", pageNo));
			} else if(command.equalsIgnoreCase("Spawner")) {
				placeSpawner(playerAlias, target, scroll.get("type", pageNo));
			} else if(command.equalsIgnoreCase("changeNearBlocks")) {
				convertBlocks(playerAlias, target, radius, scroll.get("from", pageNo), scroll.get("to"));
			} else if(command.equalsIgnoreCase("expandBlocks")) {
				expandBlocks(playerAlias, target, radius, scroll.getBoolean("vertical", pageNo), scroll.getBoolean("horizontal", pageNo), scroll.get("source"));
			} else if(command.equalsIgnoreCase("Dirtball")) {
				dirtBall(playerAlias, target, radius, scroll.getInt("duration", pageNo)); 
			} else if(command.equalsIgnoreCase("Give")) {
				give(playerAlias, scroll.get("type", pageNo), scroll.getInt("quantity", pageNo)); 
			} else if(command.equalsIgnoreCase("changeWeather")) {
				changeWeather(playerAlias);
			} else if(command.equalsIgnoreCase("setTime")) {
				setTime(playerAlias, scroll.getInt("timeOfDay", pageNo));
			} else if(command.equalsIgnoreCase("setWeather")) {
				changeWeather(playerAlias, scroll.getBoolean("isRaining", pageNo), scroll.getBoolean("isThundering", pageNo));
			} else if(command.equalsIgnoreCase("sendmessage")) {
				message(playerAlias, scroll.get("message"));
			} else if(command.equalsIgnoreCase("TeleportAway")) {
				teleportEntities(playerAlias, target, radius, scroll.getInt("tpdistance", pageNo),true);
			} else if(command.equalsIgnoreCase("TransformBiome")) {
				changeBiome(playerAlias, target, radius, Biome.valueOf(scroll.get("biome", pageNo)));
			} else {
				log("Unknown command: "+command);
			}

		}// End page-by-page for loop

		if(hasCounter) {
			reduceUse(event.getPlayer(), scroll, globalPageNo);
		} else {
			destroyChance(event.getPlayer(), dieChance);
		}
	}

//	private void goToTelePad(String playerAlias, String destination) {
//		if(spuffTeleport==null) { return; }
//		
//		spuffTeleport.teleportTo(playerAlias, destination, 0);
//		//TODO: Need to change to spuffTeleport.teleport(Player, Destination) to allow for permissions checks, warmups, cooldowns, etc.
//	}
//
//	private void setTelePadDestination(String playerAlias, Location target, String destination) {
//		if(spuffTeleport==null) { return; }
//		
//		spuffTeleport.setDestination(playerAlias, target.add(0, 1, 0), destination);
//		//TODO: Need to change to spuffTeleport.teleport(Player, Destination) to allow for permissions checks, warmups, cooldowns, etc.
//	}
//
//	private void createTelePad(String playerAlias, Location target, String targetName) {
//		if(spuffTeleport==null) { return; }
//
//		if(!canEdit(playerAlias, server.getPlayer(playerAlias).getLocation(), "place")) { log("no place"); return; }
//				
//		String serial = spuffTeleport.createTelepad(target, playerAlias, targetName);
//		
//		if(targetName==null) {
//			List<String> pages = new ArrayList<String>();
//
//			pages.add("Link a telepad to the telepad named "+serial);
////			pages.add("componentNumber=globals; dieChance=101");
//			pages.add("componentNumber=10; command=settelepaddestination; distance=10; destination="+serial);
//
//			Book scroll = new Book("Link a Telepad", "ScrollBook", pages);
//			scroll.dropBook(server.getPlayer(playerAlias).getLocation());
//		}
//
//		//TODO: Need to change to spuffTeleport.teleport(Player, Destination) to allow for permissions checks, warmups, cooldowns, etc.
//	}

	private void expandBlocks(String playerAlias, Location target, int radius,
			boolean doVert, boolean doHoriz, String source) {
		
		String[] oldBlocksArray = source.split(",");
		if(oldBlocksArray.length==0) { return; }

		List<Integer> oldBlocks = new ArrayList<Integer>();
		for(int m=0; m<oldBlocksArray.length; m++){
			try{
				oldBlocks.add(Integer.parseInt(oldBlocksArray[m]));
			}catch(NumberFormatException e){}
		}
		if(oldBlocks.size()==0) { return; }

		int vRad = 0;
		int hRad = 0;
		
		if(doVert) { vRad = radius-1; }
		if(doHoriz) { hRad = radius-1; }
		
		Block block = target.getBlock();
		for(int j=0-vRad; j<=vRad; j++){
			for(int i=0-hRad; i<=hRad; i++){
				for(int k=0-hRad; k<=hRad; k++){
//					if(!canEdit(playerAlias, target.add(i,j,k), "break")) { continue; }
					Block nearSourceBlock = block.getRelative(i, j, k);
					if(!oldBlocks.contains(nearSourceBlock.getTypeId())) { continue; }
					for(int jD=0-vRad; jD<=vRad; jD++){
						for(int iD=0-hRad; iD<=hRad; iD++){
							for(int kD=0-hRad; kD<=hRad; kD++){
								Block nearDestBlock = nearSourceBlock.getRelative(iD, jD, kD);
								if(nearDestBlock.getTypeId()!=0) { continue; }
								if(!canEdit(playerAlias, target.add(iD,jD,kD), "place")) { continue; }
								nearDestBlock.setType(nearSourceBlock.getType());
							}
						}
					}
				}		
			}					
		}		

	}

	private void createHearthBook(String playerAlias, int uses) {
		Player player = server.getPlayer(playerAlias);
		if(player==null) { return; }
		
		List<String> pages = new ArrayList<String>();
		pages.add("Return to the location that this book was created at.");
		pages.add("componentNumber=globals; usesRemaining="+String.valueOf(uses));
		pages.add("componentNumber=10; command=teleport; world="+player.getWorld().getName()+"; pitch="+player.getLocation().getPitch()+"; yaw="+player.getLocation().getYaw()+"; x="+player.getLocation().getX()+"; y="+player.getLocation().getY()+"; z="+player.getLocation().getZ());

		Book scroll = new Book("Hearthstone", "ScrollBook", pages);
		scroll.dropBook(server.getPlayer(playerAlias).getLocation());
	}

	private void teleportTo(String playerAlias, String worldName, float x, float y, float z) {
		Player player = server.getPlayer(playerAlias);
		if(player==null) { return; }
		teleportTo(playerAlias,worldName,x,y,z,player.getLocation().getPitch(), player.getLocation().getYaw());
	}

	private void teleportTo(String playerAlias, String worldName, float x, float y, float z, float pitch, float yaw) {
		Player player = server.getPlayer(playerAlias);
		if(player==null) { return; }
		World world = server.getWorld(worldName);
		if(world==null) { return; }
		
		player.teleport(new Location(world, x,y,z, yaw,pitch));
	}

	private void setTime(String playerAlias, int time) {
		if(!canEdit(playerAlias, server.getPlayer(playerAlias).getLocation(), "magic")) { return; }
		server.getPlayer(playerAlias).getWorld().setTime(time);
	}

	private void fly(String playerAlias, Float speed) {
		Player player = server.getPlayer(playerAlias);
		player.setAllowFlight(true);
		player.setFlying(true);
		if(speed>1) { speed = 0f; log("Speed set too high. Ignoring."); }
		if(speed>0) {
			server.getPlayer(playerAlias).setFlySpeed(speed);
		} else {
			server.getPlayer(playerAlias).setFlySpeed(.1f);
		}
	}

	private void message(String playerAlias, String message) {
		Player player = server.getPlayer(playerAlias);
		player.sendMessage(message);
	}

	private void potionEffect(String playerAlias, int distance, String potionName,
			int duration, int amplifier) {
		Player player = server.getPlayer(playerAlias);
//		PotionEffect effect = new PotionEffect(PotionEffectType.getByName(potionName.toUpperCase()), duration, amplifier);

		//currently, the distance isn't used. Need to change that.
		
		Snowball snowball = player.launchProjectile(Snowball.class);
		snowball.setMetadata("potionEffectName", new FixedMetadataValue(this, potionName));
		snowball.setMetadata("potionEffectDuration", new FixedMetadataValue(this, duration));
		snowball.setMetadata("potionEffectAmplifier", new FixedMetadataValue(this, amplifier));
		snowball.setBounce(true);
	}
	
	@EventHandler
	public void spellHitEvent(ProjectileHitEvent event) {
		if(!event.getEntity().getType().equals(EntityType.SNOWBALL)) { return; }
		if(!event.getEntity().hasMetadata("potionEffectName")) { return; }
		
		List<Entity> nearEntities = event.getEntity().getNearbyEntities(2, 2, 2);
		if(nearEntities.size()==0) { return; }
		LivingEntity target = null;
		for(int i=0; i<nearEntities.size(); i++) {
			if(nearEntities.get(i) instanceof LivingEntity) {
				target = (LivingEntity) nearEntities.get(i);
				break;
			}
		}
		
		if(target==null) { return; }
		
		String potionName = event.getEntity().getMetadata("potionEffectName").get(0).asString();
		int duration = event.getEntity().getMetadata("potionEffectDuration").get(0).asInt();
		int amplifier = event.getEntity().getMetadata("potionEffectAmplifier").get(0).asInt();

		PotionEffect effect = new PotionEffect(PotionEffectType.getByName(potionName.toUpperCase()), duration, amplifier);
		target.addPotionEffect(effect);

	}

/*	private void permission(String playerAlias, String permString, boolean permValue) {
		permission(playerAlias,permString,permValue,-1);
	}
*/	
	private void permission(String playerAlias, String permString, boolean permValue, int seconds) {
		if(server.getPlayer(playerAlias) == null) { return; }
		
		Permissible player = (Permissible) server.getPlayer(playerAlias);
		
		if(seconds>0) {
			player.addAttachment(plugin, permString, permValue, (seconds*20));
		} else {
			player.addAttachment(plugin, permString, permValue);
		}
	}

	private void potionEffect(String playerAlias, String potionName, int duration, int amplifier) {
//		log(potionName.toUpperCase());
		PotionEffect effect = new PotionEffect(PotionEffectType.getByName(potionName.toUpperCase()), duration, amplifier);
		server.getPlayer(playerAlias).addPotionEffect(effect);
	}

	private void convertBlocks(String playerAlias, Location target, int radius, String fromBlocks, String toBlocks) {
		String[] oldBlocksArray = fromBlocks.split(",");
		if(oldBlocksArray.length==0) { return; }
		String[] newBlocksArray = toBlocks.split(",");
		if(newBlocksArray.length==0) { return; }

		List<Integer> oldBlocks = new ArrayList<Integer>();
		for(int m=0; m<oldBlocksArray.length; m++){
			try{
				oldBlocks.add(Integer.parseInt(oldBlocksArray[m]));
			}catch(NumberFormatException e){}
		}
		if(oldBlocks.size()==0) { return; }

		List<Integer> newBlocks = new ArrayList<Integer>();
		for(int m=0; m<newBlocksArray.length; m++){
			try{
				newBlocks.add(Integer.parseInt(newBlocksArray[m]));
			}catch(NumberFormatException e){}
		}
		while(oldBlocks.size()>newBlocks.size()) {
			newBlocks.add(0);
		}

		Block block = target.getBlock();
		for(int j=0-radius; j<=radius; j++){
			for(int i=0-radius; i<=radius; i++){
				for(int k=0-radius; k<=radius; k++){
					if(!canEdit(playerAlias, target.add(i,j,k), "break")) { continue; }
					Block nearBlock = block.getRelative(i, j, k);
					if(oldBlocks.contains(nearBlock.getTypeId())) {
						nearBlock.setTypeId(newBlocks.get(oldBlocks.indexOf(nearBlock.getTypeId())));
					}
				}		
			}					
		}		
	}

	private void changeWeather(String playerAlias){
		Player player = server.getPlayer(playerAlias);
		if(player==null){ return; }
		if(!canEdit(playerAlias, player.getLocation(), "magic")) { return; }
		changeWeather(playerAlias, !player.getWorld().hasStorm(), false);

	}

	private void changeWeather(String playerAlias, boolean raining, boolean thundering){
		Player player = server.getPlayer(playerAlias);
		if(player==null){ return; }
		if(!canEdit(playerAlias, player.getLocation(), "magic")) { return; }
		player.getWorld().setStorm(raining);
		player.getWorld().setThundering(thundering);
	}

	private boolean canEdit(String playerName, Location loc, String flag) {
		if(totems == null) { return true; }
		if(server.getPlayer(playerName)!=null){
			return totems.canEdit(server.getPlayer(playerName), loc, flag.toUpperCase());			
		} else {
			return totems.canEdit(playerName, loc, flag.toUpperCase());
		}
	}

	private void changeTotemFlag(String playerAlias, Location target, String flag, String value) {
		//public boolean setTotemFlag(Location loc, Player player, String flag, String sValue) {
		//totem will catch this on its end, if it needs to.
		if(totems==null) {return;}
		totems.setTotemFlag(target, server.getPlayer(playerAlias), flag, value);
	}

	private void placeSpawner(String playerAlias, Location target, String type) {
		if(!canEdit(playerAlias, target, "place")) { return; }
		log(type);
		Block block = target.getWorld().getBlockAt(target); 
		block.setTypeId(52);
		CreatureSpawner cs = (CreatureSpawner) block.getState();
		cs.setSpawnedType(EntityType.fromName(type.toUpperCase()));

	}

	private void seaLevel(String playerAlias, Location target, int radius, int volume) {
		for(int y=(1-radius); y<=0; y++){
			for(int x=(1-radius); x<radius; x++){
				for(int z=(1-radius); z<radius; z++){
					if(!canEdit(playerAlias, target.add(x, y, z), "place")) { continue; }
					Block curBlock = target.getBlock().getRelative(x,y,z);
					if((curBlock.getTypeId()==8) || (curBlock.getTypeId()==9)) {
						volume--;
						curBlock.setType(Material.WATER);
						if(volume<0) { return; }
					}
				}
			}
		}
	}

	private void dry(String playerAlias, Location target, int radius, int volume) {
		for(int y=3; y>=-2; y--){
			for(int x=(1-radius); x<radius; x++){
				for(int z=(1-radius); z<radius; z++){
					if(!canEdit(playerAlias, target.add(x, y, z), "break")) { continue; }
					Block curBlock = target.getBlock().getRelative(x,y,z);
					if((curBlock.getTypeId()==9) || (curBlock.getTypeId()==8)) {
						if(curBlock.getData()==0) {
							volume--;
						}
						curBlock.setType(Material.AIR);
						if(volume<0) { return; }
					}
				}
			}
		}
	}

	private void build(String playerAlias, Location target, String name, int speed) {
		log(name);
		int i=0;
		Schematic schematic;
		try{
			String slash = File.separator;
			schematic = new Schematic(plugin.getDataFolder()+slash+"schematics"+slash+name+".schematic");
		} catch (IOException e) { return; }
		for(int y=0; y<schematic.getHeight(); y++){
			for(int x=0; x<schematic.getWidth(); x++){
				for(int z=0; z<schematic.getLength(); z++){
					if(!canEdit(playerAlias, target.add(x, y, z), "place")) { continue; }
					final Material mBlock = schematic.getBlock(x, y, z);
					final Block tBlock = target.getBlock().getRelative(x+schematic.offsetX, y+schematic.offsetY, z+schematic.offsetZ);
					final byte dBlock = schematic.getData(x, y, z);
					//					tBlock.setType(mBlock);
					if((tBlock.getType() != mBlock)) {
						i++;
						this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
							public void run() {
								placeBlock(tBlock, mBlock, dBlock);
							}
						}, (long) speed*i);
					}
				}
			}
		}
	}

	private void placeBlock(Block block, Material mat, byte data) {
		block.setTypeIdAndData(mat.getId(),data,false);
	}

	private void give(String playerAlias, String type, int quantity) {
		Material material;
		try{
			//This means we can use ID or Name.
			material = Material.getMaterial(Integer.parseInt(type));
		} catch (NumberFormatException e) {
			try{
				material = Material.getMaterial(type.toUpperCase());
			} catch (NullPointerException npe) {
				log("Bad material name/ID: " + type);
				return;
			}
		}

		ItemStack item = new ItemStack(material, quantity);

		server.getPlayer(playerAlias).getWorld().dropItemNaturally(server.getPlayer(playerAlias).getLocation(), item);
	}

	private void changeBiome(String playerAlias, Location location, int radius, Biome biome){

		for(int i=(0-radius); i<=radius; i++){
			for(int k=(0-radius); k<=radius; k++){
				if(!canEdit(playerAlias, location.add(i,0,k), "place")) { continue; }
				location.getBlock().getRelative(i, 0, k).setBiome(biome);
			}
		}

		int x = location.getChunk().getX();
		int z = location.getChunk().getZ();

		location.getWorld().refreshChunk(x, z);
	}

	private void teleportEntities(String playerAlias, Location location, int radius, int distance, boolean stayInWorld) {

		if(!canEdit(playerAlias, location, "magic")) { return; }

		//Let's make a list of nearby entities and how far away they are.
		//Vector is a handy type to use for the distance/direction they are from here.
		final HashMap<LivingEntity,Vector> nearEntities = new HashMap<LivingEntity,Vector>();

		//Now let's populate it.
		Entity[] entities = location.getChunk().getEntities();
		//TODO: We also need to check nearby chunks, to see if they're in the same area.
		for(Entity entity : entities) {
			if(entity instanceof LivingEntity){
				if(entity.getLocation().distance(location)<radius)
					nearEntities.put((LivingEntity) entity, location.subtract(entity.getLocation()).toVector());
			}
		}

		int x = (int) (Math.random()*2*distance)-distance;
		int z = (int) (Math.random()*2*distance)-distance;
		//get topBlock at block.add(x,z)

		Location destination = location.getWorld().getHighestBlockAt(location.add(x, 0, z)).getLocation();

		Set<Entry<LivingEntity, Vector>> nearEntitiesSet = nearEntities.entrySet();
		for(Entry<LivingEntity, Vector> entity : nearEntitiesSet) {
			entity.getKey().teleport(destination.add(entity.getValue()));
		}

	}

	private void dirtBall(String playerAlias, Location location, int radius, int ticks) {
		Block block = location.getBlock();
		final ArrayList<Block> airBlocks = new ArrayList<Block>();
		for(int i=(1-radius); i<radius; i++){
			for(int j=(1-radius); j<radius; j++){
				for(int k=(1-radius); k<radius; k++){
					if(!canEdit(playerAlias, location.add(i,j,k), "place")) { continue; }
					if(Math.sqrt((i*i)+(j*j)+(k*k)) < radius){
						Block affectedBlock = block.getRelative(j, i, k);
						if(affectedBlock.getTypeId()==0) {
							airBlocks.add(affectedBlock);
							affectedBlock.setType(Material.DIRT);
						}						
					}
				}
			}
		}
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				unDirtBall(airBlocks);
			}
		}, (long) ticks);
	}

	private void unDirtBall(ArrayList<Block> dirtBlocks) {
		for(Block b : dirtBlocks) {
			b.setType(Material.AIR);
		}
	}
	
	private void reduceUse(Player player, Book scroll, int pageNo) {
		int uses = scroll.getInt("usesRemaining", pageNo);
		if(uses<2) {
			destroyChance(player, 80);
		} else {
			scroll.setKey("usesRemaining", uses-1, pageNo);
			player.setItemInHand(scroll.toItemStack());
		}
	}

	private void destroyChance(Player player, int dieChance) {
		if((Math.random()*100)<dieChance) {
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
		}
	}

	private void fireball(String playerAlias, Location target, float power) {
		//totem will catch this on it's side, if it's installed.
		target.getWorld().createExplosion(target, power, true);
		//		target.getWorld().createExplosion(target, power, true);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("scrollbooksreload")){
			this.reloadConfig();
			log("Config reloaded.");
			return true;
		}
		return false; 
	}

}
