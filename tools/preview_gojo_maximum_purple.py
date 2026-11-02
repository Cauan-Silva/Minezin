"""Offline Maximum Purple cinematic approval: runtime GLSL and Java-exported geometry, no Minecraft."""
from pathlib import Path
import json,math,argparse,av,soundfile as sf
from fractions import Fraction
import numpy as np,moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh
from preview_armor_models import ROOT,ASSETS,lookat,translation,mesh as armor_mesh,rotation

def render(output):
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'gojo_energy');add=load_program(ctx,'gojo_energy_additive')
 payload=json.loads((ROOT/'build/gojo-roxo-maximo-samples.json').read_text(encoding='utf-8'));data=payload['quadros']
 w,h=960,600;color_texture=ctx.texture((w,h),4);color_texture.filter=(moderngl.NEAREST,moderngl.NEAREST)
 depth_texture=ctx.depth_texture((w,h));depth_texture.compare_func='';depth_texture.filter=(moderngl.NEAREST,moderngl.NEAREST)
 fbo=ctx.framebuffer(color_attachments=[color_texture],depth_attachment=depth_texture);fbo.use()
 processed=ctx.simple_framebuffer((w,h),components=4)
 post=ctx.program(vertex_shader=(ASSETS/'shaders/program/roxo_maximo_cinema.vsh').read_text(encoding='utf-8'),fragment_shader=(ASSETS/'shaders/program/roxo_maximo_cinema.fsh').read_text(encoding='utf-8'))
 ortho=np.eye(4,dtype='f4');ortho[0,0]=2/w;ortho[1,1]=2/h;ortho[0,3]=-1;ortho[1,3]=-1
 post['ProjMat'].write(ortho.T.tobytes());post['OutSize'].value=(w,h);post['InSize'].value=(w,h);post['DiffuseSampler'].value=0;post['DepthSampler'].value=1
 quad=np.array([[0,0,0],[w,0,0],[w,h,0],[0,0,0],[w,h,0],[0,h,0]],dtype='f4');quadbuffer=ctx.buffer(quad.tobytes());postvao=ctx.vertex_array(post,[(quadbuffer,'3f','Position')]);qa={}

 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 cam=np.array([3.,2.4,-4.5]);center=np.array([0.,1.62,10.]);view=lookat(cam,center)
 f=1/math.tan(math.radians(46)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nout vec4 fragColor;void main(){fragColor=vec4(.13,.16,.23,1);}")
 body=ctx.program(vertex_shader="#version 150\nin vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;out vec3 n;out vec2 uv;out float textured;void main(){gl_Position=Matrix*vec4(Position,1);n=Normal;uv=UV;textured=Textured;}",fragment_shader="#version 150\nuniform sampler2D Atlas;in vec3 n;in vec2 uv;in float textured;out vec4 fragColor;void main(){vec4 c=textured<.5?vec4(.55,.4,.29,1):texture(Atlas,uv);if(c.a<.01)discard;float l=.45+.55*max(0.,dot(normalize(n),normalize(vec3(.5,1.,-.6))));fragColor=vec4(c.rgb*l,1);}")
 im=Image.open(ASSETS/'textures/armor/trajes/gojo.png').convert('RGBA');tex=ctx.texture(im.size,4,im.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);body['Atlas'].value=0
 animation=json.loads((ASSETS/'player_animation/despertos.json').read_text(encoding='utf-8'))['animations']['desperto_1_32']['bones']
 def poses(t):
  out={}
  for bone,channels in animation.items():
   keys=sorted((float(k),v) for k,v in channels.get('rotation',{}).items());time=t/20
   if not keys:continue
   value=keys[-1][1]['vector']
   if time<=keys[0][0]:value=keys[0][1]['vector']
   else:
    for (ta,a),(tb,b) in zip(keys,keys[1:]):
     if time<=tb:
      u=(time-ta)/(tb-ta);easing=b.get('easing','linear')
      if easing=='easeOutQuad':u=1-(1-u)**2
      elif easing=='easeInQuad':u=u*u
      elif easing=='easeInOutSine':u=(1-math.cos(u*math.pi))*.5
      value=np.asarray(a['vector'])*(1-u)+np.asarray(b['vector'])*u;break
   out[bone]=list(value)
  return out
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
 def point(p):return center+np.array([-p['lateral'],p['vertical'],p['frente']])
 def world(p):return center+np.array([p['x'],p['y'],p['z']])
 def draw(t,impact_frames=1):
  nonlocal cam,view,proj
  fbo.use();ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
  info=data[str(t)];q=info['quadro'];r=info['raio'];alpha=info['alpha'];u=min(1,t/8);u=u*u*(3-2*u)
  camera=q['camera'];target=q['alvo'];eye=np.array([0.,1.62,0.]);desired=center+np.array([-camera['x'],camera['y'],camera['z']]);cam=eye*(1-u)+desired*u
  target=center+np.array([-target['x'],target['y'],target['z']]);direction=target-cam;horizontal=math.hypot(direction[0],direction[2]);impact=info['impacto']
  yaw=math.atan2(-direction[0],direction[2])+math.radians(math.sin(t*2.7)*impact);pitch=-math.atan2(direction[1],horizontal)+math.radians(math.sin(t*3.3)*impact)
  forward=np.array([-math.sin(yaw)*math.cos(pitch),-math.sin(pitch),math.cos(yaw)*math.cos(pitch)]);view=rotation([0,0,math.sin(t*2.1)*impact*.5])@lookat(cam,cam+forward)
  f=1/math.tan(math.radians(70*q['fov'])/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
  fbo.depth_mask=True;fbo.clear(.002,.004,.012,1,depth=1)
  sky=mesh(100,32,(1.,1.,1.,1.));sky[:,:3]+=cam;fbo.depth_mask=False;submit(base,14,sky,t+80)
  fbo.depth_mask=True;grid=[]
  for i in range(-30,31,2):grid.extend([[-30,0,i],[30,0,i],[i,0,-30],[i,0,30]])
  line['Matrix'].write((proj@view).T.astype('f4').tobytes());b=ctx.buffer(np.asarray(grid,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
  if np.linalg.norm(cam-eye)>.5:
   verts=armor_mesh('gojo',pose_rotations=poses(t));verts[:,:3]/=16;verts[:,0]*=-1;verts[:,2]*=-1;verts[:,3]*=-1;verts[:,5]*=-1
   body['Matrix'].write((proj@view).T.astype('f4').tobytes());tex.use();ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE_MINUS_SRC_ALPHA);b=ctx.buffer(verts.tobytes());v=ctx.vertex_array(body,[(b,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);v.render();v.release();b.release()
  fbo.depth_mask=False
  dense=alpha>.95
  if dense:ctx.enable(moderngl.CULL_FACE);fbo.depth_mask=True
  submit(base,23 if t>=90 else 2,mesh(r,22,(.702,.349,1.,alpha)),t,local=True);ctx.disable(moderngl.CULL_FACE);fbo.depth_mask=False
  submit(add,6,mesh(r*1.18,22,(.702,.349,1.,alpha*.4)),t,True,True)
  out=[]
  for j,points in enumerate(info['coroas']):
   for i in range(1,len(points)):ribbon(out,point(points[i-1]),point(points[i]),r*.055,alpha*.75,(.945,.8,1.) if j==1 else (.702,.349,1.),(i-1)/48,i/48)
  submit(add,9,out,t,True)
  out=[]
  scale=r/3.6
  for j,raw in enumerate(info['raios']):
   points=[center+(point(p)-center)*scale for p in raw];pulse=(.65+.35*max(0,math.sin(t*.22+j*1.7))**4)*alpha
   for i in range(1,len(points)):
    ribbon(out,points[i-1],points[i],.24,pulse*.55,(.702,.349,1.),0,1);ribbon(out,points[i-1],points[i],.075,pulse,(.949,.87,1.),0,1)
   for k,i in enumerate([3,5]):ribbon(out,points[i],center+(point(info['ramos'][j][k])-center)*scale,.065,pulse,(.918,.824,1.),0,1)
  submit(add,8,out,t,True)
  if t>=90:
   submit(add,22,mesh(info['explosao']['raio'],22,(.702,.349,1.,alpha*info['explosao']['alpha'])),t,True,True)
   out=[]
   for points in info['choque']:
    smooth_ribbon(out,[world(p) for p in points],.7,(.647,.165,1.),[alpha*info['alphaChoque']*.55]*len(points));smooth_ribbon(out,[world(p) for p in points],.24,(.957,.831,1.),[alpha*info['alphaChoque']]*len(points))
   submit(add,9,out,t,True)
  original=np.frombuffer(color_texture.read(),dtype='uint8').reshape(h,w,4).copy();depth_before=depth_texture.read() if t==90 else None
  color_texture.use(0);depth_texture.use(1);post['RoxoTick'].value=t;post['Strength'].value=info['explosao']['pesoPost'];post['Frames'].value=impact_frames
  clip=proj@view@np.r_[center,1.];post['ScreenCenter'].value=tuple(np.clip(clip[:2]/clip[3]*.5+.5,.04,.96)) if clip[3]>.01 else (.5,.5)
  processed.use();processed.clear(0,0,0,1);ctx.disable(moderngl.DEPTH_TEST|moderngl.BLEND);postvao.render()
  pixels=np.frombuffer(processed.read(components=4),dtype='uint8').reshape(h,w,4).copy()
  if t in [2,88,90,91,110]:qa[str(t)+'_'+str(impact_frames)]={'mean':float(pixels[:,:,:3].mean()),'source_mean':float(original[:,:,:3].mean()),'max_diff':int(np.abs(pixels.astype('i2')-original.astype('i2')).max())}
  if t==90:assert depth_before==depth_texture.read(),'Post must preserve source depth'
  im=Image.fromarray(pixels).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB')
  d=ImageDraw.Draw(im);d.text((12,12),'GOJO / ROXO MAXIMO / CUTSCENE | tick '+str(t),fill=(235,240,255));d.text((12,h-22),'OFFLINE / CAMERA + VFX + POS GLSL REAL / POSES AMOSTRADAS / FOV BASE 70',fill=(165,182,207));return im
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 frames=[draw(t) for t in range(2,111)];frames[0].save(output/'gojo-roxo-maximo-impacto.gif',save_all=True,append_images=frames[2::2],duration=100,loop=0)
 sheet=Image.new('RGB',(w*3,h*2),(9,12,20))
 for i,t in enumerate([45,88,90,91,94,100]):sheet.paste(draw(t),(i%3*w,i//3*h))
 sheet.save(output/'gojo-roxo-maximo-impacto-fases.png')
 draw(90,0)
 assert qa['2_1']['max_diff']<=1 and qa['110_1']['max_diff']<=1,'Post must fade to identity'
 assert qa['88_1']['mean']<qa['88_1']['source_mean']*.55,'Pre-impact compression must read clearly'
 assert abs(qa['90_1']['mean']-qa['90_0']['mean'])>12,'Frames-off must suppress paper impact'
 assert abs(qa['90_1']['mean']-qa['91_1']['mean'])>30,'Paper and negative impact frames must differ'
 (output/'gojo-roxo-maximo-impacto-gpu.json').write_text(json.dumps(qa,indent=2),encoding='utf-8');print('Maximum Purple GPU checks: identity, compression, frames-off, distinct stages, preserved depth')
 container=av.open(str(output/'gojo-roxo-maximo-impacto.mp4'),'w');stream=container.add_stream('libx264',rate=20);stream.width=w;stream.height=h;stream.pix_fmt='yuv420p';stream.options={'crf':'20'}
 audio=container.add_stream('aac',rate=48000);audio.layout='mono';audio.bit_rate=128000
 for im in frames:
  for packet in stream.encode(av.VideoFrame.from_image(im)):container.mux(packet)
 for packet in stream.encode():container.mux(packet)
 samples=np.zeros(round(len(frames)/20*48000),dtype='f4')
 for cue in payload['audio']:
  sound,rate=sf.read(ASSETS/('sounds/anime/'+cue['nome']+'.ogg'),dtype='float32');assert rate==48000
  sound=sound[:cue['vida']*2400]*cue['volume']*.7;offset=round((cue['atraso']-2)/20*48000)
  if offset<0:sound=sound[-offset:];offset=0
  length=min(len(sound),len(samples)-offset)
  if length>0:samples[offset:offset+length]+=sound[:length]
 samples=np.clip(samples,-.98,.98)
 for start in range(0,len(samples),1024):
  frame=av.AudioFrame.from_ndarray(samples[start:start+1024][None,:].copy(),format='fltp',layout='mono');frame.sample_rate=48000;frame.pts=start;frame.time_base=Fraction(1,48000)
  for packet in audio.encode(frame):container.mux(packet)
 for packet in audio.encode():container.mux(packet)
 container.close();tex.release();body.release();print('Maximum Purple GPU:',ctx.info['GL_RENDERER'],'frames:',len(frames))
 for r in [base,add,line,fbo,processed,color_texture,depth_texture,post,postvao,quadbuffer]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);a=p.parse_args();render(a.output)
