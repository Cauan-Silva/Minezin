"""Export-contract regressions, including Blockbench's mirrored coordinate system."""
import unittest
from pathlib import Path
from export_blockbench_projects import ASSETS,OUT,PROJECTS,make_project,read,xyz,rotation

class BlockbenchProjectsTest(unittest.TestCase):
    def test_saved_projects_match_geometry_and_have_no_orphan_nodes(self):
        for name,(model,animation,kind) in PROJECTS.items():
            with self.subTest(project=name):
                project=read(OUT/(name+'.bbmodel'))
                self.assertEqual('5.0',project['meta']['format_version'])
                self.assertEqual(kind,project['geckolib_model_type'])
                self.assertEqual('geckolib_model',project['meta']['model_format'])
                self.assertTrue((OUT/project['textures'][0]['relative_path']).resolve().is_file())
                geo=read(ASSETS/model)['minecraft:geometry'][0]
                groups={g['name']:g for g in project['groups']};elements={e['uuid']:e for e in project['elements']}
                seen=set();parents={}
                def visit(nodes,parent=None):
                    for node in nodes:
                        if isinstance(node,str):
                            self.assertIn(node,elements);self.assertNotIn(node,seen);seen.add(node);parents[node]=parent
                        else:
                            self.assertIn(node['uuid'],{g['uuid'] for g in groups.values()});visit(node['children'],node['uuid'])
                visit(project['outliner']);self.assertEqual(set(elements),seen)
                ordered=iter(project['elements'])
                for bone in geo['bones']:
                    group=groups[bone['name']]
                    self.assertEqual(xyz(bone['pivot']),group['origin'])
                    self.assertEqual(rotation(bone.get('rotation',[0,0,0])),group['rotation'])
                    for cube in bone.get('cubes',[]):
                        element=next(ordered);o=cube['origin'];s=cube['size'];frm=element['from'];to=element['to']
                        # Reverse the published Bedrock convention, then compare every extent.
                        recovered=[-to[0],frm[1],frm[2]]
                        for expected,actual in zip(o,recovered):self.assertAlmostEqual(expected,actual,places=6)
                        for expected,actual in zip(s,[to[i]-frm[i] for i in range(3)]):self.assertAlmostEqual(expected,actual,places=6)
                        self.assertEqual(group['uuid'],parents[element['uuid']])
                        self.assertEqual(xyz(cube.get('pivot',bone['pivot'])),element['origin'])
                        for f,v in cube['uv'].items():
                            self.assertEqual(0,element['faces'][f]['texture']);u=element['faces'][f]['uv']
                            for expected,actual in zip(v['uv_size'],[u[2]-u[0],u[3]-u[1]]):self.assertAlmostEqual(expected,actual,places=6)
                self.assertIsNone(next(ordered,None))

    def test_embedded_archive_does_not_require_machine_specific_texture_paths(self):
        for name in PROJECTS:
            p=make_project(name,True)
            self.assertTrue(p['textures'][0]['source'].startswith('data:image/png;base64,iVBOR'))
            self.assertNotIn('C:',str(p))

    def test_all_animation_tracks_target_existing_groups(self):
        for name in PROJECTS:
            p=read(OUT/(name+'.bbmodel'));ids={g['uuid'] for g in p['groups']}
            for animation in p.get('animations',[]):
                self.assertEqual('loop',animation['loop']);self.assertGreater(animation['length'],0)
                for key,animator in animation['animators'].items():
                    self.assertIn(key,ids)
                    for frame in animator['keyframes']:
                        self.assertGreaterEqual(frame['time'],0);self.assertLessEqual(frame['time'],animation['length'])
                        self.assertEqual({'x','y','z'},set(frame['data_points'][0]))

if __name__=='__main__':unittest.main()
