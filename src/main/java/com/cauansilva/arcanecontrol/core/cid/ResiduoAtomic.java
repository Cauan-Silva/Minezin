package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
/** Cosmetic fragments and a low ash field. Never creates falling-block entities or edits terrain. */
public final class ResiduoAtomic {
    public static void desenhar(PoseStack s,Vec3 cam,double t){
        if(!ConfigCliente.CINEMA_ATOMIC.get()||t<94||t>=240||!DesenhoMagia.iniciar(t))return;
        try{
            DesenhoMagia.lote(s,8,false,()->{
                int n=ConfigCliente.VFX_REDUZIDOS.get()?12:48;
                for(int i=0;i<n;i++){
                    var d=FormaEnergiaAtomic.detrito(i,t);if(d.alpha()<=0)continue;var p=d.ponto();
                    s.pushPose();try{s.translate(p.x(),p.y(),p.z());s.mulPose(Axis.XP.rotation((float)d.giro()));s.mulPose(Axis.ZP.rotation((float)(d.giro()*.7)));
                        DesenhoMagia.caixa(s,Vec3.ZERO,d.tamanho(),d.tamanho()*.6,d.tamanho()*.8,i%3==0?0x75667D:0x393440,(float)d.alpha());
                    }finally{s.popPose();}
                }
                float fade=(float)(Math.min(1,(t-94)/20)*Math.max(0,1-(t-145)/95));
                for(int i=0;i<(ConfigCliente.VFX_REDUZIDOS.get()?8:20);i++){
                    double a=i*2.399963+t*.0018,r=7+i*.65+(t-94)*.025;var p=new Vec3(Math.cos(a)*r,.25+(i%4)*.2,Math.sin(a)*r);
                    DesenhoMagia.fita(s,cam,p,p.add(Math.sin(a)*2,.3,-Math.cos(a)*2),.4,0,1,0x4E4557,fade*.13f);
                }
            });
        }finally{DesenhoMagia.terminar();}
    }
    private ResiduoAtomic(){}
}
