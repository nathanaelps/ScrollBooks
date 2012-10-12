package me.nathanaelps.plugins.scrollbooks;
/* The Book class.
 * Credit for the class's creation goes to:
 * JamesS237, for clues into the NBTTagCompound.
 * CodenameB, aka Vlad, for general ideas.
 * 
 */

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class Book {

	private String title;
	private String author;
	protected ArrayList<String> pages = new ArrayList<String>();
	private String keyBreak = "//+";
			
	public Book(String title, String author, List<String> pages){
		this.title = title;
		this.author = author;
		this.pages.addAll(pages);
	}

	public Book(ItemStack book){
		NBTTagCompound tags = ((CraftItemStack) book).getHandle().tag;

		try {
			this.author = tags.getString("author");
		} catch( NullPointerException e){
			this.author = "Dear Mother";
		}
		try {
			this.title = tags.getString("title");
		} catch( NullPointerException e){
			this.title = "Hearts and Kisses";
		}

		try {
			NBTTagList tagPages = tags.getList("pages");
			for(int i = 0;i<tagPages.size();i++){
				this.pages.add(tagPages.get(i).toString());
			}
		} catch( NullPointerException e){
			this.pages.add("[This Page Left Intentionally Blank]");
		}
	}
		
    public ItemStack toItemStack(){
    	/* Fun fact: Dropping the ItemStack created here results in a failed book.
    	 * Placing it directly into a Player inventory works, though.
    	 * Why is that?
    	 * 
    	 * This function is dedicated to JamesS237.
    	 */
        CraftItemStack craftBook = new CraftItemStack(Material.WRITTEN_BOOK);
        NBTTagCompound tags = new NBTTagCompound();
        NBTTagList nPages = new NBTTagList();
       
        for(int i=0; i<this.pages.size(); i++){ 
            nPages.add(new NBTTagString(Integer.toString(i+1),this.pages.get(i)));
        }

        tags.setString("title",this.title);
        tags.setString("author",this.author);
        tags.set("pages", nPages);
 
        craftBook.getHandle().setTag(tags);
        craftBook.setAmount(1);
               
        return (ItemStack) craftBook;
    }
    
    public void applyToItemStack(ItemStack itemStackToApplyTo){
    	itemStackToApplyTo.setType(Material.WRITTEN_BOOK);
    	
        NBTTagCompound tags = new NBTTagCompound();
        NBTTagList nPages = new NBTTagList();
       
        for(int i=0; i<this.pages.size(); i++){ 
            nPages.add(new NBTTagString(Integer.toString(i+1),this.pages.get(i)));
        }

        tags.setString("title",this.title);
        tags.setString("author",this.author);
        tags.set("pages", nPages);
 
        ((CraftItemStack) itemStackToApplyTo).getHandle().setTag(tags);
//        itemStack.setAmount(1);
    }

	public String getTitle(){
		return this.title;
	}

	public String getAuthor(){
		return this.author;
	}

	public List<String> getPages(){
		return this.pages;
	}
	
	public void setTitle(String title){
		this.title = title;
	}

	public void setAuthor(String author){
		this.author = author;
	}

	public void setPages(List<String> pages){
		this.pages = (ArrayList<String>) pages;
	}
	
	public String getPage(int pageNo){
		if(this.pages.size()<pageNo) { return ""; }
		return this.pages.get(pageNo);
	}

	public void setPage(int pageNo, String page){
		if(this.pages.size()<pageNo) { this.pages.remove(pageNo); }
		this.pages.add(pageNo, page);
	}
	
	public String getKeyBreak(){
		return this.keyBreak;
	}

	public void setKeyBreak(String keyBreak){
		this.keyBreak = keyBreak;
	}

	public String get(String desiredKey) {
		for(String page : this.pages) {
			String[] lines = page.split(keyBreak);
			for(String line : lines) {
				String[] express = line.split(" ", 2);
				if(express[0].equals(desiredKey)) { return express[1]; }
			}	
		}
		return null;
	}

	public int getInt(String desiredKey) {
		for(String page : this.pages) {
			String[] lines = page.split(keyBreak);
			for(String line : lines) {
				String[] express = line.split(" ", 2);
				if(express[0].equals(desiredKey)) {
					try{ return Integer.parseInt(express[1]); }
					catch( NumberFormatException e){ /* do nothing.*/ }
				}
			}	
		}
		return 0;
	}
	
	public Float getFloat(String desiredKey) {
		for(String page : this.pages) {
			String[] lines = page.split(keyBreak);
			for(String line : lines) {
				String[] express = line.split(" ", 2);
				if(express[0].equals(desiredKey)) {
					try{ return Float.parseFloat(express[1]); }
					catch( NumberFormatException e){ /* do nothing.*/ }
				}
			}	
		}
		return 0f;
	}
	
	public boolean getBoolean(String desiredKey) {
		for(String page : this.pages) {
			String[] lines = page.split(keyBreak);
			for(String line : lines) {
				String[] express = line.split(" ", 2);
				if(express[0].equals(desiredKey)) {
					if(express[1].equals("true")) { return true; }
					if(express[1].equals("false")) { return false; }
				}
			}	
		}
		return false;
	}
	
}
