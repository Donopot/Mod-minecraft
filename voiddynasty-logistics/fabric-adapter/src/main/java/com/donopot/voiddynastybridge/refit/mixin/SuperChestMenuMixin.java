package com.donopot.voiddynastybridge.refit.mixin;
import com.donopot.voiddynastybridge.refit.*;

import com.donopot.voiddynasty.menu.SuperChestMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Keep packet slot IDs identical on both sides; scrolling only changes the server projection. */
@Mixin(value=SuperChestMenu.class,remap=false)
public abstract class SuperChestMenuMixin extends AbstractContainerMenu {
    @Shadow @Final @Mutable private Container container;
    @Unique private PagedContainer vd$window;
    protected SuperChestMenuMixin(MenuType<?> type,int id){super(type,id);}
    @Inject(method="<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V",at=@At("RETURN"))
    private void vd$window(int id,Inventory inventory,Container original,CallbackInfo ci){
        vd$window=new PagedContainer(original,inventory.player.level().isClientSide());container=vd$window;
        for(int i=0;i<PagedContainer.WINDOW;i++){
            var old=slots.get(i);var slot=new PageSlot(vd$window,i,old.x,old.y);
            slot.index=i;slots.set(i,slot);
        }
        for(int i=0;i<4;i++)addDataSlot(new PageWord(vd$window,i));
    }
    @Inject(method="getMaxScrollRow",at=@At("HEAD"),cancellable=true)
    private void vd$max(CallbackInfoReturnable<Integer> ci){if(vd$window!=null)ci.setReturnValue(vd$window.maxRow());}
    @Inject(method="getScrollRow",at=@At("HEAD"),cancellable=true)
    private void vd$row(CallbackInfoReturnable<Integer> ci){if(vd$window!=null)ci.setReturnValue(vd$window.row());}
    @Inject(method="setScrollRow",at=@At("HEAD"),cancellable=true)
    private void vd$scroll(int row,CallbackInfo ci){if(vd$window!=null){vd$window.row(row);ci.cancel();}}
}
