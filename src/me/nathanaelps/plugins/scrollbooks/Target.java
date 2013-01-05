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

	enum TargetType{
		LOCATION,
		
		SOURCE,
		DESTINATION,
		
		BLOCK,
		LIQUID,
		TRANSPARENT,
		SOLIDBLOCK,
		
		ENTITY,
		LIVINGENTITY
	}
	
	private List<Block> sightLine = new ArrayList<Block>(); // = event.getPlayer().getLineOfSight(null, 60);
	private int distance = 0;

	private Location loc = null;
	
	private Block source = null;
	private Block anyBlock = null;
	private Block liquid = null;
	private Block transparent = null;
	private Block solidBlock = null;

	private Entity entity = null;
	private LivingEntity livingEntity = null;
	
	Target(LivingEntity inEntity, boolean checkEntities) {
        int maxDistance = 120; //higher than 140 can interfere with unloaded chunks. At least the bukkit docs say so.
        List<Block> blocks = new ArrayList<Block>();
        Iterator<Block> itr = new BlockIterator(inEntity, maxDistance);
        Block prevBlock = null;
        List<Entity> ents = new ArrayList<Entity>();
        List<LivingEntity> lents = new ArrayList<LivingEntity>();
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if(checkEntities) {
            	if(livingEntity==null) {
            		if(entity==null){
            			ents = Utils.getNearbyEntities(block.getLocation(), .5);
            			ents.remove(inEntity);
            			if(!ents.isEmpty()){
            				entity = ents.get(0);
            				lents = Utils.getLivingEntities(ents);
            				if(!lents.isEmpty()){
            					livingEntity = lents.get(0);
            					if(loc==null){
            						loc = livingEntity.getLocation();
            					}
            				}
            			}
            		} else { //if entity has already been set
            			lents = Utils.getNearbyLivingEntities(block.getLocation(), .5);
            			lents.remove(inEntity);
            			if(!lents.isEmpty()){
            				livingEntity = lents.get(0);
            				if(loc==null){
            					loc = livingEntity.getLocation();
            				}
            			}
            		}
            	}
            }
            if(solidBlock==null) {
            	if(block.getType().isSolid()) {
            		solidBlock = block;
            		transparent = prevBlock;
            		if(loc==null) {
            			loc = block.getLocation();
            		}
            	}
            	prevBlock = block;
            	if(liquid!=null) { continue; }
            	if(block.isLiquid()) { liquid = block; }
            	if(anyBlock!=null) { continue; }
            	if(!block.isEmpty()) { anyBlock = block; }
            }
        }
        source = blocks.get(0);
        sightLine.addAll(blocks);
    }
	
	public Target(LivingEntity inEntity) {
		this(inEntity, false);
	}

	public Block getSolidBlock(){
		int index = sightLine.indexOf(solidBlock);
		if(index>distance) { return sightLine.get(distance); }
		else { return solidBlock; }
	}
	
	public void setDistance(int distance){
		if(distance>119) { distance = 119; } //Let's not get carried away, now!
		this.distance = distance;
	}

	public Block getTransparent() {
		int index = sightLine.indexOf(transparent);
		if(index>distance) { return sightLine.get(distance); }
		else { return transparent; }
	}

	public LivingEntity getLivingEntity() {
		double dist = source.getLocation().distance(livingEntity.getLocation());
		if(dist>distance) { return null; }
		else { return livingEntity; }
	}

	public Location getLoc() {
		double dist = source.getLocation().distance(loc);
		if(dist>distance) { return sightLine.get(distance).getLocation(); }
		else { return loc; }
	}
}
