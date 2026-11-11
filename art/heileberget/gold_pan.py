"""A real shallow gold-washing bowl, exported separately for gentle rocking."""
import bpy,ast,pathlib,math
from math import sin,cos,pi
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
outside=bpy.data.collections['Heileberget • landscape'];M=bpy.data.materials
for ob in list(outside.objects):
    if ob.name.startswith(('Gold washing pan','Gold in pan','Gold pan preview')):bpy.data.objects.remove(ob,do_unlink=True)
current=bpy.data.collections.get('Gold pan • animated')
if current is None:
    current=bpy.data.collections.new('Gold pan • animated');scene.collection.children.link(current)
for ob in list(current.objects):bpy.data.objects.remove(ob,do_unlink=True)
def material(name,h):return M.get(name) or mat(name,h,.33,.45)
metal=material('Gold pan • forest enamel','527F79')
rim=material('Gold pan • worn rim','ACC4B0')
# Closed cross-section: underside, rolled lip, inward-sloping bowl and flat floor.
section=[(.01,0),(.45,0),(.58,.045),(.77,.22),(.79,.255),(.775,.28),(.75,.27),(.56,.10),(.43,.06),(.01,.06)]
verts=[];faces=[];sides=48
for radius,z in section:
    for i in range(sides):
        a=i*2*pi/sides;verts.append((radius*cos(a),radius*sin(a),z))
for j in range(len(section)):
    for i in range(sides):
        a=j*sides+i;b=j*sides+(i+1)%sides;c=((j+1)%len(section))*sides
        faces.append((a,b,c+(i+1)%sides,c+i))
ob=mesh('Gold pan • smooth bowl',verts,faces,metal,True)
ob.data.materials.append(rim)
for p in ob.data.polygons:
    if p.index//sides in [3,4,5]:p.material_index=1
# Three inner riffles catch the little stones without looking like stacked plates.
for j in range(3):
    radius=.54+j*.053;z=.105+j*.047
    points=[(radius*cos(i*2*pi/48),radius*sin(i*2*pi/48),z) for i in range(49)]
    tube('Gold pan • riffle',points,[.012]*49,rim,6)
ob=bpy.data.objects.new('Gold pan preview',None);outside.objects.link(ob)
ob.instance_type='COLLECTION';ob.instance_collection=current
x=-14.8;z=3
ground=1.65+max(0,min(1,(3-z)/24))*6+.22*sin(x*.44)*cos(z*.3)
ob.location=(x,-z,ground+.09)
scene.view_layers[0].layer_collection.children[current.name].exclude=True
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('GOLD_PAN_READY')
