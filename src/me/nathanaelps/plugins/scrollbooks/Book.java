package me.nathanaelps.plugins.scrollbooks;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Book {

	private ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
	private BookMeta meta = (BookMeta) book.getItemMeta();
	private String keyBreak = "; ";
			
	public Book(String title, String author, List<String> pages){
		setAuthor(author);
		setPages(pages);
		setTitle(title);
	}
	
	@SuppressWarnings("unused")
	private void log(Object in){
		System.out.println(in);
	}

	public Book(BookMeta book){
		this.book.setItemMeta(book);
	}
		
	public Book(ItemStack book){
		this.book = book;
	}
		
    public ItemStack toItemStack(){
        return (ItemStack) book;
    }
    
    public void dropBook(Location location){
		location.getWorld().dropItemNaturally(location, book);
    }
    
    public String getAuthor(){ return meta.getAuthor(); }
    public void setAuthor(String author){ meta.setAuthor(author); book.setItemMeta(meta); }
    public String getTitle(){ return meta.getTitle(); }
    public void setTitle(String title){ meta.setTitle(title); book.setItemMeta(meta); }
    public List<String> getPages(){ return meta.getPages(); }
    public void setPages(List<String> pages){ meta.setPages(pages); book.setItemMeta(meta); }

    public int size(){ return ((BookMeta) book.getItemMeta()).getPageCount(); }

	public String getKeyBreak(){
		return this.keyBreak;
	}

	public void setKeyBreak(String keyBreak){
		this.keyBreak = keyBreak;
	}

	public String get(String desiredKey, int pageNo) {
		String page = ((BookMeta) book.getItemMeta()).getPage(pageNo);
		String[] lines = page.split(keyBreak);
		for(String line : lines) {
			String[] express = line.split("=", 2);
			if(express[0].equalsIgnoreCase(desiredKey)) { return express[1]; }
		}	
		return null;
	}
	
	public HashMap<String, String> getAll(int pageNo) {
		String page = ((BookMeta) book.getItemMeta()).getPage(pageNo);
		String[] lines = page.split(keyBreak);
		HashMap<String,String> out = new HashMap<String,String>();
		for(String line : lines) {
			String[] express = line.split("=", 2);
			if(express[0].length()<1) { continue; }
			out.put(express[0], express[1]);
		}
		return out;
	}

	public void putAll(HashMap<String,String> in, int pageNo){
		Set<String> keys = in.keySet();
		String out = "";
		for(String key: keys){
			out = out+keyBreak+key+"="+in.get(key);
		}
		((BookMeta) book.getItemMeta()).setPage(pageNo, out.replaceFirst(keyBreak, ""));
	}
	
	public String get(String desiredKey) {
		String out = null;
		for(int pageNo=0; pageNo<((BookMeta) book.getItemMeta()).getPageCount(); pageNo++){
			out = get(desiredKey,pageNo);
			if(out != null) { return out; }
		}
		return null;
	}

	public int getInt(String desiredKey, int pageNo){
		String out = get(desiredKey,pageNo);
		if(out!=null){
			try{ return Integer.parseInt(out); }
			catch( NumberFormatException e){ /* do nothing.*/ }
		}
		return 0;
	}
	
	public Float getFloat(String desiredKey, int pageNo){
		String out = get(desiredKey,pageNo);
		if(out!=null){
			try{ return Float.parseFloat(out); }
			catch( NumberFormatException e){ /* do nothing.*/ }
		}
		return 0f;
	}
	
	public boolean getBoolean(String desiredKey, int pageNo){
		String out = get(desiredKey,pageNo);
		if(out!=null){
			if(out.equals("true")) { return true; }
			if(out.equals("false")) { return false; }
		}
		return false;
	}
	
	public int getInt(String desiredKey) {
		for(String page : ((BookMeta) book.getItemMeta()).getPages()) {
			String[] lines = page.split(keyBreak);
			for(String line : lines) {
				String[] express = line.split("=", 2);
				if(express[0].equalsIgnoreCase(desiredKey)) {
					try{ return Integer.parseInt(express[1]); }
					catch( NumberFormatException e){ /* do nothing.*/ }
				}
			}	
		}
		return 0;
	}
	
	public Float getFloat(String desiredKey) {
		for(String page : ((BookMeta) book.getItemMeta()).getPages()) {
			String[] lines = page.split(keyBreak);
			for(String line : lines) {
				String[] express = line.split("=", 2);
				if(express[0].equalsIgnoreCase(desiredKey)) {
					try{ return Float.parseFloat(express[1]); }
					catch( NumberFormatException e){ /* do nothing.*/ }
				}
			}	
		}
		return 0f;
	}
	
	public boolean getBoolean(String desiredKey) {
		for(String page : ((BookMeta) book.getItemMeta()).getPages()) {
			String[] lines = page.split(keyBreak);
			for(String line : lines) {
				String[] express = line.split("=", 2);
				if(express[0].equals(desiredKey)) {
					if(express[1].equalsIgnoreCase("true")) { return true; }
					if(express[1].equalsIgnoreCase("false")) { return false; }
				}
			}	
		}
		return false;
	}

	public void setKey(String key, Object value, int pageNo) {
		HashMap<String, String> in = getAll(pageNo);
		in.put(key, String.valueOf(value));
		putAll(in, pageNo);
	}
	
	//-------- Methods from Implemented Class.


	public void addPage(String... pages) {
		meta.addPage(pages);
		book.setItemMeta(meta);
	}

	public String getPage(int page) {
		return meta.getPage(page);
	}

	public int getPageCount() {
		return meta.getPageCount();
	}

	public void setPage(int page, String data) {
		meta.setPage(page, data);
		book.setItemMeta(meta);
	}

	public void setPages(String... pages) {
		meta.setPages(pages);
		book.setItemMeta(meta);
	}
	
}
