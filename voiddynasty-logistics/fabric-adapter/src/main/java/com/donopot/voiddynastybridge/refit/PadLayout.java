package com.donopot.voiddynastybridge.refit;

/** Logical GUI coordinates, independently testable at Minecraft's smallest supported UI size. */
public record PadLayout(int left,int width,int rows,int footer,int contentBottom) {
    public static PadLayout of(int width,int height){int w=Math.max(280,Math.min(540,width-24));return new PadLayout((width-w)/2,w,Math.max(1,(height-142)/22),height-54,height-62);}
    public int columnWidth(){return (width-12)/2;}
    public static boolean name(String name){return name!=null&&name.matches("[\\p{L}\\p{N}_-]{1,32}");}
}
