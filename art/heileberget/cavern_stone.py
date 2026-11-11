"""Mineral variation and gently hewn pillars for the great cavern. Idempotent.

Keeps the wall envelope and door openings intact, and stays within collision radii.
"""
import bpy, ast, pathlib, math
from math import sin, cos, pi, exp
from mathutils import Vector, noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
current=bpy.data.collections['Inside the mountain'];M=bpy.data.materials
for ob in list(current.objects):
    if ob.name.startswith(('Hall pillar','Side arch foundation')):
        bpy.data.objects.remove(ob,do_unlink=True)

def pillar(name,x,base,height,wide):
    verts=[];faces=[];sides=16;rows=14
    for j in range(rows+1):
        t=j/rows
        profile=.82+.13*exp(-((t-.04)/.18)**2)+.12*exp(-((t-.96)/.22)**2)
        for i in range(sides):
            a=i*2*pi/sides
            r=wide*(profile+.025*sin(a*5+t*5)+.016*cos(a*9-t*3))
            verts.append((x+r*cos(a)+.06*sin(t*pi*2),15+r*sin(a),base+t*height))
    for j in range(rows):
        for i in range(sides):
            a=j*sides+i;b=j*sides+(i+1)%sides;faces.append((a,b,b+sides,a+sides))
    faces.extend([tuple(reversed(range(sides))),tuple(range(rows*sides,(rows+1)*sides))])
    return mesh(name,verts,faces,None,True)
for x in [-8,6]:pillar('Hall pillar',x,4.4 if x>3 else 0,9,1.35)
for x in [-19,-9,7,19]:pillar('Side arch foundation',x,4.4 if x>3 else 0,5.2,.68)

def clamp(v):return max(0,min(1,v))
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
for ob in current.objects:
    if ob.type!='MESH' or not ob.name.startswith(('Cavern • sculpted','Hall pillar','Side arch foundation','Vaulted connecting arch')):continue
    attr=ob.data.color_attributes.get('Albedo') or ob.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in ob.data.vertices:
        p=ob.matrix_basis@v.co
        broad=noise.noise_vector(Vector((p.x*.23,p.y*.29,p.z*.38)))[0]
        mineral=noise.noise_vector(Vector((p.x*.57,p.y*.47,p.z*.12)))[1]
        c=blend(rgb('7A8296'),rgb('C4BEB9'),clamp(.5+broad*.7+mineral*.24))
        # Warm limestone veins and cool recesses share the landscape's grey-lilac palette.
        vein=clamp((noise.noise_vector(p*.7)[2]-.22)*1.3)
        c=blend(c,rgb('CEBDA3'),vein*.35)
        foot=exp(-max(0,p.z-(4.4 if p.x>3 else 0))*.85)*.19
        c=blend(c,rgb('7A8A71'),foot)
        attr.data[v.index].color=(*[linear(q) for q in c],1)
    ob.data.materials.clear();ob.data.materials.append(M['Painted natural albedo'])
    for p in ob.data.polygons:p.material_index=0
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('CAVERN_HEWN_PILLARS_AND_MINERAL_COLOUR_READY')
