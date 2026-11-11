"""Join the scenery to the exact playable boundary, without an exposed terrain step.

Idempotent. Every half-metre vertex on the +/-26 m boundary is shared in position
with the walkable landscape. Outer rings gradually descend towards the fjord.
"""
import bpy, ast, pathlib, math
from math import sin, cos, pi, exp
from mathutils import Vector, noise

ROOT = pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree = ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n, ast.FunctionDef)], type_ignores=[]), 'helpers', 'exec'))
scene = bpy.data.scenes['Heileberget']; bpy.context.window.scene = scene
current = bpy.data.collections['Heileberget • landscape']
for o in list(current.objects):
    if o.name == 'Valley beyond the paths': bpy.data.objects.remove(o, do_unlink=True)

def clamp(v): return max(0, min(1, v))
def blend(a, b, t): return tuple(x*(1-t)+y*t for x,y in zip(a,b))

verts=[]; faces=[]; per_side=104; count=4*per_side
for radius in [25.5, 26, 27, 29, 32, 36, 42, 50, 60, 70]:
    for side in range(4):
        for i in range(per_side):
            a=-radius+2*radius*i/per_side
            if radius==25.5:
                # Preserve half-metre samples at the ridge's y=21 slope break.
                # Uniformly shrinking the inner ring missed that line by 5 cm.
                a=max(-25.5,min(25.5,-26+i*.5))
            x,y=[(a,-radius),(radius,a),(-a,radius),(-radius,-a)][side]
            edge=max(abs(x)-26,abs(y)-26,0)
            z=H(max(-26,min(26,x)),max(-26,min(26,y)))
            z-=edge*.27
            z+=sin(x*.12)*sin(y*.09)*min(edge*.04,1.2)
            verts.append((x,y,z-.008))
for j in range(9):
    for i in range(count):
        a=j*count+i; b=j*count+(i+1)%count
        face=[];seen=set()
        for k in (a,a+count,b+count,b):
            key=tuple(round(v,6) for v in verts[k])
            if key not in seen:face.append(k);seen.add(key)
        if len(face)>2:faces.append(tuple(face))
o=mesh('Valley beyond the paths',verts,faces,None,True)
attr=o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
for v in o.data.vertices:
    p=v.co
    t=clamp(.5+noise.noise_vector(Vector((p.x*.13,p.y*.13,4)))[0]*1.4)
    green=blend(rgb('7A9A70'),rgb('B4BE85'),t)
    rock=clamp((abs(noise.noise_vector(Vector((p.x*.16,p.y*.16,1)))[0])-.30)*4.5)
    c=blend(green,rgb('B7B9B9'),rock)
    attr.data[v.index].color=(*[linear(c) for c in c],1)
o.data.materials.append(bpy.data.materials['Painted natural albedo'])
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('CONTINUOUS_TERRAIN_SKIRT',len(verts),len(faces))
