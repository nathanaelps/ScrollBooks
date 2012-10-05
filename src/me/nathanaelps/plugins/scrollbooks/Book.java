package me.nathanaelps.plugins.scrollbooks;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/* The Book class.
 * Credit for the class's creation goes to:
 * JamesS237, for clues into the NBTTagCompound.
 * CodenameB, aka Vlad, for general ideas.
 */

public class Book {

	private String title;
	private String author;
	protected ArrayList<String> pages = new ArrayList<String>();

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
			this.author = "Herobrine";
		}
		try {
			this.title = tags.getString("title");
		} catch( NullPointerException e){
			this.title = "Necrowombicon";
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
		
    public ItemStack generateItemStack(){
        CraftItemStack newbook = new CraftItemStack(Material.WRITTEN_BOOK);
       
        NBTTagCompound newBookData = new NBTTagCompound();
       
        newBookData.setString("author",author);
        newBookData.setString("title",title);

        NBTTagList nPages = new NBTTagList();
        for(int i = 0;i<pages.size(); i++)
        { 
            nPages.add(new NBTTagString(Integer.toString(i+1),pages.get(i)));
        }

        newBookData.set("pages", nPages);
 
        newbook.getHandle().setTag(newBookData);

        newbook.setAmount(1);
               
        return (ItemStack) newbook;
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
}
