"""Native Blockbench 5 projects from the same GeckoLib geometry used by the mod.

Coordinate conversion follows Blockbench's Bedrock codec (X reflection, X/Y
rotation inversion). No external model assets are imported.
"""
from pathlib import Path
import argparse,base64,copy,json,uuid,zipfile

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/arcanecontrol'
OUT=ROOT/'art/blockbench'
TEXTURE=ASSETS/'textures/armor/anime_reference_atlas.png'
PROJECTS={
    **{n:(f'geo/armaduras/{n}.geo.json',f'animations/armaduras/{n}.animation.json','Armor') for n in ['gojo','frieren','shadow','asta']},
    'asta_black':('geo/asta/forma.geo.json',None,'Entity'),
    **{n:(f'geo/nemesis/{i}.geo.json',f'animations/nemesis/{i}.animation.json','Entity') for i,n in enumerate(['toji','aurora','qual','liebe'],1)},
}

def uid(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'minezin/armor-rework/'+name))
def xyz(v):return [-v[0],v[1],v[2]]
def rotation(v):return [-v[0],-v[1],v[2]]
def read(path):return json.loads(path.read_text(encoding='utf-8'))

def make_project(name,embedded=False):
    model,animation,kind=PROJECTS[name]
    texture_path=ASSETS/'textures/armor/anime_atlas.png' if model.startswith('geo/nemesis/') else TEXTURE
    geo=read(ASSETS/model)['minecraft:geometry'][0]
    elements=[];groups=[];nodes={};groupids={b['name']:uid(name+'/'+b['name']) for b in geo['bones']}
    for bone in geo['bones']:
        gid=groupids[bone['name']];children=[]
        groups.append({'uuid':gid,'name':bone['name'],'origin':xyz(bone['pivot']),
                       'rotation':rotation(bone.get('rotation',[0,0,0])),
                       'export':True,'visibility':True,'isOpen':True,'children':[]})
        nodes[bone['name']]={'uuid':gid,'isOpen':True,'children':children}
        for index,c in enumerate(bone.get('cubes',[])):
            cid=uid(name+'/'+bone['name']+'/'+str(index));o=c['origin'];s=c['size']
            faces={}
            for face,data in c['uv'].items():
                u,v=data['uv'];w,h=data['uv_size']
                faces[face]={'uv':[u,v,u+w,v+h],'texture':0}
            elements.append({'uuid':cid,'name':bone['name']+'_'+str(index),'type':'cube','box_uv':False,
                             'from':[-o[0]-s[0],o[1],o[2]],'to':[-o[0],o[1]+s[1],o[2]+s[2]],
                             'origin':xyz(c.get('pivot',bone['pivot'])),
                             'rotation':rotation(c.get('rotation',[0,0,0])),
                             'autouv':0,'export':True,'faces':faces})
            children.append(cid)
    roots=[]
    for bone in geo['bones']:
        node=nodes[bone['name']]
        if bone.get('parent'):nodes[bone['parent']]['children'].append(node)
        else:roots.append(node)
    texture={'uuid':uid('atlas'),'name':texture_path.name,'id':'0','mode':'bitmap',
             'relative_path':'../../src/main/resources/assets/arcanecontrol/textures/armor/'+texture_path.name,
             'uv_width':geo['description']['texture_width'],'uv_height':geo['description']['texture_height']}
    if embedded:
        texture['source']='data:image/png;base64,'+base64.b64encode(texture_path.read_bytes()).decode('ascii')
        texture['internal']=True
    project={'meta':{'format_version':'5.0','model_format':'geckolib_model','box_uv':False},
             'name':name+'_reference_rework','model_identifier':geo['description']['identifier'].removeprefix('geometry.'),
             'geckolib_modid':'arcanecontrol','geckolib_model_type':kind,
             'resolution':{'width':geo['description']['texture_width'],'height':geo['description']['texture_height']},
             'elements':elements,'groups':groups,'outliner':roots,'textures':[texture]}
    if animation:
        animations=[]
        for aname,anim in read(ASSETS/animation)['animations'].items():
            animators={}
            for bname,channels in anim.get('bones',{}).items():
                if bname not in groupids:raise ValueError('Animation bone missing: '+bname)
                frames=[]
                for channel,keys in channels.items():
                    for time,value in (keys.items() if isinstance(keys,dict) else {'0':keys}.items()):
                        value=rotation(value) if channel=='rotation' else value
                        frames.append({'uuid':uid(name+aname+bname+channel+time),'channel':channel,'time':float(time),
                                       'interpolation':'linear','data_points':[dict(zip(['x','y','z'],map(str,value)))]})
                animators[groupids[bname]]={'name':bname,'type':'bone','keyframes':frames}
            animations.append({'uuid':uid(name+aname),'name':aname,'loop':'loop' if anim.get('loop') else 'once',
                               'length':anim['animation_length'],'animators':animators})
        project['animations']=animations
    return project

def write_projects(package=None):
    OUT.mkdir(parents=True,exist_ok=True)
    for name in PROJECTS:(OUT/f'{name}.bbmodel').write_bytes((json.dumps(make_project(name),ensure_ascii=False,separators=(',',':'))+'\n').encode('utf-8'))
    if package:
        package=Path(package);package.parent.mkdir(parents=True,exist_ok=True)
        with zipfile.ZipFile(package,'w',zipfile.ZIP_DEFLATED) as z:
            for name in PROJECTS:z.writestr(name+'.bbmodel',json.dumps(make_project(name,True),ensure_ascii=False,separators=(',',':')))
            z.write(OUT/'README.md','README.md')
    print('Exported',len(PROJECTS),'editable Blockbench projects.')

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--package');args=p.parse_args();write_projects(args.package)
