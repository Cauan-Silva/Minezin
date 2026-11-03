"""Offline view of the Hollow mask exactly as CamadaHollow builds it: the addBox calls of the "mask" part are read from
the Java source (texOffs picks the colour region of textures/ichigo/mascara.png) and drawn on a head-sized mannequin,
front / 3-4 / side. Not Minecraft: flat light, no player skin."""
from pathlib import Path
import re,math,argparse
import numpy as np,moderngl
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,lookat

FONTE=ROOT/'src/main/java/com/cauansilva/arcanecontrol/core/ichigo/CamadaHollow.java'
CORES={(0,0):(.93,.91,.87),(0,16):(.09,.06,.10),(18,16):(.74,.16,.26)}

def caixas():
    s=FONTE.read_text(encoding='utf-8');s=s[s.index('var mesh=new MeshDefinition();var b=CubeListBuilder'):s.index('addOrReplaceChild("mask"')]
    out=[]
    def ler(texto,amb):
        cor=None
        for m in re.finditer(r'texOffs\((\d+),(\d+)\)|addBox\(([^)]*)\)',texto):
            if m.group(1):cor=CORES[(int(m.group(1)),int(m.group(2)))];continue
            v=[float(eval(a.strip().rstrip('f').replace('f*','*').replace('f+','+').replace('f-','-'),{},amb)) for a in m.group(3).split(',')]
            out.append((v,cor))
    # Eye loop: rows {x0,x1,y} mirrored on both sides, all in the dark region.
    olhos=re.search(r'for\(int lado=-1;.*?float\[\]\[\] linhas=\{(.*?)\};.*?\}\}',s,re.S)
    if olhos:
        for l in re.findall(r'\{([^{}]*)\}',olhos.group(1)):
            x0,x1,y=[float(v.rstrip('f')) for v in l.split(',')]
            for lado in (-1,1):out.append(([x0 if lado>0 else -x1,y,-4.85,x1-x0,.32,.2],CORES[(0,16)]))
        s=s[:olhos.start()]+s[olhos.end():]
    laco=re.search(r'for\(int k=0;k<(\d+);k\+\+\)\{float x=([^;]+);(.*?)\}',s,re.S)
    ler(s[:laco.start()],{});
    for k in range(int(laco.group(1))):ler(laco.group(3),{'k':k,'x':eval(laco.group(2).replace('f',''),{},{'k':k})})
    ler(s[laco.end():],{})
    return out

def render(saida):
    out=Path(saida);out.mkdir(parents=True,exist_ok=True);w,h=520,520
    ctx=moderngl.create_standalone_context(require=330);fbo=ctx.simple_framebuffer((w*3,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST)
    prog=ctx.program(vertex_shader='#version 330\nin vec3 P;in vec3 N;in vec3 C;uniform mat4 M;out vec3 c;out vec3 n;void main(){gl_Position=M*vec4(P,1);c=C;n=N;}',
                     fragment_shader='#version 330\nin vec3 c;in vec3 n;out vec4 o;void main(){float l=.45+.55*max(0.,dot(normalize(n),normalize(vec3(-.3,.6,-1))));o=vec4(c*l,1);}')
    tri=[]
    def caixa(o,s,cor):
        x,y,z=o;W,H,D=s
        v=[np.array([x+W*(i&1),y+H*(i>>1&1),z+D*(i>>2&1)]) for i in range(8)]
        for f,n in [((0,1,3,2),(0,0,-1)),((4,6,7,5),(0,0,1)),((0,4,5,1),(0,-1,0)),((2,3,7,6),(0,1,0)),((0,2,6,4),(-1,0,0)),((1,5,7,3),(1,0,0))]:
            for k in (0,1,2,0,2,3):tri.append([*v[f[k]],*n,*cor])
    for (x,y,z,W,H,D),cor in caixas():caixa((x,-y-H,z),(W,H,D),cor)   # model y grows down: flip to world up
    caixa((-4,0,-4),(8,8,8),(.62,.48,.38))                               # head
    buf=ctx.buffer(np.array(tri,dtype='f4').tobytes());vao=ctx.vertex_array(prog,[(buf,'3f 3f 3f','P','N','C')])
    f=1/math.tan(math.radians(32)/2);proj=np.array([[f,0,0,0],[0,f,0,0],[0,0,-1.002,-.2],[0,0,-1,0]])
    ctx.clear(.10,.11,.14)
    for i,cam in enumerate([(0,4.5,-22),(-14,7,-17),(-22,4.5,0)]):
        ctx.viewport=(i*w,0,w,h);vao.program['M'].write((proj@lookat(np.array(cam,float),np.array([0,4.2,0.]))).T.astype('f4').tobytes());vao.render()
    im=Image.frombytes('RGBA',(w*3,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im)
    for i,t in enumerate(['FRENTE','3/4','LADO']):d.text((i*w+10,10),f'MASCARA HOLLOW - {t} (geometria do CamadaHollow)',fill=(230,232,240))
    im.save(out/'ichigo-mascara.png');print('ok',len(caixas()),'caixas')
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
