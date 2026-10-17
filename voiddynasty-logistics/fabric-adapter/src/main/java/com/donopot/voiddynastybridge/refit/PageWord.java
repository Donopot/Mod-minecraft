package com.donopot.voiddynastybridge.refit;
import net.minecraft.world.inventory.DataSlot;
public final class PageWord extends DataSlot {
    private final PagedContainer page;private final int part;
    public PageWord(PagedContainer page,int part){this.page=page;this.part=part;}
    @Override public int get(){return page.word(part);}
    @Override public void set(int value){page.word(part,value);}
}
