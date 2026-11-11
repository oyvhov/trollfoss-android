"""Restore the smile rim's rounded cross-section after the old planar face adjustment.
Flattening every lip vertex onto the mouth surface made overlapping triangles.
This replacement is idempotent and leaves the original sculpt and teeth intact.
"""
import bpy,ast,pathlib,math
from math import sin,cos,pi
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
current=bpy.data.collections['Rumle • sculpt']
for ob in list(current.objects):
    if ob.name.startswith('Sculpted smile rim'):bpy.data.objects.remove(ob,do_unlink=True)
outline=[]
for i in range(65):
    t=2*pi*i/64;x=.77*cos(t);z=3.91+.37*sin(t)+.18*abs(cos(t))
    y=-1.235-.16*(1-min(1,abs(x)/.85)**2)
    outline.append((x,y,z))
curve('Sculpted smile rim',outline,.048,bpy.data.materials['Lids and lips'],10)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('RUMLE_SMILE_VOLUME_RESTORED')
