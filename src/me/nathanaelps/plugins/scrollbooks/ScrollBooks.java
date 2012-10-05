package me.nathanaelps.plugins.scrollbooks;

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
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ScrollBooks extends JavaPlugin implements Listener {
	
	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static ScrollBooks plugin;
	
    public void onDisable() {
    	log("Disabled");
    }

    public void onEnable() {
    	pluginName = this.getDescription().getName();
    	pluginVersion = this.getDescription().getVersion();
    	server = this.getServer();
    	plugin = this;
    	
    	getServer().getPluginManager().registerEvents(this, this);

    	log("Enabled.");
    }
    
	public static void log(String in) {
    	System.out.println("[" + pluginName + "] " + in);
    }
    public void log(int in) {
    	log(String.valueOf(in));
    }
    
    @EventHandler public void onBlockBreakEvent(BlockBreakEvent event) {
    	Block block = event.getBlock();
    	if(block.getType()!=Material.SIGN_POST) { return; }
    	if(event.getPlayer().getItemInHand().getType()!=Material.BOOK) { return; }
    	Sign sign = (Sign) block.getState();
       	if(!(sign.getLine(0).equals("magic"))) { return; }
       	
       	int depth;
       	try {
       		depth = Integer.parseInt(sign.getLine(2));
       	} catch(NumberFormatException e) {
       		event.getPlayer().sendMessage("Pfft!");
       		block.getWorld().createExplosion(block.getLocation(), 0);
       		return;
       	}
    	
       	String pile = "";
       	
       	for(int i=0; i<depth; i++) {
       		pile = pile + " " + block.getRelative(0, i+1, 0).getTypeId();
       	}

//       	log(pile);
       	
       	ConfigurationSection spellsConfig = this.getConfig().getConfigurationSection("spells");
       	Set<String> spells = spellsConfig.getKeys(false);
       	
    	String author = "ScrollBook";
    	List<String> pages = new ArrayList<String>();
       	String title = "";
       	
		for(String spell : spells) {
			if(pile.equals(" "+spell)) {
				title = this.getConfig().getString("spells."+spell+".title");
				
				ConfigurationSection argumentConfig = this.getConfig().getConfigurationSection("spells."+spell+".page");
				Set<String> arguments = argumentConfig.getKeys(false);
				for(String argument: arguments){
					pages.add(argument + " " + this.getConfig().get("spells."+spell+".page."+argument));
				}
				
				continue;
				
			}
		}
    	
		if(title.equals("")) { return; }
		
		for(int i=0; i<depth; i++) {
			block.getRelative(0, i+1, 0).setType(Material.AIR);
		}
		
    	try{
        	Book book = new Book(title, author, pages);
        	ItemStack bookItem;
    		bookItem = book.generateItemStack();
    		event.getPlayer().getInventory().addItem(bookItem);
    	}
    	catch(IllegalArgumentException e){log("Still illegal."); return;}
    }
    
	@EventHandler public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		if(!(item.getType().equals(Material.WRITTEN_BOOK))) { return; }
		Scroll scroll = new Scroll(item);
		
		if((scroll.getAuthor() == null) || (!(scroll.getAuthor().equals("ScrollBook")))) { return; }
		
		int distance = scroll.getInt("distance");
		int radius = scroll.getInt("radius");
		
		String title = scroll.getTitle();
		Location target = event.getPlayer().getTargetBlock(null, distance).getLocation();
		
		if(title.equals("Build")) {
			build(target, scroll.get("schematic"), scroll.getInt("speed"));
		}
		
		if(title.equals("SeaLevel")) {
			seaLevel(target, radius, scroll.getInt("volume"));
		}
		
		if(title.equals("Fireball")) {
			fireball(target, scroll.getFloat("power"));
		}
		
		if(title.equals("Dirtball")) {
			dirtBall(target, radius, scroll.getInt("duration")); 
		}

		if(title.equals("Give")) {
			give(event.getPlayer(), scroll.get("type"), scroll.getInt("quantity")); 
		}

		if(title.equals("Oubliette")) {
			dig(target, scroll.getInt("depth"), radius);
		}
		
		if(title.equals("Teleport")) {
			teleportEntities(target,radius,scroll.getInt("tpdistance"),true);
		}

		if(title.equals("TransformBiome")) {
			changeBiome(target,radius,Biome.valueOf(scroll.get("biome")));
		}	
		
		destroy(event.getPlayer(), scroll.getInt("dieChance"));
	}

	private void seaLevel(Location target, int radius, int volume) {
		for(int x=(1-radius); x<radius; x++){
			for(int y=(1-radius); y<=0; y++){
				for(int z=(1-radius); z<radius; z++){
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

	private void build(Location target, String name, int speed) {
		int i=0;
		Schematic schematic;
		try{
			schematic = new Schematic(name);
		} catch (IOException e) { return; }
		for(int y=0; y<schematic.getHeight(); y++){
			for(int x=0; x<schematic.getWidth(); x++){
				for(int z=0; z<schematic.getLength(); z++){
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
	
	private void give(Player player, String type, int quantity) {
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
		
		player.getWorld().dropItemNaturally(player.getLocation(), item);
	}

	private void dig(Location location, int depth, int radius) {
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
	
	
	private void changeBiome(Location location, int radius, Biome biome){
		for(int i=(1-radius); i<radius; i++){
			for(int j=(1-radius); j<radius; j++){
				location.getBlock().getRelative(i, 0, j).setBiome(biome);
			}
		}
		
		int x = location.getChunk().getX();
		int z = location.getChunk().getZ();
		
		location.getWorld().refreshChunk(x, z);
	}
	
	private void teleportEntities(Location location, int radius, int distance, boolean stayInWorld) {
		
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
	
	private void dirtBall(Location location, int radius, int ticks) {
		Block block = location.getBlock();
		final ArrayList<Block> airBlocks = new ArrayList<Block>();
		for(int i=(1-radius); i<radius; i++){
			for(int j=(1-radius); j<radius; j++){
				for(int k=(1-radius); k<radius; k++){
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

	private void destroy(Player player, int chance) {
		if((Math.random()*100)<chance) { player.setItemInHand(new ItemStack(Material.AIR)); }
	}

	private void fireball(Location target, float power) {
		target.getWorld().createExplosion(target, power, true);
		target.getWorld().createExplosion(target, power, true);
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
