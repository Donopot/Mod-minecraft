package com.donopot.voiddynasty.network;

/** Infos d'un vaisseau envoyées au client pour l'affichage du pad. */
public record ShipInfo(String name, String typeDisplay, boolean deployed, int x, int y, int z) {}
