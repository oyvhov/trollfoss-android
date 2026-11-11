import bpy,math,ast,pathlib
from mathutils import Vector,noise
from math import sin,cos,pi,exp,sqrt
source=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale/art/heileberget/build_scene.py').read_text(encoding='utf-8')
exec(compile(ast.Module(body=[n for n in ast.parse(source).body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.context.scene;CAVE=bpy.data.collections['Inside the mountain'];current=CAVE
M=bpy.data.materials;stone=M['Cavern warm limestone'];wood=M['Weathered timber'];cream=M['Vanilla cream'];gold=M['Old gold'];vertexmat=M['Painted natural albedo']
# Close the small roof/floor seams while leaving the full entrance open.
wall=bpy.data.objects['Cavern • sculpted continuous walls']
for v in wall.data.vertices:v.co.z*=1.23
roof=bpy.data.objects['Cavern • high vaulted roof']
for v in roof.data.vertices:v.co.x*=1.08;v.co.y=7+(v.co.y-7)*1.11
floor=bpy.data.objects['Cavern • connected floor']
for v in floor.data.vertices:
    if v.co.x==-20:v.co.x=-22
    if v.co.x==20:v.co.x=22
    if v.co.y==-14:v.co.y=-16
    if v.co.y==28:v.co.y=30
for x in [-19,-9,7,19]:
    z=max(0,min(4.4,23*.23)) if x>3 else 0
    tube('Side arch foundation',[(x,15,z),(x,15,z+5.2)],[.65,.65],stone,12)
# A bevelled stone edge carries the upper gallery, instead of an unsupported floor.
cube('Balcony stone fascia',(3.22,14,2.1),(.5,28,4.2),stone,.1)
# Lamps are held by metal frames. Add pegs and knots to the main furniture.
for o in list(CAVE.objects):
    if o.name.startswith(('Great larder shelf','Larder • wooden shelf','Cake table')):
        for side in [-1,1]:
            p=o.location.copy();p.x+=side*o.dimensions.x*.42;p.y-=o.dimensions.y*.5+.01
            ell('Wooden joinery peg',p,(.045,.025,.045),M['Birch scars'],8,6)
# Paint soft variations into broad surfaces; the same colours go into Android.
def srgb(v):return v*12.92 if v<.0031308 else 1.055*v**(1/2.4)-.055
for o in CAVE.objects:
    if o.type!='MESH' or not o.name.startswith(('Cavern •','Hall pillar','Vaulted connecting arch','Side arch foundation','Balcony stone fascia')):continue
    base=tuple(o.data.materials[0].get('game_color',[.55,.51,.46]))
    attr=o.data.color_attributes.get('Albedo') or o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in o.data.vertices:
        p=o.matrix_world@v.co
        texture=1+noise.noise_vector(p*.8)[0]*.15+noise.noise_vector(p*3)[0]*.04
        attr.data[v.index].color=(*[linear(min(1,c*texture)) for c in base],1)
    o.data.materials.clear();o.data.materials.append(vertexmat)
# Keep all intermediate art passes as source; this is the editable deliverable.
scene['art_version']='Blender sculpt 2 • spacious cave and natural highlands'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('Cave seams closed, materials painted')
