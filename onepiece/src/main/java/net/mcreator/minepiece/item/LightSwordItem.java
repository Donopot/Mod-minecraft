package net.mcreator.minepiece.item;
import net.minecraft.world.item.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
public final class LightSwordItem extends Item {
 public LightSwordItem(Properties properties){super(ToolMaterial.DIAMOND.applySwordProperties(properties.stacksTo(1),4,-2.5F));}
 @Override public void inventoryTick(ItemStack stack,ServerLevel world,Entity holder,EquipmentSlot slot){super.inventoryTick(stack,world,holder,slot);net.mcreator.minepiece.procedures.LightSwords.inventoryTick(stack,holder);}
}
