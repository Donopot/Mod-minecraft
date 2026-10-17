package com.donopot.voiddynastybridge.refit;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A 54-slot projection. The server owns the full inventory; the client only holds the visible window. */
public final class PagedContainer implements Container {
    public static final int WINDOW=54,COLUMNS=9;
    private final Container backing;
    private final boolean client;
    private int capacity,row;
    public PagedContainer(Container backing,boolean client){this.backing=backing;this.client=client;capacity=backing.getContainerSize();}
    public int capacity(){return capacity;}
    public int row(){return row;}
    public static int maxRow(int capacity){return (int)Math.max(0,((long)capacity+8)/9-6);}
    public int maxRow(){return maxRow(capacity);}
    public void row(int value){row=Math.clamp(value,0,maxRow());}
    public int word(int part){return switch(part){case 0->capacity&65535;case 1->capacity>>>16;case 2->row&65535;case 3->row>>>16;default->throw new IllegalArgumentException();};}
    public void word(int part,int value){
        if(!client)return;
        int v=value&65535;
        switch(part){case 0->capacity=(capacity&0xffff0000)|v;case 1->capacity=(capacity&65535)|(v<<16);case 2->row=(row&0xffff0000)|v;case 3->row=(row&65535)|(v<<16);default->throw new IllegalArgumentException();}
        capacity=Math.max(0,capacity);row=Math.max(0,row);
    }
    public boolean exists(int slot){return slot>=0&&slot<WINDOW&&(long)row*COLUMNS+slot<capacity;}
    private int index(int slot){return client?slot:row*COLUMNS+slot;}
    @Override public int getContainerSize(){return WINDOW;}
    @Override public boolean isEmpty(){for(int i=0;i<WINDOW;i++)if(!getItem(i).isEmpty())return false;return true;}
    @Override public ItemStack getItem(int slot){return exists(slot)?backing.getItem(index(slot)):ItemStack.EMPTY;}
    @Override public ItemStack removeItem(int slot,int count){if(!exists(slot))return ItemStack.EMPTY;var out=backing.removeItem(index(slot),count);setChanged();return out;}
    @Override public ItemStack removeItemNoUpdate(int slot){return exists(slot)?backing.removeItemNoUpdate(index(slot)):ItemStack.EMPTY;}
    @Override public void setItem(int slot,ItemStack stack){if(exists(slot)){backing.setItem(index(slot),stack);setChanged();}}
    @Override public void setChanged(){backing.setChanged();}
    @Override public boolean stillValid(Player player){return backing.stillValid(player);}
    @Override public int getMaxStackSize(){return backing.getMaxStackSize();}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return exists(slot)&&backing.canPlaceItem(index(slot),stack);}
    @Override public boolean canTakeItem(Container target,int slot,ItemStack stack){return exists(slot)&&backing.canTakeItem(target,index(slot),stack);}
    @Override public void startOpen(ContainerUser user){backing.startOpen(user);}
    @Override public void stopOpen(ContainerUser user){backing.stopOpen(user);}
    @Override public void clearContent(){for(int i=0;i<WINDOW;i++)if(exists(i))backing.setItem(index(i),ItemStack.EMPTY);setChanged();}
}
