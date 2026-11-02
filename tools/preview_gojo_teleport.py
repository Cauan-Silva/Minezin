"""Offline Gojo teleport approval: runtime GLSL and Java-exported geometry, no Minecraft."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh
from preview_armor_models import ROOT,ASSETS,lookat,translation,mesh as armor_mesh

def render(output):
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'gojo_energy');add=load_program(ctx,'gojo_energy_additive')
 data=json.loads((ROOT/'build/gojo-teleporte-samples.json').read_text(encoding='utf-8'))
 w,h=720,480;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 cam=np.array([13.,5.,-13.]);center=np.array([0.,0.,0.]);view=lookat(cam,np.array([4.,1.1,0.]))
 f=1/math.tan(math.radians(46)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nout vec4 fragColor;void main(){fragColor=vec4(.13,.16,.23,1);}")
 body=ctx.program(vertex_shader="#version 150\nin vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;out vec3 n;out vec2 uv;out float textured;void main(){gl_Position=Matrix*vec4(Position,1);n=Normal;uv=UV;textured=Textured;}",fragment_shader="#version 150\nuniform sampler2D Atlas;in vec3 n;in vec2 uv;in float textured;out vec4 fragColor;void main(){vec4 c=textured<.5?vec4(.55,.4,.29,1):texture(Atlas,uv);if(c.a<.01)discard;float l=.45+.55*max(0.,dot(normalize(n),normalize(vec3(.5,1.,-.6))));fragColor=vec4(c.rgb*l,1);}")
 im=Image.open(ASSETS/'textures/armor/trajes/gojo.png').convert('RGBA');tex=ctx.texture(im.size,4,im.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);body['Atlas'].value=0
 verts=armor_mesh('gojo');verts[:,:3]/=16;bodybuffer=ctx.buffer(verts.tobytes());bodyvao=ctx.vertex_array(body,[(bodybuffer,'3f 3f 2f 1f','Position','Normal','UV','Textured')])
 def submit(program,mode,verts,t,additive=False,local=False):
  if not len(verts):return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  program['ModelViewMat'].write((view@translation(center) if local else view).T.astype('f4').tobytes());program['ProjMat'].write(proj.T.tobytes());program['EffectTime'].value=t/20;program['EffectMode'].value=mode
  viewer=cam-center;viewer/=np.linalg.norm(viewer);program['Viewer'].value=tuple(viewer)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(program,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def ribbon(out,a,b,width,alpha,color,u0,u1):
  side=np.cross(b-a,cam-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side*=width/n;pts=[a-side,b-side,b+side,a+side];uv=[(u0,0),(u1,0),(u1,1),(u0,1)]
  for k in [0,1,2,0,2,3]:out.append([*pts[k],*color,alpha,*uv[k]])
 def smooth_ribbon(out,points,width,color,lights):
  sides=[]
  for i,p in enumerate(points):
   tangent=points[min(len(points)-1,i+1)]-points[max(0,i-1)];side=np.cross(tangent,cam-p);length=np.linalg.norm(side)
   side=side/length if length>1e-8 else sides[-1]/np.linalg.norm(sides[-1]) if sides else np.array([1.,0.,0.]);sides.append(side*width)
  for i in range(1,len(points)):
   pts=[points[i-1]-sides[i-1],points[i]-sides[i],points[i]+sides[i],points[i-1]+sides[i-1]];u=(i-1)/(len(points)-1);v=i/(len(points)-1);uv=[(u,0),(v,0),(v,1),(u,1)];alpha=[lights[i-1],lights[i],lights[i],lights[i-1]]
   for k in [0,1,2,0,2,3]:out.append([*pts[k],*color,alpha[k],*uv[k]])
 def raw(p):return np.array([p['x'],p['y'],p['z']])
 def portal(p,anchor):return anchor+np.array([p['z'],p['y'],p['x']])
 def draw(t):
  fbo.use();ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
  fbo.depth_mask=True;fbo.clear(.002,.004,.012,1,depth=1)
  sky=mesh(100,32,(1.,1.,1.,1.));sky[:,:3]+=cam;fbo.depth_mask=False;submit(base,14,sky,t+80)
  fbo.depth_mask=True;grid=[]
  for i in range(-16,21,2):grid.extend([[-16,0,i],[20,0,i],[i,0,-16],[i,0,20]])
  line['Matrix'].write((proj@view).T.astype('f4').tobytes());b=ctx.buffer(np.asarray(grid,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
  anchor=np.array([8.,0.,0.]) if t>=0 else np.array([0.,0.,0.]);body['Matrix'].write((proj@view@translation(anchor)).T.astype('f4').tobytes());tex.use();ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE_MINUS_SRC_ALPHA);bodyvao.render();fbo.depth_mask=False
  if 0<=t<=18:
   info=data[str(t)];out=[]
   for end,side in enumerate(info['pontas']):
    anchor=np.array([end*8.,0.,0.]);alpha=side['alpha']
    for points in side['arcos']:
     smooth_ribbon(out,[portal(p,anchor) for p in points],.19,(.153,.616,1.),[alpha*math.sin(i/32*math.pi)*.45 for i in range(33)])
     smooth_ribbon(out,[portal(p,anchor) for p in points],.055,(.647,.929,1.),[alpha*math.sin(i/32*math.pi) for i in range(33)])
    for p in side['particulas']:
     q=portal(p,anchor);ribbon(out,q,q+np.array([0,.12,0]),.04,alpha*.8,(.718,.945,1.),0,1)
   for points in info['rastros']:
    smooth_ribbon(out,[raw(p) for p in points],.14,(.16,.608,1.),[info['alphaRastro']*.35]*len(points));smooth_ribbon(out,[raw(p) for p in points],.035,(.725,.961,1.),[info['alphaRastro']]*len(points))
   submit(add,24,out,t,True)
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im);d.text((12,12),'GOJO / TELEPORTE / '+('ANTES' if t<0 else 'tick '+str(t)),fill=(235,240,255));d.text((12,h-22),'OFFLINE / CORPO ESTATICO / DESLOCAMENTO INSTANTANEO / VFX DO MOD',fill=(165,182,207));return im
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 frames=[draw(t) for t in range(-8,27)];frames[0].save(output/'gojo-teleporte.gif',save_all=True,append_images=frames[1:],duration=50,loop=0)
 sheet=Image.new('RGB',(w*2,h*2),(9,12,20))
 for i,t in enumerate([-2,0,4,12]):sheet.paste(draw(t),(i%2*w,i//2*h))
 sheet.save(output/'gojo-teleporte-fases.png');print('Teleport GPU:',ctx.info['GL_RENDERER'],'frames:',len(frames))
 bodyvao.release();bodybuffer.release();tex.release();body.release()
 for r in [base,add,line,fbo]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);a=p.parse_args();render(a.output)
