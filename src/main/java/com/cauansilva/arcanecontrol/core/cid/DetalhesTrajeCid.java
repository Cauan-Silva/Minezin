package com.cauansilva.arcanecontrol.core.cid;
/** Small secondary offsets, derived from the fixed-tick springs. Collar inputs come from its own spring response. */
public final class DetalhesTrajeCid {
    public static final java.util.List<String> OSSOS=java.util.List.of("cid_collar_left","cid_collar_right","cid_collar_back","aba_esq","aba_dir");
    public static MolaMantoCid.Pose pose(String osso,MolaMantoCid.Pose base){
        if(!Double.isFinite(base.x()+base.y()+base.z()))return new MolaMantoCid.Pose(0,0,0);
        double x=0,y=0,z=0;
        if(osso.equals("cid_collar_back")){x=base.x()*.07;y=base.y()*.1;z=base.z()*.12;}
        else if(osso.equals("cid_collar_left")||osso.equals("cid_collar_right")){
            int lado=osso.endsWith("left")?-1:1;x=base.x()*.18;y=base.y()*.25;z=base.z()*.3-base.x()*lado*.07;
        }else if(osso.equals("aba_esq")||osso.equals("aba_dir")){int lado=osso.equals("aba_esq")?-1:1;x=-base.x()*.55;y=base.y()*.55;z=base.z()*.65+base.x()*lado*.15;}
        double limite=osso.startsWith("cid_collar_")?.045:.10;
        return new MolaMantoCid.Pose(clamp(x,limite),clamp(y,limite),clamp(z,limite));
    }
    private static double clamp(double v,double max){return Math.max(-max,Math.min(max,v));}
    private DetalhesTrajeCid(){}
}
