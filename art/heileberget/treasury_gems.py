"""Faceted crystal shafts and cut loose diamonds replace the old long cone forms.
Retain their locations and heights; navigation and interaction points stay fixed.
"""
import bpy, ast, pathlib, math
from math import sin, cos, pi, exp, sqrt
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
current=bpy.data.collections['Inside the mountain'];M=bpy.data.materials
material=mat('Treasury • luminous mineral','91D5E5',.24,.10,1.1)
for index,ob in enumerate(sorted(current.objects,key=lambda ob:ob.name)):
    if not ob.name.startswith(('Crystal column','Treasury crystal','Loose diamond')):continue
    source=bpy.data.meshes.get('Gem original • '+ob.name)
    if source is None:
        source=ob.data.copy();source.name='Gem original • '+ob.name;source.use_fake_user=True
    xs=[v.co.x for v in source.vertices];ys=[v.co.y for v in source.vertices];zs=[v.co.z for v in source.vertices]
    x=(min(xs)+max(xs))*.5;y=(min(ys)+max(ys))*.5;z0=min(zs);height=max(zs)-z0
    radius=max(max(xs)-min(xs),max(ys)-min(ys))*.5
    loose=ob.name.startswith('Loose diamond')
    if loose:
        rings=[(0,.015),(.56,radius),(.77,radius*.94),(1,radius*.52)];sides=8
    else:
        rings=[(0,radius*.88),(.73,radius),(1,.015)];sides=6
    vertices=[];faces=[]
    for h,r in rings:
        for j in range(sides):
            a=j*2*pi/sides+.18+index*.37
            vertices.append((x+cos(a)*r+(0 if loose else .06*h),y+sin(a)*r,z0+h*height))
    for row in range(len(rings)-1):
        for j in range(sides):
            k=row*sides+j;n=row*sides+(j+1)%sides
            faces.append((k,n,n+sides,k+sides))
    faces.extend([tuple(reversed(range(sides))),tuple(range((len(rings)-1)*sides,len(rings)*sides))])
    data=bpy.data.meshes.new(ob.name+' • polished facets');data.from_pydata(vertices,[],faces);data.update()
    data.materials.append(material)
    attr=data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    palette=['80BACB','B3E5EC','9DB9D3','BCD4E1']
    base=rgb(palette[index%len(palette)])
    for vertex in data.vertices:
        t=(vertex.co.z-z0)/max(.01,height)
        side=vertex.index%sides
        light=.72+.23*t+(0.15 if side%3==0 else -.04 if side%3==1 else .03)
        color=[min(1,c*light) for c in base]
        attr.data[vertex.index].color=(*[linear(c) for c in color],1)
    ob.data=data
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('TREASURY_GEMS_READY')
