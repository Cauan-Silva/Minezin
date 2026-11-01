"""Actual blade geometry, scale and atlas in OpenGL; not the Minecraft hand/item renderer."""
from pathlib import Path
import argparse
import numpy as np
import moderngl
from PIL import Image,ImageDraw
from preview_armor_models import mesh,lookat,ASSETS

def render(output,backend=None):
    ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
    p=ctx.program(vertex_shader='''#version 150
    in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;
    out vec2 uv;out vec3 normal;void main(){gl_Position=Matrix*vec4(Position,1);uv=UV;normal=Normal;}''',
    fragment_shader='''#version 150
    uniform sampler2D Atlas;in vec2 uv;in vec3 normal;out vec4 fragColor;
    void main(){vec4 c=texture(Atlas,uv);if(c.a<.1)discard;float l=.5+.5*max(0.,dot(normalize(normal),normalize(vec3(-.4,.6,-1))));fragColor=vec4(c.rgb*l,c.a);}''')
    texture=Image.open(ASSETS/'textures/armor/anime_reference_atlas.png').convert('RGBA');tex=ctx.texture(texture.size,4,texture.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);tex.use();p['Atlas'].value=0
    w,h=400,600;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
    proj=np.diag([2/100,2/150,-2/400,1]);proj[2,3]=-1;view=lookat(np.array([24.,60.,-180.]),np.array([0.,60.,0.]));p['Matrix'].write((proj@view).T.astype('f4').tobytes())
    sheet=Image.new('RGB',(w*2,h+60),(10,14,22));draw=ImageDraw.Draw(sheet)
    for i,pose in enumerate(['idle','divider']):
        fbo.clear(.10,.12,.16,1,depth=1);vertices=mesh('matadora',pose)
        assert np.isfinite(vertices).all();buf=ctx.buffer(vertices[:,:8].tobytes());vao=ctx.vertex_array(p,[(buf,'3f 3f 2f','Position','Normal','UV')]);vao.render(moderngl.TRIANGLES)
        image=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM);sheet.paste(image.convert('RGB'),(i*w,0))
        draw.text((i*w+12,12),'BASE'if i==0 else 'BLACK DIVIDER - TICK 18',fill=(235,238,247));vao.release();buf.release()
    draw.text((12,h+12),'GEOMETRIA E TEXTURA DO MOD - GUARDA E EMPUNHADURA PRESERVADAS',fill=(235,238,247))
    draw.text((12,h+34),'Sem pose da mao, animacao do jogador ou iluminacao Minecraft.',fill=(162,180,204))
    output=Path(output);output.parent.mkdir(parents=True,exist_ok=True);sheet.save(output);print('Divider model GPU:',ctx.info['GL_RENDERER'],output)
    p.release();tex.release();fbo.release();ctx.release()
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--backend');a=p.parse_args();render(a.output,a.backend)
