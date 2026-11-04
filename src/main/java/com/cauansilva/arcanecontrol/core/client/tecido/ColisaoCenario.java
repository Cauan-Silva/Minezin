package com.cauansilva.arcanecontrol.core.client.tecido;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import java.util.*;

/** The world's blocks as an obstacle for the smooth cloth (docs/tecido-arquitetura.md, future work: floor, walls,
 *  fences). The renderers hand SuperficieTecido points in view space (camera-relative and rotated by the camera, the
 *  pose stack GameRenderer gives the level); each point is taken to world space, and if it is inside a block's
 *  collision box it leaves through the nearest face up or sideways (never down), with a small gap.
 *  Render-only, like the leg collision: the physics does not change, so the cloth never jitters.
 *  Only used when the outfit is drawn in the world (com() checks the posed legs are where the entity is), not in the
 *  inventory or other previews. Block boxes are cached for the frame. */
public final class ColisaoCenario implements SuperficieTecido.Obstaculo {
    private static final double FOLGA=.02;
    private final Level mundo;private final Vec3 camera;private final Matrix3f paraMundo,paraVista;
    private final Map<Long,List<AABB>> caixas=new HashMap<>();
    private ColisaoCenario(Level mundo,Vec3 camera,float xRot,float yRot){
        this.mundo=mundo;this.camera=camera;
        // View rotation used by GameRenderer.renderLevel: X by the pitch, then Y by the yaw + 180.
        paraVista=new Matrix3f().rotateX((float)Math.toRadians(xRot)).rotateY((float)Math.toRadians(yRot+180));
        paraMundo=new Matrix3f(paraVista).invert();
    }
    /** pernas plus the world's blocks, when the posed legs are within reach of the entity (drawn in the world). */
    public static List<SuperficieTecido.Obstaculo> com(List<SuperficieTecido.Obstaculo> pernas,Entity e){
        var mc=Minecraft.getInstance();if(e==null||mc.level==null||e.level()!=mc.level)return pernas;
        var cam=mc.gameRenderer.getMainCamera();var c=new ColisaoCenario(mc.level,cam.getPosition(),cam.getXRot(),cam.getYRot());
        boolean noMundo=false;
        for(var o:pernas)if(o instanceof SuperficieTecido.Caixa k){noMundo=c.mundo(k.o()).distanceToSqr(e.position().add(0,.4,0))<2.5*2.5;break;}
        if(!noMundo)return pernas;
        var l=new ArrayList<>(pernas);l.add(c);return l;
    }
    Vec3 mundo(double[] p){var v=new Vector3f((float)p[0],(float)p[1],(float)p[2]);paraMundo.transform(v);return camera.add(v.x(),v.y(),v.z());}
    private double[] vista(Vec3 w){var d=w.subtract(camera);var v=new Vector3f((float)d.x,(float)d.y,(float)d.z);paraVista.transform(v);return new double[]{v.x(),v.y(),v.z()};}
    private List<AABB> blocos(BlockPos bp){
        return caixas.computeIfAbsent(bp.asLong(),k->{var s=mundo.getBlockState(bp);if(s.isAir())return List.of();
            var forma=s.getCollisionShape(mundo,bp);if(forma.isEmpty())return List.of();
            var l=new ArrayList<AABB>();for(var a:forma.toAabbs())l.add(a.move(bp));return l;});
    }
    @Override public double[] fora(double[] p){
        var w=mundo(p);var bp=BlockPos.containing(w);boolean moveu=false;
        for(var a:blocos(bp)){
            if(w.x<=a.minX||w.x>=a.maxX||w.y<=a.minY||w.y>=a.maxY||w.z<=a.minZ||w.z>=a.maxZ)continue;
            // Nearest way out: up (floors, slabs), or sideways (walls, fences). Never down, so the hem never sinks.
            double cima=a.maxY-w.y,xm=w.x-a.minX,xM=a.maxX-w.x,zm=w.z-a.minZ,zM=a.maxZ-w.z;
            double m=Math.min(cima,Math.min(Math.min(xm,xM),Math.min(zm,zM)));
            if(m==cima)w=new Vec3(w.x,a.maxY+FOLGA,w.z);
            else if(m==xm)w=new Vec3(a.minX-FOLGA,w.y,w.z);else if(m==xM)w=new Vec3(a.maxX+FOLGA,w.y,w.z);
            else if(m==zm)w=new Vec3(w.x,w.y,a.minZ-FOLGA);else w=new Vec3(w.x,w.y,a.maxZ+FOLGA);
            moveu=true;
        }
        return moveu?vista(w):p;
    }
}
