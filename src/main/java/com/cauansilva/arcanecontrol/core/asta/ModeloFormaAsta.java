package com.cauansilva.arcanecontrol.core.asta;
import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mesma geometria JSON usada na previa; volume original independente da armadura equipada. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ModeloFormaAsta {
    private record Parte(Vec3 centro,Vec3 tamanho,Vec3 pivot,Vec3 rot,int cor,boolean asa){}
    private static List<Parte> partes;
    @SubscribeEvent public static void recarga(RegisterClientReloadListenersEvent e){e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)r->partes=null);}
    private static Vec3 vec(JsonObject j,String n){var a=j.getAsJsonArray(n);return new Vec3(a.get(0).getAsDouble(),a.get(1).getAsDouble(),a.get(2).getAsDouble());}
    private static List<Parte> carregar(){
        try(var in=net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("arcanecontrol","models/effects/asta_forma.json")).orElseThrow().open()){
            var j=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();var lista=new ArrayList<Parte>();
            for(var v:j.getAsJsonArray("pieces")){var c=v.getAsJsonObject();var size=vec(c,"size");lista.add(new Parte(vec(c,"origin").add(size.scale(.5)).scale(1/16.0),size.scale(1/32.0),vec(c,"pivot").scale(1/16.0),vec(c,"rotation"),Integer.parseInt(c.get("color").getAsString(),16),c.get("wing").getAsBoolean()));}
            return List.copyOf(lista);
        }catch(Exception ex){com.mojang.logging.LogUtils.getLogger().warn("Forma Asta indisponivel",ex);return List.of();}
    }
    public static void desenhar(PoseStack s,double t,float alpha,double idade){
        if(partes==null)partes=carregar();float abertura=(float)Math.min(1,idade/30);
        DesenhoMagia.lote(s,8,false,()->{for(var p:partes){
            s.pushPose();
            if(p.asa){s.translate(-.25,1.4375,.1875);s.mulPose(Axis.YP.rotationDegrees((float)(Math.sin(t*.06)*5+(1-abertura)*65)));s.mulPose(Axis.ZP.rotationDegrees((1-abertura)*-35));s.translate(.25,-1.4375,-.1875);}
            s.translate(p.pivot.x,p.pivot.y,p.pivot.z);
            s.mulPose(Axis.XP.rotationDegrees((float)p.rot.x));s.mulPose(Axis.YP.rotationDegrees((float)p.rot.y));s.mulPose(Axis.ZP.rotationDegrees((float)p.rot.z));
            s.translate(-p.pivot.x,-p.pivot.y,-p.pivot.z);DesenhoMagia.caixa(s,p.centro,p.tamanho.x,p.tamanho.y,p.tamanho.z,p.cor,alpha);s.popPose();
        }});
    }
}
