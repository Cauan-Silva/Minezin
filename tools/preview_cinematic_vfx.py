"""Temporal composition review using the real GLSL. Reference geometry, not Minecraft footage.

No game renderer, Photon bloom, characters, damage or terrain simulation is reproduced.
"""
from pathlib import Path
import argparse,math
import numpy as np
import moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh
from preview_power_vfx import ring
from preview_armor_models import lookat

UP=np.array([0.,1.,0.])

def ribbons(paths,camera):
    out=[]
    for a,b,width,color,u,v in paths:
        a,b=np.asarray(a,dtype=float),np.asarray(b,dtype=float)
        side=np.cross(b-a,camera-a);length=np.linalg.norm(side)
        if length<1e-8:continue
        side*=width/length;points=[a-side,b-side,b+side,a+side];uv=[(u,0),(v,0),(v,1),(u,1)]
        for k in [0,1,2,0,2,3]:out.append([*points[k],*color,*uv[k]])
    return np.array(out,dtype='f4').reshape((-1,9))

def shifted(vertices,pos):
    out=vertices.copy();out[:,:3]+=pos;return out

def arc(radius,progress,width,color):
    paths=[];angle=math.pi*1.5
    for i in range(36):
        u=i/36;a=-angle/2+(u+progress-.5)*angle;b=a+angle/36
        p=[0,math.cos(a)*radius,-2.1-math.sin(a)*radius]
        q=[0,math.cos(b)*radius,-2.1-math.sin(b)*radius]
        paths.append((p,q,width*math.sqrt(math.sin(u*math.pi)),color,u*4,(u+1/36)*4))
    return paths

def render(output,backend=None):
    ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
    programs={name:load_program(ctx,name,'one' if name=='atomic_energy' else None)
              for name in ['anime_energy','anime_energy_additive','atomic_energy']}
    w,h=360,310;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use()
    ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
    sheet=Image.new('RGB',(w*4,h*3+70),(10,14,23));text=ImageDraw.Draw(sheet)
    violet=(.72,.42,1,1);white=(.94,.91,1,1);gold=(.92,.81,.58,1);blue=(.65,.73,.95,1)
    frames=[('ATOMIC',35),('ATOMIC',75),('ATOMIC',96),('ATOMIC',128),
            ('SINGULARIDADE',35),('SINGULARIDADE',100),('SINGULARIDADE',215),('SINGULARIDADE',225),
            ('BLACK DIVIDER',13),('BLACK DIVIDER',17),('BLACK DIVIDER',18),('BLACK DIVIDER',24)]
    for index,(kind,t)in enumerate(frames):
        atomic=kind=='ATOMIC';hole=kind=='SINGULARIDADE'
        camera=np.array([12.,15.,38.])if atomic else np.array([0.,8.,30.])if hole else np.array([13.,7.,17.])
        original_camera=camera.copy()
        target=np.array([0.,12.,0.])if atomic else np.zeros(3)
        view=lookat(camera,target).astype('f4');span=48 if atomic else 34 if hole else 12
        proj=np.diag([2/(span*w/h),2/span,-2/200,1.]).astype('f4');proj[2,3]=-1
        fbo.depth_mask=True;fbo.clear(.025,.032,.048,1,depth=1)
        def draw(name,mode,vertices,add=False,depth=False):
            if not len(vertices):return
            assert np.isfinite(vertices).all(),(kind,t,mode)
            p=programs[name];p['ProjMat'].write(proj.T.tobytes());p['ModelViewMat'].write(view.T.tobytes())
            p['EffectTime'].value=t/20;p['EffectMode'].value=mode
            if 'Viewer'in p:p['Viewer'].value=tuple(camera/np.linalg.norm(camera))
            fbo.depth_mask=depth;ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if add else moderngl.ONE_MINUS_SRC_ALPHA)
            buf=ctx.buffer(vertices.tobytes());vao=ctx.vertex_array(p,[(buf,'3f 4f 2f','Position','Color','UV0')]);vao.render(moderngl.TRIANGLES);vao.release();buf.release()
        # Neutral spatial grid makes silhouette and scale readable without pretending to be terrain.
        paths=[]
        for k in range(-4,5):
            paths.extend([([-16,-4,k*4],[16,-4,k*4],.012,(.11,.15,.23,.7),0,1),([k*4,-4,-16],[k*4,-4,16],.012,(.11,.15,.23,.7),0,1)])
        draw('anime_energy',8,ribbons(paths,camera))
        if atomic:
            if t<90:
                paths=[];charge=min(1,t/90)
                for j in range(9):
                    previous=None
                    for i in range(29):
                        u=i/28;r=(1-u)*(5.5-2*charge);a=j*2.399963+u*5-t*.035
                        warp=math.sin(u*18+j*3+t*.09)*(1-u)*.22
                        point=np.array([math.cos(a)*r+warp,(1-u)*(1+math.sin(j*1.7)*2)+math.sin(u*math.pi)*.8,math.sin(a)*r-warp])
                        if previous is not None:paths.append((previous,point,.07+t*.001,white,(i-1)/28*4,u*4))
                        previous=point
                draw('atomic_energy',0,ribbons(paths,camera),True)
                for j in range(3):draw('anime_energy_additive',6,shifted(ring(1.2+j*.65,1.245+j*.65,UP,violet),[0,.05+j*.01,0]),True)
            else:
                age=t-90;fade=max(0,1-age/150);r=(.6+16*(1-math.exp(-age/13)))*fade
                draw('atomic_energy',1,shifted(mesh(r,detail=28,color=(1,1,1,fade*.7)),[0,1,0]),True)
                for j in range(3):
                    radius=18*(1-math.exp(-max(0,age-j*5)/13))
                    if radius>0:draw('atomic_energy',0,shifted(ring(max(0,radius-.16),radius,UP,(*violet[:3],fade*.65)),[0,.2+j*.3,0]),True)
            if 90<=t<130:
                age=t-90;alpha=max(0,1-age/40)**1.5;paths=[]
                for j in range(8):
                    a=j*math.pi/4
                    paths.append(([math.cos(a)*2,.3,math.sin(a)*2],[math.cos(a)*5,14+math.sin(j*2)*3,math.sin(a)*5],.1,(*white[:3],alpha*.5),0,5))
                draw('anime_energy_additive',1,ribbons(paths,camera),True)
            if 50<=t<170:
                u=max(0,min(1,(t-50)/40));base=48*(1-u*u*(3-2*u));r=.55 if t<90 else 1.6
                paths=[]
                for j in range(12):
                    a=j*math.pi/6;p=[math.cos(a)*r,base,math.sin(a)*r];q=[p[0],52,p[2]]
                    paths.append((p,q,.32 if t<90 else .7,(1,1,1,.8),0,8))
                draw('atomic_energy',2,ribbons(paths,camera),True)
        elif hole:
            u=max(0,min(1,t/70));r=.15+4.65*u*u*(3-2*u)
            if t>=220:
                u=min(1,(t-220)/14);r*=1-u*u*(3-2*u)
            draw('anime_energy',3,mesh(r,detail=24,color=blue),depth=True)
            draw('anime_energy_additive',9,ring(r*1.12,r*2.45,camera,(*blue[:3],.65)),True)
            draw('anime_energy_additive',4,ring(r*1.3,r*2.8,[math.sin(t*.008)*.3,1,math.cos(t*.008)*.2],gold),True)
            draw('anime_energy_additive',1,ring(r*1.03,r*1.08,camera,white),True)
            paths=[]
            for j in range(8):
                previous=None
                for i in range(25):
                    u=i/24;a=j*math.tau/8+u*6-t*.04;radius=(1-u)*14+r
                    p=[math.cos(a)*radius,math.sin(u*math.pi+j)*1.5*(1-u),math.sin(a)*radius]
                    if previous is not None:paths.append((previous,p,.035,(*blue[:3],.55),(i-1)/24*5,u*5))
                    previous=p
            draw('anime_energy_additive',1,ribbons(paths,camera),True)
            if t>=220:draw('anime_energy_additive',4,ring(max(0,(t-220)*.5-.3),(t-220)*.5,UP,gold),True)
        else:
            alpha=0 if t<16 or t>=30 else min(1,(t-16)/2)*max(0,(30-t)/12)**1.5
            progress=max(0,min(1,(t-16)/3))
            if t<16:
                draw('anime_energy_additive',1,ribbons([([0,-.3,0],[0,1.7,0],.05,(*white[:3],.5),0,2)],camera),True)
            else:
                draw('anime_energy',7,ribbons(arc(4,progress,.32,(.075,.035,.08,alpha)),camera))
                draw('anime_energy_additive',10,ribbons(arc(4.05,progress,.065,(.93,.15,.3,alpha)),camera),True)
        assert np.array_equal(camera,original_camera),'Geometry changed the camera position'
        tile=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM)
        pixels=np.asarray(tile);assert pixels[:,:,:3].std()>2,(kind,t,'blank frame')
        x=index%4*w;y=index//4*h;sheet.paste(tile.convert('RGB'),(x,y))
        text.text((x+12,y+12),f'{kind} | TICK {t}',fill=(235,237,248))
    text.text((12,h*3+12),'MATERIAIS GLSL DO MOD - COMPOSICOES DE REFERENCIA, FORA DO MINECRAFT',fill=(231,235,245))
    text.text((12,h*3+34),'Sem Photon bloom, personagens ou simulacao de dano/terreno. Nao certifica a aparencia no jogo.',fill=(159,177,200))
    output=Path(output);output.parent.mkdir(parents=True,exist_ok=True);sheet.save(output)
    print('Cinematic GPU:',ctx.info['GL_RENDERER']);print('Rendered 12 temporal compositions:',output)
    for p in programs.values():p.release()
    fbo.release();ctx.release()

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--backend');a=p.parse_args();render(a.output,a.backend)
