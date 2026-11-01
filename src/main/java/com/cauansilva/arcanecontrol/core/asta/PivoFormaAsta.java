package com.cauansilva.arcanecontrol.core.asta;
import net.minecraft.world.phys.Vec3;

/** Armor/Bedrock coordinates to the local coordinates of the vanilla model part. */
public final class PivoFormaAsta {
    private PivoFormaAsta(){}
    public static Vec3 converter(Vec3 v,String attachment){
        if(!attachment.equals("body")&&!attachment.equals("head")&&!attachment.equals("right_arm"))throw new IllegalArgumentException("Attachment invalido");
        return new Vec3(v.x+(attachment.equals("right_arm")?5:0),(attachment.equals("right_arm")?22:24)-v.y,v.z).scale(1/16.0);
    }
}
