package com.donopot.voiddynastybridge.refit;
import net.minecraft.world.inventory.Slot;
public final class PageSlot extends Slot {
    private final PagedContainer page;private final int logical;
    public PageSlot(PagedContainer page,int slot,int x,int y){super(page,slot,x,y);this.page=page;logical=slot;}
    @Override public boolean isActive(){return page.exists(logical);}
}
