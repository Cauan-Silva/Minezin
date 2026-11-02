"""Segment only Gojo's outer hem and collar; preserve the source texture and shoulders."""
from pathlib import Path
import copy,json,sys
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/arcanecontrol'

def cut_uv(source,origin,size):
 out=copy.deepcopy(source['uv']);so=source['origin'];ss=source['size']
 for face,uv in out.items():
  ax=0 if face in ['north','south'] else 2 if face in ['east','west'] else 0
  ay=1 if face in ['north','south','east','west'] else 2
  w,h=uv['uv_size']
  fx=min(1,max(1/w,size[ax]/ss[ax]));fy=min(1,max(1/h,size[ay]/ss[ay]))
  x=max(0,min(1-fx,(origin[ax]-so[ax])/ss[ax]));y=max(0,min(1-fy,(so[ay]+ss[ay]-origin[ay]-size[ay])/ss[ay]))
  u,v=uv['uv'];w,h=uv['uv_size'];uv['uv']=[round(u+x*w,6),round(v+y*h,6)];uv['uv_size']=[round(w*fx,6),round(h*fy,6)]
 return out

def build():
 data=json.loads((ROOT/'art/gojo/gojo-base.geo.json').read_text(encoding='utf-8'));geo=data['minecraft:geometry'][0];bones=geo['bones'];body=next(b for b in bones if b['name']=='armorBody');source=copy.deepcopy(body['cubes'][0]);hem=copy.deepcopy(body['cubes'][5]);collar=copy.deepcopy(body['cubes'][1:5]);body['cubes']=[body['cubes'][0]]
 bones.append({'name':'gojo_collar','parent':'armorBody','pivot':[0,22.4,0],'cubes':collar})
 for side in ['back','front','left','right']:
  count=3 if side in ['back','front'] else 2
  for col in range(count):
   for row in range(2):
    top=14.6-row*1.3;bottom=top-1.33;name=f'gojo_hem_{side}_{col}_{row}'
    if side in ['back','front']:
     x=(col-1)*2.8;z=2.14 if side=='back' else -2.30;origin=[x-1.47,bottom,z];size=[2.94,1.33,.16];pivot=[x,top,2.16 if side=='back' else -2.16]
    else:
     z=(col-.5)*2.1;x=-4.32 if side=='left' else 4.16;origin=[x,bottom,z-1.2];size=[.16,1.33,2.4];pivot=[-4.18 if side=='left' else 4.18,top,z]
    cube={'origin':origin,'size':size,'uv':cut_uv(source if row==0 else hem,origin,size)}
    bones.append({'name':name,'parent':'armorBody' if row==0 else f'gojo_hem_{side}_{col}_{row-1}','pivot':pivot,'cubes':[cube]})
 path=ASSETS/'geo/armaduras/gojo.geo.json';path.write_text(json.dumps(data,separators=(',',':'))+'\n',encoding='utf-8')
 inventory=copy.deepcopy(data);g=inventory['minecraft:geometry'][0];g['description']['identifier']='geometry.gojo_chestplate'
 for b in g['bones']:
  if 'parent' not in b and b['name'] not in ['armorBody','armorLeftArm','armorRightArm']:b['cubes']=[]
 (ASSETS/'geo/armaduras/gojo_chestplate.geo.json').write_text(json.dumps(inventory,separators=(',',':'))+'\n',encoding='utf-8')
 sys.path.insert(0,str(ROOT/'tools'));from export_blockbench_projects import make_project,OUT
 (OUT/'gojo.bbmodel').write_text(json.dumps(make_project('gojo'),ensure_ascii=False,separators=(',',':'))+'\n',encoding='utf-8')
 print('Gojo: outer hem in 20 linked bones, firm collar in one bone; shoulders/texture preserved.')
if __name__=='__main__':build()
