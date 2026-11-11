"""Clear blue mountain air, warmer granite, and a richer meadow. Colour only.

The same world-coordinate colour function paints the plateau and its skirt, so
the change cannot introduce a new seam. No extra geometry or mobile shader cost.
"""
import bpy, ast, pathlib, math
from mathutils import Vector, noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
M=bpy.data.materials
def clamp(x):return max(0,min(1,x))
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def paint(ob,fn):
    attr=ob.data.color_attributes.get('Albedo') or ob.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in ob.data.vertices:
        p=ob.matrix_basis@v.co
        attr.data[v.index].color=(*[linear(clamp(q)) for q in fn(p,v.normal)],1)
def ground(p,n):
    t=clamp(.5+noise.noise_vector(Vector((p.x*.13,p.y*.13,4)))[0]*1.4)
    green=blend(rgb('619460'),rgb('B5C779'),t)
    stone=clamp((abs(noise.noise_vector(Vector((p.x*.16,p.y*.16,1)))[0])-.30)*4.5)
    return blend(green,rgb('ADB3B3'),stone*.78)
for ob in bpy.data.collections['Heileberget • landscape'].objects:
    if ob.type!='MESH':continue
    if ob.name.startswith(('Continuous walkable','Valley beyond')):paint(ob,ground)
    elif ob.name.startswith(('Tarn shoreline','Heather outcrop','Cave arch','Cave jamb','Diamond socket stone')):
        paint(ob,lambda p,n:blend(rgb('85929D'),rgb('C8C5B8'),clamp(.53+noise.noise_vector(p*.25)[0]*.95)))
sky=bpy.data.objects['Sky dome']
# Ease out of the warm horizon earlier; the old slow gradient made most of the
# normal camera view grey-cream even under a blue zenith.
paint(sky,lambda p,n:blend(rgb('ECE8D7'),rgb('68AFE0'),clamp(p.z/72)**.60))
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('CLEAR_DAYLIGHT_COLOUR_READY')
