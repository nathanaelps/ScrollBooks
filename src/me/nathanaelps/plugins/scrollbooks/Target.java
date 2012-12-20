package me.nathanaelps.plugins.scrollbooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;

public class Target {
	
	private List<Block> sightLine; // = event.getPlayer().getLineOfSight(null, 60);

	public Location loc = null;
	
	public Block source = null;
	public Block block = null;
	public Block liquid = null;
	public Block transparent = null;
	public Block solidBlock = null;

	public Entity entity = null;
	public LivingEntity livingEntity = null;
	
	Target(LivingEntity entity){
		getLineOfSight(entity);
	}
	
    private void getLineOfSight(LivingEntity entity) {
        int maxDistance = 120; //higher than 140 can interfere with unloaded chunks. At least the bukkit docs say so.
        ArrayList<Block> blocks = new ArrayList<Block>();
        Iterator<Block> itr = new BlockIterator(entity, maxDistance);
        Block prevBlock = null;
//        World world = entity.getWorld();
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            loc = block.getLocation();
            if(solidBlock!=null) { break; } //if(lastTransparentBlock!=null) { break; }
            if(block.getType().isSolid()) {
            	solidBlock = block;
            	transparent = prevBlock;
            }
            prevBlock = block;
            if(liquid!=null) { continue; }
            if(block.isLiquid()) { liquid = block; }
           	if(this.block!=null) { continue; }
           	if(!block.isEmpty()) { this.block = block; }
        }
        sightLine.addAll(blocks);
    }
	
}
