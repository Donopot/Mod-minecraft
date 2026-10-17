package br.com.oneminepiece.opworldlike.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.*;
import net.mcreator.minepiece.procedures.ParameciaStoneData;

/** Active effects on arbitrary mobs are not a client-side effect registry. Track the visual explicitly. */
@Mixin(LivingEntity.class)
public class ParameciaStoneDataMixin implements ParameciaStoneData {
 @Unique private static final EntityDataAccessor<Boolean> minepiece$STONE=SynchedEntityData.defineId(LivingEntity.class,EntityDataSerializers.BOOLEAN);
 @Inject(method="defineSynchedData",at=@At("TAIL"))
 private void minepiece$defineStone(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(minepiece$STONE,false);}
 public boolean minepiece$stoneFlag(){return ((LivingEntity)(Object)this).getEntityData().get(minepiece$STONE);}
 public void minepiece$stoneFlag(boolean value){((LivingEntity)(Object)this).getEntityData().set(minepiece$STONE,value);}
}
