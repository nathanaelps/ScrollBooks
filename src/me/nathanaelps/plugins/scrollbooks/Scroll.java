package me.nathanaelps.plugins.scrollbooks;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class Scroll extends Book {

	public Scroll(ItemStack book) {
		super(book);
	}
	
	public Scroll(String title, String author, List<String> pages){
		super(title, author, pages);
	}
	
	public String get(String desiredKey) {
		for(String page : this.pages) {
			String[] lines = page.split("\\+");
			for(String line : lines) {
				String[] express = line.split(" ", 2);
				if(express[0].equals(desiredKey)) { return express[1]; }
			}	
		}
		return null;
	}

	public int getInt(String desiredKey) {
		for(String page : this.pages) {
			String[] lines = page.split("\\+");
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
			String[] lines = page.split("\\+");
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
			String[] lines = page.split("\\+");
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
