package me.nathanaelps.plugins.scrollbooks;
/* The Book class.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Book {

	private BookMeta book;
	private String keyBreak = "; ";
			
	public Book(String title, String author, List<String> pages){
		book.setTitle(title);
		book.setAuthor(author);
		book.setPages(pages);
	}
	
	@SuppressWarnings("unused")
	private void log(Object in){
		System.out.println(in);
	}

	public Book(BookMeta book){
		this.book = book;
	}
		
	public Book(ItemStack book){
		this.book = (BookMeta) book;
	}
		
    public ItemStack toItemStack(){
        return (ItemStack) book;
    }
    
    public void dropBook(Location location){
		Item droppedBook = location.getWorld().dropItemNaturally(location, new ItemStack(Material.WRITTEN_BOOK));
		((BookMeta) droppedBook).setAuthor(book.getAuthor());
		((BookMeta) droppedBook).setTitle(book.getTitle());
		((BookMeta) droppedBook).setPages(book.getPages());
    }
    
/*    public void applyToItemStack(Item itemToApplyTo){
    	ItemStack itemStack = itemToApplyTo.getItemStack();
    	
    	BookMeta book = (BookMeta) itemStack;

    	book.setAuthor(author);
    	book.setTitle(title);
    	book.setPages(pages);
    }
*/	
    public String getAuthor(){ return book.getAuthor(); }
    public void setAuthor(String author){ book.setAuthor(author); }
    public String getTitle(){ return book.getTitle(); }
    public void setTitle(String title){ book.setTitle(title); }
    public List<String> getPages(){ return book.getPages(); }
    public void setPages(List<String> pages){ book.setPages(pages); }

    public int size(){ return book.getPageCount(); }

	public String getKeyBreak(){
		return this.keyBreak;
	}

	public void setKeyBreak(String keyBreak){
		this.keyBreak = keyBreak;
	}

	public String get(String desiredKey, int pageNo) {
		String page = book.getPage(pageNo);
		String[] lines = page.split(keyBreak);
		for(String line : lines) {
			String[] express = line.split("=", 2);
			if(express[0].equalsIgnoreCase(desiredKey)) { return express[1]; }
		}	
		return null;
	}
	
	public HashMap<String, String> getAll(int pageNo) {
		String page = book.getPage(pageNo);
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
		book.setPage(pageNo, out.replaceFirst(keyBreak, ""));
	}
	
	public String get(String desiredKey) {
		String out = null;
		for(int pageNo=0; pageNo<book.getPageCount(); pageNo++){
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
		for(String page : book.getPages()) {
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
		for(String page : book.getPages()) {
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
		for(String page : book.getPages()) {
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
	
}
