package me.nathanaelps.plugins.scrollbooks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Selection{
	
	//-------- "Constants"
	private Block emptyBlock = ScrollBooks.server.getWorlds().get(0).getBlockAt(0, 0, 0);
	private Set<Block> blocks = new HashSet<Block>();
	private Material material = Material.AIR;

	//-------- Constructors
	public Selection(Block in){
		blocks.add(in);
		material = in.getType();
	}

	public Selection(Set<Block> in){
		blocks.addAll(in);
	}

	public Selection(){
	}

	//-------- Utility Functions
	public static void log(String in) {
		System.out.println("[" + ScrollBooks.pluginName + "] " + in);
	}
	public static void log(Object in) {
		log(String.valueOf(in));
	}
		
	//-------- Selection Creation Functions
	public void contiguousLozengeVolume(int distance){
		Set<Block> queue = new HashSet<Block>();
		Set<Block> nextQueue = new HashSet<Block>();
		
		queue.addAll(blocks);
		
		for(int i=distance; i>0; i--){
			for(Block q : queue){
				Set<Block> tBlocks = touchingBlocks(q);
				for(Block t : tBlocks){
					if(queue.contains(t)) { continue; }
					if(blocks.contains(t)) { continue; }
					if(t.getType().equals(material)) { nextQueue.add(t); }
				}
			}
			blocks.addAll(queue);
			queue.clear();
			queue.addAll(nextQueue);
			nextQueue.clear();
		}
	}
	
	public void contiguousCubicVolume(int distance){
		Set<Block> queue = new HashSet<Block>();
		Set<Block> nextQueue = new HashSet<Block>();
		
		queue.addAll(blocks);
		
		for(int i=distance; i>0; i--){
			for(Block q : queue){
				Set<Block> tBlocks = centeredCubeA(q,1);
				for(Block t : tBlocks){
					if(queue.contains(t)) { continue; }
					if(blocks.contains(t)) { continue; }
					if(t.getType().equals(material)) { nextQueue.add(t); }
				}
			}
			blocks.addAll(queue);
			queue.clear();
			queue.addAll(nextQueue);
			nextQueue.clear();
		}
	}
	
	private Set<Block> touchingBlocks(Block block){
		Set<Block> out = new HashSet<Block>();
		
		out.add(block.getRelative( 1, 0, 0));
		out.add(block.getRelative(-1, 0, 0));
		out.add(block.getRelative( 0, 1, 0));
		out.add(block.getRelative( 0,-1, 0));
		out.add(block.getRelative( 0, 0, 1));
		out.add(block.getRelative( 0, 0,-1));
		
		return out;
	}

	private Set<Block> centeredCubeA(Block block, int radius){
		Set<Block> finishedBlocks = new HashSet<Block>();
		
		for(int i=-radius; i<=radius; i++){
			for(int j=-radius; j<=radius; j++){
				for(int k=-radius; k<=radius; k++){
					finishedBlocks.add(block.getRelative(i, j, k));
				}
			}
		}
		return finishedBlocks;
	}
	
	public void outset(int radius){
		outset(radius, radius, radius);
	}
	
	public void rect(Block start, Block end){
		blocks.clear();
		
		World world = start.getWorld();
		
		for(int i=start.getX(); i<=end.getX(); i++){
			for(int j=start.getY(); j<=end.getY(); j++){
				for(int k=start.getZ(); k<=end.getZ(); k++){
					blocks.add(world.getBlockAt(i, j, k));
				}
			}
		}
	}
	
	public void outset(int xRad, int yRad, int zRad){
		Set<Block> finishedBlocks = new HashSet<Block>();

		for(Block block : blocks){
			for(int i=-xRad; i<=xRad; i++){
				for(int j=-yRad; j<=yRad; j++){
					for(int k=-zRad; k<=zRad; k++){
						finishedBlocks.add(block.getRelative(i, j, k));
					}
				}
			}
		}
		blocks.addAll(finishedBlocks);
	}
	
	//--------- Selection Information Functions
	public Block getHighest(Material mat){
		Block out = emptyBlock;
		
		for(Block block : blocks){
			if(block.getY()>out.getY() &&
					block.getType().equals(mat)){
				out = block;
			}
		}
		if(out.equals(emptyBlock)) { return null; }
		return out;
	}
	
	public Block getLowest(Material mat){
		Block out = emptyBlock.getRelative(0,255,0);
		
		for(Block block : blocks){
			if(block.getY()<out.getY() &&
					block.getType().equals(mat)){
				out = block;
			}
		}
		if(out.equals(emptyBlock)) { return null; }
		return out;
	}
	
	public boolean contains(Material mat){
		for(Block block : blocks){
			if(block.getType().equals(mat)) { return true;}
		}
		return false;
	}
	
	public Selection permitted(String player, String flag){
		Selection out = new Selection();
		
		for(Block block : blocks){
			if(ScrollBooks.totems.canEdit(player, block, flag)){
				out.add(block);
			}
		}
		return out;

	}
	
	public Selection only(Material mat){
		Selection out = new Selection();
		
		for(Block block : blocks){
			if(block.getType().equals(mat)) {
				out.add(block);
			}
		}
		return out;

	}
	
	public boolean contains(Block block){
		return blocks.contains(block);
	}
	
	public void clear(){
		blocks.clear();
	}
	
	//--------- Selection Modification Functions
	
	public void convert(Material from, Material to){
		for(Block block : blocks){
			if(block.getType().equals(from)) {
				block.setType(to);
			}
		}
	}
	
	public void to(Material mat){
		for(Block block : blocks){
			block.setType(mat);
		}
	}
	
	public void randomRemove(double percentToRemove){
		for(Block block : blocks){
			if(Math.random()<percentToRemove){
				blocks.remove(block);
			}
		}
	}
	
	public void removeAbove(int altitude){
		Set<Block> temp = new HashSet<Block>();
		
		temp.addAll(blocks);
		
		for(Block block : temp){
			if(block.getY()>altitude) {
				blocks.remove(block);
			}
		}
	}
	
	public void removeBelow(int altitude){
		for(Block block : blocks){
			if(block.getY()<altitude) {
				blocks.remove(block);
			}
		}
	}
	
	//-------- Default Set Functions
	
	public boolean add(Block e) {
		if(e==null) { throw new NullPointerException();}
		return blocks.add((Block) e);
	}

	public boolean addAll(Collection<Block> c) {
		return blocks.addAll(c);
	}

	public boolean addAll(Selection c) {
		return blocks.addAll(((Selection) c).blocks);			
	}

	public boolean contains(Object o) {
		return blocks.contains(o);
	}

	public boolean containsAll(Collection<Block> c) {
		return blocks.containsAll(c);
	}

	public boolean isEmpty() {
		return blocks.isEmpty();
	}

	public Iterator<Block> iterator() {
		return blocks.iterator();
	}

	public boolean remove(Block o) {
		return blocks.remove(o);
	}

	public boolean retainAll(Object o) {
		if(o instanceof Collection) {
			return blocks.retainAll((Collection<?>) o);
		} else if(o instanceof Selection) {
			return blocks.retainAll(((Selection) o).blocks);
		}
		return false;
	}

	public boolean removeAll(Object o) {
		if(o instanceof Collection) {
			return blocks.removeAll((Collection<?>) o);
		} else if(o instanceof Selection) {
			return blocks.removeAll(((Selection) o).blocks);
		}
		return false;
	}

	public int size() {
		return blocks.size();
	}
}
