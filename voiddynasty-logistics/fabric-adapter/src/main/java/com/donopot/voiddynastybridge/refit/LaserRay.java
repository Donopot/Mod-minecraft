package com.donopot.voiddynastybridge.refit;
import java.util.List;
import net.minecraft.world.phys.*;

public final class LaserRay {
    public record Hit(int index,Vec3 point){}
    private LaserRay(){}
    /** The supplied end is already clipped to the nearest solid block. */
    public static Hit closest(Vec3 start,Vec3 end,List<AABB> boxes){
        double nearest=start.distanceToSqr(end);int selected=-1;Vec3 hit=end;
        for(int i=0;i<boxes.size();i++){
            var box=boxes.get(i);var point=box.contains(start)?java.util.Optional.of(start):box.clip(start,end);
            if(point.isPresent()&&start.distanceToSqr(point.get())<nearest){nearest=start.distanceToSqr(point.get());hit=point.get();selected=i;}
        }
        return new Hit(selected,hit);
    }
}
