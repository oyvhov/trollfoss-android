"""Original blue butterfly with rounded scalloped wings and separate flight pivots."""
import ast,bpy,pathlib,math
from math import sin,cos,pi
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
ink=bpy.data.materials.get('Butterfly • velvet body') or mat('Butterfly • velvet body','455367')
cream=bpy.data.materials.get('Butterfly • ivory markings') or mat('Butterfly • ivory markings','FAE7B8')
groups={}
for part in ['body','wing']:
    col=bpy.data.collections.get('Butterfly • '+part)
    if col:
        for ob in list(col.objects):bpy.data.objects.remove(ob,do_unlink=True)
    else:col=bpy.data.collections.new('Butterfly • '+part)
    col.use_fake_user=True;groups[part]=col
current=groups['body']
ell('Butterfly • thorax',(0,0,.025),(.028,.065,.027),ink,12,8)
ell('Butterfly • abdomen',(0,.075,.017),(.020,.069,.022),ink,12,8)
ell('Butterfly • head',(0,-.070,.030),(.026,.026,.028),ink,12,8)
for side in [-1,1]:
    curve('Butterfly • antenna',[(side*.014,-.079,.047),(side*.030,-.112,.077),(side*.049,-.129,.079)],.0035,ink,5)
    ell('Butterfly • antenna tip',(side*.049,-.129,.079),(.007,.009,.007),cream,8,6)
current=groups['wing']
outline=[(.035,-.06),(.12,-.24),(.26,-.30),(.36,-.21),(.35,-.10),(.24,-.015),(.29,.075),(.26,.19),(.16,.22),(.07,.16),(.025,.045)]
outline=[Vector((x,y,0)) for x,y in outline];smooth=[];n=len(outline)
for i in range(n):
    a,b,c,d=[outline[j%n] for j in [i-1,i,i+1,i+2]]
    for j in range(4):
        t=j/4;smooth.append(.5*((2*b)+(-a+c)*t+(2*a-5*b+4*c-d)*t*t+(-a+3*b-3*c+d)*t*t*t))
centre=Vector((.105,.0,.028));verts=[centre];cols=[rgb('B5DBE8')];faces=[]
for r,color in [(.58,'6CB8DA'),(.83,'80BFDB'),(.93,'4D7996'),(1,'40546B')]:
    for p in smooth:
        v=centre.lerp(p,r);v.z=.026*(1-r*r)
        verts.append(v);cols.append(rgb(color))
n=len(smooth)
for i in range(n):faces.append((0,1+i,1+(i+1)%n))
for ring in range(3):
    for i in range(n):
        a=1+ring*n+i;b=1+ring*n+(i+1)%n
        faces.append((a,a+n,b+n,b))
ob=mesh('Butterfly • rounded blue wing',verts,faces,bpy.data.materials['Painted natural albedo'],True)
attr=ob.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
for entry,color in zip(attr.data,cols):entry.color=(*[linear(c) for c in color],1)
for x,y,r in [(.27,-.21,.018),(.30,-.15,.015),(.235,.12,.016),(.19,.165,.014),(.125,.17,.011)]:
    ell('Butterfly • ivory wing spot',(x,y,.015),(r,r*.85,.003),cream,10,6)
# Collection instances are the editable Blender preview, never baked into scenery.
stage=bpy.data.collections['Heileberget • landscape']
for ob in list(stage.objects):
    if ob.name.startswith('Butterfly preview •'):bpy.data.objects.remove(ob,do_unlink=True)
for part,col in groups.items():
    for side in ([-1,1] if part=='wing' else [0]):
        ob=bpy.data.objects.new('Butterfly preview • '+part+str(side),None);stage.objects.link(ob)
        ob.instance_type='COLLECTION';ob.instance_collection=col;ob.location=(-3,-17.5,2.65)
        if side:ob.scale.x=side;ob.rotation_euler[1]=side*.5
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('BLUE_BUTTERFLY_MODEL_READY')
