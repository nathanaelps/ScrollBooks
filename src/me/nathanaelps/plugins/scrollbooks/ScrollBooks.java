package me.nathanaelps.plugins.scrollbooks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
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
    
	public static void log(String in) {
    	System.out.println("[" + pluginName + "] " + in);
    }
    public void log(int in) {
    	log(String.valueOf(in));
    }
    
    @EventHandler public void onBlockBreakEvent(BlockBreakEvent event) {
    	Block block = event.getBlock();
    	Player player = event.getPlayer();
    	if(block.getType()!=Material.SIGN_POST) { return; }
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
       		pile = pile + " " + block.getRelative(0, i+1, 0).getTypeId();
       	}

       	ConfigurationSection spellsConfig = this.getConfig().getConfigurationSection("spells");
       	Set<String> spells = spellsConfig.getKeys(false);
       	
    	String author = "ScrollBook";
    	List<String> pages = new ArrayList<String>();
       	String title = "";
       	
		for(String spell : spells) {
			if(pile.equals(" "+spell)) {
				if((getConfig().contains("spells."+spell+".permission")) &&
						(!player.hasPermission("scrollbooks.makeScroll."+getConfig().getString("spells."+spell+".permission")))) {
					return;
				}
				title = this.getConfig().getString("spells."+spell+".title");
				
				ConfigurationSection argumentConfig = this.getConfig().getConfigurationSection("spells."+spell+".page");
				Set<String> arguments = argumentConfig.getKeys(false);
				String page="title="+title;
				for(String argument: arguments){
					page = page+"; "+argument + "=" + this.getConfig().get("spells."+spell+".page."+argument);
				}
				pages.add(page);
				continue;
				
			}
		}
    	
		if(title.equals("")) { return; }
		
		for(int i=0; i<depth; i++) {
			block.getRelative(0, i+1, 0).setType(Material.AIR);
		}
		
		if(!player.hasPermission("scrollBooks.admin.freeSpells")){
			if(player.getItemInHand().getAmount()==1) {
				player.getItemInHand().setTypeId(0);
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
		
		if(     (scroll.getAuthor() == null)                          || //If the book has an author that isn't ScrollBook or op, then exit.
				(
						(!(scroll.getAuthor().equals("ScrollBook")))          
//						&& (!(server.getPlayer(scroll.getAuthor()).isOp()))
				)) { return; }
		
		int distance = scroll.getInt("distance");
		int radius = scroll.getInt("radius");
		String playerAlias = scroll.get("playerAlias");
		if(playerAlias==null) { playerAlias = event.getPlayer().getName(); }
		
		String title = scroll.getTitle();
		Location target = event.getPlayer().getTargetBlock(null, distance).getLocation();
		
		//TODO: set up the first page to be an "Info" page,
		//then set up each additoinal page to be a self-contained spell.
		//Spells will be cast in page order
		//Perhaps we could have some pages of GOTO? IDK. Make it a programmign language
		
		
		if(title.equals("Build")) {
			build(playerAlias, target, scroll.get("schematic"), scroll.getInt("speed"));
		}
		
		if(title.equals("SeaLevel")) {
			seaLevel(playerAlias, target, radius, scroll.getInt("volume"));
		}
		
		if(title.equals("ChangeTotemFlag")) {
			changeTotemFlag(playerAlias, target, scroll.get("flag"), scroll.get("value"));
		}
		
		if(title.equals("DryOut")) {
			dry(playerAlias, target, radius, scroll.getInt("volume"));
		}
		
		if(title.equals("Fireball")) {
			fireball(playerAlias, target, scroll.getFloat("power"));
		}
		
		if(title.equals("Spawner")) {
			placeSpawner(playerAlias, target, scroll.get("type"));
		}
		
		if(title.equals("Herbicide")) {
			convertBlocks(playerAlias, target, radius, scroll.get("kill"), "0");
			convertBlocks(playerAlias, target, radius, scroll.get("from"), scroll.get("to"));
		}
		
		if(title.equals("Dirtball")) {
			dirtBall(playerAlias, target, radius, scroll.getInt("duration")); 
		}

		if(title.equals("Give")) {
			give(playerAlias, scroll.get("type"), scroll.getInt("quantity")); 
		}

		if(title.equals("Oubliette")) {
			dig(playerAlias, target, scroll.getInt("depth"), radius);
		}
		
		if(title.equals("Thunderstorm")) {
			changeWeather(playerAlias, true, true);
		}
		
		if(title.equals("Teleport")) {
			teleportEntities(playerAlias, target, radius, scroll.getInt("tpdistance"),true);
		}

		if(title.equals("TransformBiome")) {
			changeBiome(playerAlias, target, radius, Biome.valueOf(scroll.get("biome")));
		}	
		
		destroy(event.getPlayer(), scroll);
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
		for(int i=0-radius; i<=radius; i++){
			for(int j=0-radius; j<=radius; j++){
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
		if(totems == null) { return false; }
		return totems.canEdit(playerName, loc, flag);
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
					if((curBlock.getTypeId()==8) || (curBlock.getTypeId()==9)) {
						volume--;
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

	private void dig(String playerAlias, Location location, int depth, int radius) {
		Block block = location.getBlock();
//		final HashMap<Block,Material> repairBlocks = new HashMap<Block,Material>();
		for(int i=(0-depth); i<1; i++){
			for(int j=(1-radius); j<radius; j++){
				for(int k=(1-radius); k<radius; k++){
					Block affectedBlock = block.getRelative(j, i, k);
					if(affectedBlock.getType().equals(Material.GRASS)) {
						affectedBlock.setType(Material.AIR);
					}
				}
			}
		}
	}
	
	
	private void changeBiome(String playerAlias, Location location, int radius, Biome biome){
		
		for(int i=(1-radius); i<radius; i++){
			for(int k=(1-radius); k<radius; k++){
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

	private void destroy(Player player, Book scroll) {
//		if(scroll.getInt("dieChance")>0) {
			if((Math.random()*100)<scroll.getInt("dieChance")) { player.setItemInHand(new ItemStack(Material.AIR)); }
//		} else if(scroll.getInt("usesRemaining")>0) {
//			scroll.setKey("usesRemaining", scroll.getInt("usesRemaining")-1);
//		} else {
//			player.setItemInHand(new ItemStack(Material.AIR));
//		}
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
