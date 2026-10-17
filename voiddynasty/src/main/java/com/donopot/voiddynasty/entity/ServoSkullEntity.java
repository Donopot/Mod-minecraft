package com.donopot.voiddynasty.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

/**
 * Servo-crâne : drone volant (rendu Vex) qui suit son propriétaire et ramasse les
 * items au sol dans un rayon de 8 blocs, en les déposant dans son inventaire.
 */
public class ServoSkullEntity extends Vex {

    public static final int COLLECT_RADIUS = 8;

    private UUID ownerUUID;

    public ServoSkullEntity(EntityType<? extends ServoSkullEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes();
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public LivingEntity getOwner() {
        if (ownerUUID == null) return null;
        return this.level().getPlayerByUUID(ownerUUID);
    }

    @Override
    protected void registerGoals() {
        // le servo-crâne ne combat pas : il suit son owner (vol Vex) et ramasse les items
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.collectItems();
        }
    }

    private void collectItems() {
        if (!(getOwner() instanceof Player owner)) return;
        AABB area = owner.getBoundingBox().inflate(COLLECT_RADIUS);
        for (ItemEntity item : this.level().getEntitiesOfClass(ItemEntity.class, area)) {
            if (!item.isAlive()) continue;
            ItemStack stack = item.getItem();
            if (owner.getInventory().add(stack)) {
                item.discard();
            }
        }
    }
}
