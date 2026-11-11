"""A small original blue-and-honey woodland bird with a pivoted feathered wing.
The Android renderer mirrors the one wing and flaps both around their shoulders.
"""
import bpy,ast,pathlib,math
from math import sin,cos,pi
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
def colour(name,h):
    m=bpy.data.materials.get(name) or bpy.data.materials.new(name)
    m.use_nodes=True;c=rgb(h);m['game_color']=list(c);m['emission']=0
    m.diffuse_color=(*[linear(v) for v in c],1)
    p=next(n for n in m.node_tree.nodes if n.type=='BSDF_PRINCIPLED')
    p.inputs['Base Color'].default_value=m.diffuse_color;p.inputs['Roughness'].default_value=.85
    return m
blue=colour('Bird • slate blue','587D8D');navy=colour('Bird • deep cap','35566C')
cream=colour('Bird • warm breast','EAD19A');ivory=colour('Bird • cheek','F7ECDD')
ink=colour('Bird • little eyes','202D38');beak=colour('Bird • beak','AD865A')
groups={}
for part in ['body','wing']:
    name='Bird • '+part;old=bpy.data.collections.get(name)
    if old:
        for ob in list(old.objects):bpy.data.objects.remove(ob,do_unlink=True)
        bpy.data.collections.remove(old)
    groups[part]=bpy.data.collections.new(name)
    scene.collection.children.link(groups[part])
current=groups['body']
ell('Bird rounded body',(0,.02,0),(.135,.24,.145),blue,20,12)
ell('Bird honey breast',(0,-.08,-.032),(.119,.155,.112),cream,18,12)
ell('Bird round head',(0,-.20,.12),(.113,.12,.114),navy,20,12)
for side in [-1,1]:
    ell('Bird cream cheek',(side*.085,-.245,.097),(.033,.063,.052),ivory,14,10)
    ell('Bird bright eye',(side*.094,-.267,.135),(.014,.014,.018),ink,12,8)
    ell('Bird eye glint',(side*.102,-.277,.143),(.004,.005,.005),ivory,8,6)
tube('Bird little beak',[(0,-.302,.111),(0,-.366,.096)],[.032,.003],beak,8)
for i in range(3):
    ob=ell('Bird tail feather',((i-1)*.038,.285,-.015),(.036,.16,.025),navy if i!=1 else blue,12,8)
    ob.rotation_euler[2]=(i-1)*-.16
current=groups['wing']
ell('Bird wing shoulder',(.10,.035,0),(.14,.115,.045),blue,18,10)
for i in range(5):
    ob=ell('Bird flight feather',(.19+i*.035,.09+i*.036,-.006),(.24-i*.018,.046,.023),navy if i>2 else blue,16,8)
    ob.rotation_euler[2]=.12+i*.13
for i in range(3):
    ob=ell('Bird pale wing bar',(.12+i*.075,.025+i*.036,.032),(.048,.024,.009),ivory,12,6)
    ob.rotation_euler[2]=.5
stage=bpy.data.collections['Heileberget • landscape']
for ob in list(stage.objects):
    if ob.name.startswith('Bird preview •'):bpy.data.objects.remove(ob,do_unlink=True)
for part,col in groups.items():
    for side in ([-1,1] if part=='wing' else [0]):
        ob=bpy.data.objects.new('Bird preview • '+part+str(side),None);stage.objects.link(ob)
        ob.instance_type='COLLECTION';ob.instance_collection=col
        ob.location=(side*.11,0,5.02 if side else 5)
        if side:ob.scale.x=side
    scene.collection.children.unlink(col)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('BIRD_MODEL_READY')
