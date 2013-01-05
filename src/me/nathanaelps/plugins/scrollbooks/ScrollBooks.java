package me.nathanaelps.plugins.scrollbooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
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
import org.bukkit.potion.PotionType;
import com.thespuff.plugins.totems.Totems;

public class ScrollBooks extends JavaPlugin implements Listener {

	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static ScrollBooks plugin;
	public static Totems totems;

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
	public void log(Object in) {
		log(String.valueOf(in));
	}

	@EventHandler public void onMakeScroll(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
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
		}
		
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
		}
		
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
		}


		//end Pages processing

		if(title.equals("")) { return; }

		for(int i=0; i<depth; i++) {
			block.getRelative(0, i+1, 0).setType(Material.AIR);
		}

		if(!player.hasPermission("scrollBooks.admin.freeSpells")){
			if(player.getItemInHand().getAmount()==1) {
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
			} else {
				player.getItemInHand().setAmount(player.getItemInHand().getAmount()-1);
			}
		}

		block.setTypeId(0);
		event.setCancelled(true);
		
		Book book = new Book(title, author, pages);
		log(book.getAuthor());
		book.dropBook(block.getLocation());
	}

	@EventHandler public void onUseScroll(PlayerInteractEvent event) {
		if(event.getAction()!=Action.LEFT_CLICK_AIR && event.getAction()!=Action.LEFT_CLICK_BLOCK) { return; }
		ItemStack item = event.getPlayer().getItemInHand();
		if(!(item.getType().equals(Material.WRITTEN_BOOK))) { return; }

		event.setCancelled(true);

		Book s = new Book(item);
		String authorName = s.getAuthor();
		if(authorName==null || authorName.length()<1) { authorName="ScrollBook"; }
		OfflinePlayer author = server.getOfflinePlayer(authorName);

		if(!(server.getOperators().contains(author)) &&
				!(author.getName().equals("ScrollBook")))         
		{ return; }


		int scrollSize = s.size();
		HashMap<Integer,String> componentMap = new HashMap<Integer,String>();
		
		int globalPageNo = 0;
		boolean hasCounter = false;
		for(int pageNo = 1; pageNo<=scrollSize; pageNo++){
			String comNo = s.get("no", pageNo); //Component Number
			if(comNo==null) { comNo = s.get("comNo", pageNo); }
			if(comNo==null) { comNo = s.get("componentNumber", pageNo); }
			if(comNo==null) { continue; }
			if(comNo.equalsIgnoreCase("globals")) { globalPageNo = pageNo; }
			else if(comNo.equalsIgnoreCase("global")) { globalPageNo = pageNo; }
			componentMap.put(pageNo, comNo);
		}
		//need to set up the globals page.
		int dieChance = 0;
		boolean checkEntities = false;
		if(globalPageNo>0) {
			if(s.getBoolean("requiresOp", globalPageNo) && !event.getPlayer().isOp()) { return; }
			if(s.getInt("usesRemaining", globalPageNo)>0) { hasCounter = true; }
			if(s.getBoolean("checkEntities", globalPageNo)) { checkEntities = true; }
			dieChance = s.getInt("dieChance", globalPageNo);
		}
		
		Target target = new Target(event.getPlayer(), checkEntities);
		
		//begin page-by-page FOR loop here.
		for(int pageNo = 1; pageNo<=scrollSize; pageNo++){
			int p = pageNo;
			String command = s.get("command", pageNo);
			if(command == null) { continue; }

			dieChance = (dieChance + s.getInt("dieChance", pageNo));
			target.setDistance(s.getInt("distance", pageNo));
			
			int radius = s.getInt("radius", pageNo);
			String plr = s.get("playerAlias", pageNo);
			if(plr==null) { plr = event.getPlayer().getName(); }

			if(command.equalsIgnoreCase("comment")){
				//Do nothing.
			} else if(command.equalsIgnoreCase("createFloor")) {
				blockFill(plr, target.getTransparent(), radius, s.getBoolean("isCeiling", pageNo), true, s.get("to", pageNo));
			} else if(command.equalsIgnoreCase("createHearthBook")) {
				createHearthBook(plr, s.getInt("avgUses", pageNo));
			} else if(command.equalsIgnoreCase("effect")) {
				String effect = s.get("effect", pageNo);
				
				if(effect.equalsIgnoreCase("explosion")) {
					
				} else if(effect.equalsIgnoreCase("potion")) {
					potionEffect(plr, target.getLivingEntity(), s.get("potion", p));
				} else if(effect.equalsIgnoreCase("lightning")) {
					lightningEffect(plr, target.getLoc());
				} else if(effect.equalsIgnoreCase("visual")) {
					visualEffect(plr, target.getTransparent(), s.get("visual", p));
				}

			} else if(command.equalsIgnoreCase("teleport")) {
				teleportTo(plr, s.get("world", pageNo), s.getFloat("x", pageNo), s.getFloat("y", pageNo), s.getFloat("z", pageNo));
			} else if(command.equalsIgnoreCase("fly")) {
				fly(plr, s.getFloat("speed", pageNo));
			} else if(command.equalsIgnoreCase("displayMessage")) {
				longMessage(plr, s.get("message", pageNo));
			} else if(command.equalsIgnoreCase("permission")) {
				permission(plr, s.get("permission", pageNo), s.getBoolean("value", pageNo), s.getInt("duration", pageNo));
			} else if(command.equalsIgnoreCase("applyPotionEffect")) {
				potionEffect(plr, s.get("potionName", pageNo), s.getInt("duration", pageNo), s.getInt("amplifier", pageNo));
			} else if(command.equalsIgnoreCase("castPotionEffect")) {
				potionEffect(plr, s.getInt("distance"), s.get("potionName", pageNo), s.getInt("duration", pageNo), s.getInt("amplifier", pageNo));
			} else if(command.equalsIgnoreCase("ChangeTotemFlag")) {
				changeTotemFlag(plr, target.getSolidBlock(), s.get("flag", pageNo), s.get("value", pageNo));
			} else if(command.equalsIgnoreCase("Spawner")) {
				placeSpawner(plr, target.getTransparent(), s.get("type", pageNo));
			} else if(command.equalsIgnoreCase("changeNearBlocks")) {
				convertBlocks(plr, target.getSolidBlock(), radius, s.get("from", pageNo), s.get("to"));
			} else if(command.equalsIgnoreCase("Dirtball")) {
				dirtBall(plr, target.getLoc(), radius, s.getInt("duration", pageNo)); 
			} else if(command.equalsIgnoreCase("Give")) {
				give(plr, s.get("type", pageNo), s.getInt("quantity", pageNo)); 
			} else if(command.equalsIgnoreCase("changeWeather")) {
				changeWeather(plr);
			} else if(command.equalsIgnoreCase("setTime")) {
				setTime(plr, s.getInt("timeOfDay", pageNo));
			} else if(command.equalsIgnoreCase("setWeather")) {
				changeWeather(plr, s.getBoolean("isRaining", pageNo), s.getBoolean("isThundering", pageNo));
			} else if(command.equalsIgnoreCase("sendmessage")) {
				message(plr, s.get("message"));
			} else if(command.equalsIgnoreCase("TeleportAway")) {
				teleportEntities(plr, target.getLoc(), radius, s.getInt("tpdistance", pageNo));
			} else if(command.equalsIgnoreCase("TransformBiome")) {
				changeBiome(plr, target.getSolidBlock(), radius, Biome.valueOf(s.get("biome", pageNo).toUpperCase()), (double) s.getFloat("mottle", pageNo));
			} else if(command.equalsIgnoreCase("blockFill")) {
				blockFill(plr, target.getSolidBlock(), radius, s.getBoolean("isCeiling", pageNo), s.getBoolean("isFloor", pageNo), s.get("to", pageNo));
			} else {
				log("Unknown command: "+command);
			}

		}// End page-by-page for loop

		if(hasCounter) {
			reduceUse(event.getPlayer(), s, globalPageNo);
		} else {
			destroyChance(event.getPlayer(), dieChance);
		}
	}
	
	private void longMessage(String playerAlias, String message) {
		Player player = server.getPlayer(playerAlias);
		if(player==null) { return; }
	}

	private void visualEffect(String player, Block block, String effectName) {
		if(!canEdit(player, block, "magic")) { return; }
		Effect effect;
		Object data; 
		if(effectName.equalsIgnoreCase("flames")) {
			effect = Effect.MOBSPAWNER_FLAMES;
			data = null;
		} else if(effectName.equalsIgnoreCase("potion")) {
			effect = Effect.POTION_BREAK;
			data = PotionType.REGEN;
		} else if(effectName.equalsIgnoreCase("ender")) {
			effect = Effect.POTION_BREAK;
			data = null;
		} else if(effectName.equalsIgnoreCase("smoke")) {
			effect = Effect.POTION_BREAK;
			data = BlockFace.UP;
		} else {
			return;
		}
		final Location rT = block.getLocation();
		final Effect rE = effect;
		final Object rD = data;
		Runnable task = new Runnable() { public void run() {rT.getWorld().playEffect(rT, rE, rD);} };
		final int killSwitch = server.getScheduler().scheduleSyncRepeatingTask(plugin, task, 10, 10);
		Runnable kill = new Runnable() { public void run() { server.getScheduler().cancelTask(killSwitch); } };
		server.getScheduler().runTaskLater(plugin, kill, 300);
	}

	private void lightningEffect(String player, Location target){
		if(!canEdit(player, target, "magic")) { return; }
		target.getWorld().strikeLightningEffect(target);
	}
		
	private void potionEffect(String player, LivingEntity livingEntity, String potion) {
		if(!canEdit(player, livingEntity.getLocation(), "magic")) { return; }
		
		FallingBlock loc = livingEntity.getWorld().spawnFallingBlock(livingEntity.getLocation(), 36, (byte) 0);
		List<Entity> nearEntities = loc.getNearbyEntities(2, 2, 2);
		loc.remove();
		
		if(nearEntities.size()==0) { return; }
		LivingEntity victim = null;
		for(int i=0; i<nearEntities.size(); i++) {
			if(nearEntities.get(i) instanceof LivingEntity) {
				victim = (LivingEntity) nearEntities.get(i);
				break;
			}
		}
		
		if(victim==null) { return; }
		
		PotionEffect effect = new PotionEffect(PotionEffectType.getByName(potion.toUpperCase()), 300, 1);
		victim.addPotionEffect(effect);
	}

	private void blockFill(String playerAlias, Block block, int radius, boolean isCeiling, boolean isFloor, String changeTo){
		Selection selection = new Selection(block);
		selection.contiguousCubicVolume(radius);
		if(isCeiling) { selection.removeBelow(block.getY()); }
		if(isFloor) { selection.removeAbove(block.getY()); }
		
		Material dest = Utils.getMaterial(changeTo);
		
		selection.to(dest);
	}
	
	private void createHearthBook(String playerAlias, int uses) {
		Player player = server.getPlayer(playerAlias);
		if(player==null) { return; }
		
		List<String> pages = new ArrayList<String>();
		pages.add("Return to the location that this book was created at.");
		pages.add("componentNumber=globals; dieChance="+String.valueOf(uses));
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
		PotionEffect effect = new PotionEffect(PotionEffectType.getByName(potionName.toUpperCase()), duration, amplifier);
		server.getPlayer(playerAlias).addPotionEffect(effect);
		//PotionEffectType.
	}

	private void convertBlocks(String playerAlias, Block block, int radius, String fromBlocks, String toBlocks) {

		List<Material> oldBlocks = Utils.getMaterialList(fromBlocks);
		List<Material> newBlocks = Utils.getMaterialList(toBlocks);
		
		while(oldBlocks.size()>newBlocks.size()) {
			newBlocks.add(Material.AIR);
		}

		Selection selection = new Selection(block);
		selection.outset(radius);
		
		Iterator<Material> oldIt = oldBlocks.iterator();
		Iterator<Material> newIt = newBlocks.iterator();
		
		while(oldIt.hasNext()){
			selection.convert(oldIt.next(), newIt.next());
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

	private boolean canEdit(String playerName, Location location, String flag) {
		return canEdit(playerName, location.getBlock(), flag);
	}

	private boolean canEdit(String playerName, Block block, String flag) {
		if(totems == null) { return true; }
		if(server.getPlayer(playerName)!=null){
			return totems.canEdit(server.getPlayer(playerName), block, flag.toUpperCase());			
		} else {
			return totems.canEdit(playerName, block, flag.toUpperCase());
		}
	}

	private void changeTotemFlag(String playerAlias, Block block, String flag, String value) {
		//public boolean setTotemFlag(Location loc, Player player, String flag, String sValue) {
		//totem will catch this on its end, if it needs to.
		if(totems==null) {return;}
		totems.setTotemFlag(block.getLocation(), server.getPlayer(playerAlias), flag, value);
	}

	private void placeSpawner(String playerAlias, Block block, String type) {
		if(!canEdit(playerAlias, block, "place")) { return; }
		block.setTypeId(52);
		CreatureSpawner cs = (CreatureSpawner) block.getState();
		cs.setSpawnedType(EntityType.fromName(type.toUpperCase()));

	}

//						this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
//							public void run() {
//								placeBlock(tBlock, mBlock, dBlock);
//							}
//						}, (long) speed*i);
//
//	private void placeBlock(Block block, Material mat, byte data) {
//		block.setTypeIdAndData(mat.getId(),data,false);
//	}

	private void give(String playerAlias, String type, int quantity) {
		Material material = Utils.getMaterial(type);

		ItemStack item = new ItemStack(material, quantity);

		server.getPlayer(playerAlias).getWorld().dropItemNaturally(server.getPlayer(playerAlias).getLocation(), item);
	}

	/**
	 * Changes the Biome in a square radius of central point.
	 * Note the mottle parameter, which permits the old biome to remain in areas.
	 * The higher the mottle parameter is, the less the biome will be changed.
	 * @param playerAlias Whose permissions to check.
	 * @param solidBlock Location of center.
	 * @param radius Radius to change, in blocks.
	 * @param biome Biome to change to.
	 * @param mottle The chance (between 0 and 1) that the biome will not be changed.
	 */
	private void changeBiome(String playerAlias, Block block, int radius, Biome biome, double mottle){
		Location solidBlock = block.getLocation();
		Block startBlock = solidBlock.getBlock();
		solidBlock.add(0-radius, 0, 0-radius);
//		log(mottle);
		
		for(int i=(0-radius); i<=radius; i++){
			for(int k=(0-radius); k<=radius; k++){
				if(Math.random()>mottle) {
					Block newBlock = solidBlock.getBlock();
					if(!canEdit(playerAlias, newBlock.getLocation(), "place")) { continue; }
					newBlock.setBiome(biome);
				}
				solidBlock.add(0,0,1);
			}
			solidBlock.add(1,0,-1-(2*radius));
		}

		int x = startBlock.getChunk().getX();
		int z = startBlock.getChunk().getZ();
		startBlock.getWorld().refreshChunk(x, z);

		solidBlock = startBlock.getLocation();
	}

	private void teleportEntities(String playerAlias, Location target, int radius, int distance) {

		if(!canEdit(playerAlias, target, "magic")) { return; }

		List<Entity> ents = Utils.getNearbyEntities(target, radius, radius, radius);
		
		int xMod = 0;
		if(Math.random()<.5) { xMod=1; } else { xMod=-1; }
		int zMod = 0;
		if(Math.random()<.5) { zMod=1; } else { zMod=-1; }
		
		int x = xMod * (int) Math.random()*distance;
		int z = zMod * (int) Math.sqrt((distance*distance) + (x*x));

		Location destination = target.getWorld().getHighestBlockAt(target.add(x, 0, z)).getLocation();
		
		for(Entity ent : ents) {
			ent.teleport(destination);
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

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("scrollbooksreload")){
			this.reloadConfig();
			log("Config reloaded.");
			return true;
		}
		return false; 
	}

}
