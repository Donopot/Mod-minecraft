package com.donopot.voiddynasty;

import net.minecraft.core.BlockPos;

/**
 * Un vaisseau possédé par un joueur.
 * - déployé  : base != null (position du coin inférieur), les blocs sont dans le monde.
 * - rangé    : storedBytes != null (buffer gzippé des blocs), base == null.
 */
public final class ShipData {
    public String name;
    public ShipType type;
    public BlockPos base;
    public byte[] storedBytes;

    public ShipData(String name, ShipType type) {
        this.name = name;
        this.type = type;
        this.base = null;
        this.storedBytes = null;
    }

    public boolean isDeployed() {
        return base != null;
    }
}
