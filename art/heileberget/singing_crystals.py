"""Three original playable crystal clusters, with a quiet carved stone garden.
Collection instances preview the same separate meshes animated on Android.
"""
import bpy,ast,pathlib,math
from math import sin,cos,pi,exp
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
CAVE=bpy.data.collections['Inside the mountain'];M=bpy.data.materials
for ob in list(CAVE.objects):
    if ob.name.startswith('Singing •'):bpy.data.objects.remove(ob,do_unlink=True)
current=CAVE
positions=[(-17.4,16.8),(-15.7,17.8),(-13.9,17.2)]
for i,(x,y) in enumerate(positions):
    ell('Singing • smooth stone plinth',(x,y,.10),(.76,.66,.17),M['Granite 02'],24,10)
    for j in range(3):
        a=j*2*pi/3
        ell('Singing • moss cushion',(x+cos(a)*.55,y+sin(a)*.5,.16),(.22,.18,.11),M['Moss 02'],10,6)
    # Inlaid dots suggest the notes visually without requiring written text.
    for j in range(i+1):
        ell('Singing • note pebble',(x+(j-i*.5)*.18,y-.64,.07),(.055,.07,.035),M['Vanilla cream'],10,6)

for index,(name,h,height) in enumerate([('blue','78CCDD',2.15),('lilac','B2A0DB',2.8),('honey','E8C27D',2.4)]):
    group='Singing crystal • '+name;old=bpy.data.collections.get(group)
    if old:
        for ob in list(old.objects):bpy.data.objects.remove(ob,do_unlink=True)
        bpy.data.collections.remove(old)
    current=bpy.data.collections.new(group);scene.collection.children.link(current)
    base=rgb(h)
    material=M.get(group) or M.new(group);material.use_nodes=True
    material['game_color']=list(base);material.diffuse_color=(*[linear(v) for v in base],1)
    p=next(n for n in material.node_tree.nodes if n.type=='BSDF_PRINCIPLED')
    p.inputs['Base Color'].default_value=material.diffuse_color;p.inputs['Roughness'].default_value=.27
    for k,(x,y,s,tilt) in enumerate([(0,0,1,.05),(-.36,.04,.48,-.2),(.35,.10,.61,.18)]):
        verts=[];faces=[];r=.27*s;z0=.18;hh=height*s
        for z,rr in [(z0,r*.84),(z0+hh*.74,r),(z0+hh,.015)]:
            for j in range(6):
                a=j*pi/3+.2
                verts.append((x+cos(a)*rr+tilt*(z-z0),y+sin(a)*rr,z))
        for row in range(2):
            for j in range(6):
                a=row*6+j;b=row*6+(j+1)%6;faces.append((a,b,b+6,a+6))
        faces.extend([tuple(reversed(range(6))),tuple(range(12,18))])
        o=mesh('Singing • '+name+' prism',verts,faces,material,False)
        bevel=o.modifiers.new('Polished crystal edges','BEVEL');bevel.width=.018*s;bevel.segments=1;apply(o,bevel)
        attr=o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
        for v in o.data.vertices:
            t=max(0,min(1,(v.co.z-z0)/hh));shine=max(0,v.normal.dot(Vector((-.5,-.7,.5)).normalized()))**6*.15
            c=[min(1,b*(.67+.29*t)+shine) for b in base]
            attr.data[v.index].color=(*[linear(v) for v in c],1)
    preview=bpy.data.objects.new('Singing • '+name+' preview',None);CAVE.objects.link(preview)
    preview.instance_type='COLLECTION';preview.instance_collection=current;preview.location=(*positions[index],0)
    scene.collection.children.unlink(current)

# Absolute placement also makes rerunning after an interrupted authoring pass safe.
cairn=sorted((ob for ob in bpy.data.collections['Heileberget • landscape'].objects if ob.name.startswith('Lookout • cairn')),key=lambda ob:ob.name)
for j,ob in enumerate(cairn):ob.location=(-21.5,20.3,H(-21.5,20.3)+j*.22)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('SINGING_CRYSTALS_READY')
