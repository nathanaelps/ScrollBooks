package me.nathanaelps.plugins.scrollbooks;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;

public class Utils {

	public static void log(String in) {
		System.out.println("[" + ScrollBooks.pluginName + "] " + in);
	}
	public void log(Object in) {
		log(String.valueOf(in));
	}
		
	public static Material getMaterial(String in) {
		Material out = Material.AIR;
		try{
			out = Material.getMaterial(Integer.valueOf(in));
		} catch(NumberFormatException e) {
			out = Material.matchMaterial(in);
		}
		if(out==null) {
			log("Couldn't find material: "+in);
			return Material.AIR;
		}
		return out;
	}
	
	public static List<Material> getMaterialList(String csvBlocks) {
		List<Material> out = new ArrayList<Material>();
		
		String[] oldBlocksArray = csvBlocks.split(",");
		if(oldBlocksArray.length==0) { return out; }

		for(int m=0; m<oldBlocksArray.length; m++){
			out.add(Utils.getMaterial(oldBlocksArray[m]));
		}

		return out;
	}

	public static List<Entity> getNearbyEntities(Location loc, double x, double y, double z) {
		FallingBlock ent = loc.getWorld().spawnFallingBlock(loc, 132, (byte) 0);
		List<Entity> out = ent.getNearbyEntities(x, y, z);
		ent.remove();
		
		return out;
	}
	
	public static List<Entity> getNearbyEntities(Location loc, double r) {
		return getNearbyEntities(loc, r,r,r);
	}
	
	public static List<LivingEntity> getNearbyLivingEntities(Location loc, double r) {
		List<Entity> ents = getNearbyEntities(loc, r,r,r);
		List<LivingEntity> out = new ArrayList<LivingEntity>();
		
		for(Entity ent : ents){
			if(ent instanceof LivingEntity) { out.add((LivingEntity) ent); }
		}
		
		return out;
	}
	
	public static List<LivingEntity> getLivingEntities(List<Entity> ents) {
		List<LivingEntity> out = new ArrayList<LivingEntity>();
		
		for(Entity ent : ents){
			if(ent instanceof LivingEntity) { out.add((LivingEntity) ent); }
		}
		
		return out;
	}

}
