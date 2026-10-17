package com.donopot.voiddynastybridge;

import java.util.UUID;

/** Polls may defer one click; they never invalidate a delivery confirmation. */
public final class DeliveryInteraction {
    public record Click(UUID player,String action){}
    private String request="";
    private Click pending;
    public void started(String type){request=type;}
    public boolean mutationBusy(){return !request.isEmpty()&&!request.equals("inspect");}
    public boolean defer(UUID player,String action){
        if(!request.equals("inspect"))return false;
        if(pending==null)pending=new Click(player,action);
        return true;
    }
    public Click completed(){request="";Click next=pending;pending=null;return next;}
    public static boolean canShip(boolean fresh,boolean receiverAvailable,boolean paired,String phase,int iron,boolean mutationBusy){
        return canShip(fresh,receiverAvailable,paired,phase,iron,64,mutationBusy);
    }
    public static boolean canShip(boolean fresh,boolean receiverAvailable,boolean paired,String phase,int count,int required,boolean mutationBusy){
        return fresh&&receiverAvailable&&paired&&(phase.equals("IDLE")||phase.equals("SETTLED"))&&count>=required&&!mutationBusy;
    }
    public static String pauseMessage(String code){
        return switch(code){
            case "PAUSED_MODE"->"Menu ou dialogue Rogue Trader ouvert : envoi suspendu";
            case "PAUSED_COMBAT"->"Combat Rogue Trader : envoi suspendu";
            case "UNAVAILABLE"->"Campagne RT absente ou en chargement";
            case "PERSISTENCE_NOT_VERIFIED"->"Charger la sauvegarde RT PERSISTANCE VALIDEE ou REWARD_COMMITTED";
            case "NATIVE_CARGO_UNAVAILABLE"->"Cargaison native indisponible dans cette campagne";
            case "NATIVE_CARGO_NOT_FULL"->"Cargaison native incomplete : conserver les sauvegardes et le depot";
            default->"";
        };
    }
}
