package me.nathanaelps.plugins.scrollbooks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

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

}
