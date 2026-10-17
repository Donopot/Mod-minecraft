package com.donopot.voiddynasty;

/**
 * Types de vaisseaux disponibles dans la flotte.
 * Chaque type référence un template .bin.gz embarqué (format binaire voiddynasty) + ses dimensions réelles.
 */
public enum ShipType {
    EMPEROR("emperor.bin.gz", "Empereur", 266, 118, 84),
    FRIGATE("frigate.bin.gz", "Frégate impériale", 266, 118, 84),
    RUIN("ruin.bin.gz", "Ruine", 80, 16, 80),
    TITAN("titan.bin.gz", "Titan", 54, 124, 74),
    ANDROMEDA("andromeda.bin.gz", "Andromède", 113, 143, 444),
    EXECUTOR("executor.bin.gz", "Executor", 181, 53, 710);

    public final String template;
    public final String displayName;
    public final int nx, ny, nz;

    ShipType(String template, String displayName, int nx, int ny, int nz) {
        this.template = template;
        this.displayName = displayName;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }

    public String resourcePath() {
        return "/assets/voiddynasty/structures/" + template;
    }
}
