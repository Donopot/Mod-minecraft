package com.donopot.voiddynastybridge;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Cargo and escrow live in the chunk, independently of the world journal. */
public final class CargoBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items=NonNullList.withSize(9,ItemStack.EMPTY);
    public String world="", crate="", delivery="", phase="IDLE";
    public int reserved;
    public String reservedItem="minecraft:iron_ingot",mission=CargoCatalog.DEFAULT;
    public CargoBlockEntity(BlockPos p,BlockState s){super(BridgeMod.CARGO_ENTITY,p,s);}
    @Override public int getContainerSize(){return 9;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> v){items=v;}
    @Override protected Component getDefaultName(){return Component.literal("VOID DYNASTY | Depot de fer");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new ChestMenu(MenuType.GENERIC_9x1,id,inventory,this,1);}
    public boolean locked(){return (!phase.equals("IDLE")&&!phase.equals("SETTLED")) || BridgeMod.service==null || !BridgeMod.service.matches(this) || BridgeMod.service.locked();}
    @Override public boolean canOpen(Player p){return !locked() && super.canOpen(p);}
    @Override public boolean stillValid(Player p){return !locked() && super.stillValid(p);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return !locked() && CargoCatalog.accepts(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());}
    @Override public ItemStack removeItem(int slot,int count){return locked()?ItemStack.EMPTY:super.removeItem(slot,count);}
    @Override public ItemStack removeItemNoUpdate(int slot){return locked()?ItemStack.EMPTY:super.removeItemNoUpdate(slot);}
    @Override public void setItem(int slot,ItemStack stack){if(!locked() && (stack.isEmpty()||CargoCatalog.accepts(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())))super.setItem(slot,stack);}
    @Override public void clearContent(){if(!locked())super.clearContent();}
    @Override public int[] getSlotsForFace(Direction d){return new int[0];}
    @Override public boolean canPlaceItemThroughFace(int i,ItemStack s,Direction d){return false;}
    @Override public boolean canTakeItemThroughFace(int i,ItemStack s,Direction d){return false;}
    private boolean matchesItem(ItemStack s,String id){return !s.isEmpty()&&BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals(id);}
    public int count(String item){int n=0;for(ItemStack s:items)if(matchesItem(s,item))n+=s.getCount();return n;}
    public void reserve(String id,CargoCatalog.Mission m){if(count(m.item())<m.count()||reserved!=0||(!phase.equals("IDLE")&&!phase.equals("SETTLED")))throw new IllegalStateException("Reservation invalide");int n=m.count();for(ItemStack s:items){if(!matchesItem(s,m.item()))continue;int k=Math.min(n,s.getCount());s.shrink(k);n-=k;if(n==0)break;}reserved=m.count();reservedItem=m.item();mission=m.id();delivery=id;phase="RESERVED";setChanged();}
    public void settle(String id){if(!delivery.equals(id)||reserved!=CargoCatalog.get(mission).count()||!phase.equals("RESERVED"))throw new IllegalStateException("Recu invalide");reserved=0;phase="SETTLED";setChanged();}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);items=NonNullList.withSize(9,ItemStack.EMPTY);ContainerHelper.loadAllItems(in,items);world=in.getStringOr("VDWorld","");crate=in.getStringOr("VDCrate","");delivery=in.getStringOr("VDDelivery","");phase=in.getStringOr("VDPhase","IDLE");reserved=in.getIntOr("VDReserved",0);reservedItem=in.getStringOr("VDReservedItem","minecraft:iron_ingot");mission=in.getStringOr("VDMission",CargoCatalog.DEFAULT);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);ContainerHelper.saveAllItems(out,items);out.putString("VDWorld",world);out.putString("VDCrate",crate);out.putString("VDDelivery",delivery);out.putString("VDPhase",phase);out.putInt("VDReserved",reserved);out.putString("VDReservedItem",reservedItem);out.putString("VDMission",mission);}
}
