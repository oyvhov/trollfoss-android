"""Unify Rumle's shoulder and soften the small lookout cairn. Idempotent.

The unmodified arm is kept as an unlinked source mesh; exports include only the
finished arm, preserving the existing animation pivot and all finger shapes.
"""
import bpy, ast, pathlib, math
from math import sin, cos, pi
from mathutils import Vector, noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
M=bpy.data.materials
current=bpy.data.collections['Rumle • waving hand']
linked=current.name not in scene.collection.children
if linked:scene.collection.children.link(current)
original=bpy.data.meshes.get('Rumle • original arm source')
arm=next(o for o in current.objects if o.name=='Rumle • articulated arm')
if original is None:
    original=arm.data.copy();original.name='Rumle • original arm source';original.use_fake_user=True
arm.data=original.copy()
for ob in list(current.objects):
    if ob.name.startswith('Rumle • rounded shoulder'):
        bpy.data.objects.remove(ob,do_unlink=True)
cap=ell('Rumle • rounded shoulder',(0,0,-.015),(.575,.545,.53),M['Warm granite skin'],32,20)
bpy.ops.object.select_all(action='DESELECT')
arm.select_set(True);cap.select_set(True);bpy.context.view_layer.objects.active=arm
bpy.ops.object.join()
r=arm.modifiers.new('Continuous shoulder volume','REMESH');r.mode='VOXEL';r.voxel_size=.032;apply(arm,r)
s=arm.modifiers.new('Gentle shoulder polish','SMOOTH');s.factor=.65;s.iterations=3;apply(arm,s)
d=arm.modifiers.new('Mobile hand topology','DECIMATE');d.ratio=.48;apply(arm,d)
for p in arm.data.polygons:p.use_smooth=True
assert len(arm.data.vertices)>1500
if linked:scene.collection.children.unlink(current)

current=bpy.data.collections['Heileberget • landscape']
for ob in list(current.objects):
    if ob.name.startswith('Lookout • cairn'):bpy.data.objects.remove(ob,do_unlink=True)
# Use the exact ground height of the current terrain, which differs from the
# original build helper around the tarn. The ledge is far from the water basin.
def ground(x,y):
    z=-y;ridge=max(0,min(1,(3-z)/24))*6
    lake=((x+3)/10)**2+((z-1)/8)**2
    return 1.65+ridge+.22*sin(x*.44)*cos(z*.3)-6.2*max(0,1-lake)+math.exp(-((x+3)**2+(z-1)**2)*.11)*6.2
base=ground(-21.5,20.3)
for j in range(5):
    ob=ell('Lookout • cairn',(-21.5+.025*sin(j*3),20.3+.018*cos(j*2),base+.12+j*.23),
           (.72-j*.105,.55-j*.075,.165),M['Granite 02'],20,12)
    for v in ob.data.vertices:
        v.co.x*=1+.045*sin(v.co.y*11+j)
        v.co.z+=.011*sin(v.co.x*8+j)*cos(v.co.y*7)
    ob.rotation_euler.z=j*.41
    ob.data.update()
    attr=ob.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in ob.data.vertices:
        shade=.96+.04*noise.noise_vector(v.co*5)[0]
        c=rgb(['AEB4B7','B5B5AA','A1ABB3','BABBAF','B0B7B9'][j])
        attr.data[v.index].color=(*[linear(q*shade) for q in c],1)
    ob.data.materials.clear();ob.data.materials.append(M['Painted natural albedo'])
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('UNIFIED_SHOULDER_AND_ROUNDED_CAIRN',len(arm.data.polygons),'arm polygons')
