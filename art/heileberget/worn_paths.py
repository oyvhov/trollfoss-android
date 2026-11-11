"""Worn earth footpaths with grass-softened edges, retaining the authored routes.

Vertex colour and small stones only. No runtime textures or extra mobile shader.
The original ribbon is retained unlinked, so repeating this pass is deterministic.
"""
import bpy, ast, pathlib, math, random
from math import sin, cos, pi, exp, sqrt
from mathutils import Vector, noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
current=bpy.data.collections['Heileberget • landscape'];M=bpy.data.materials
for ob in list(current.objects):
    if ob.name.startswith('Path • small rounded stone'):bpy.data.objects.remove(ob,do_unlink=True)
def clamp(x):return max(0,min(1,x))
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def H(x,y):
    z=-y;ridge=max(0,min(1,(3-z)/24))*6
    lake=((x+3)/10)**2+((z-1)/8)**2
    return 1.65+ridge+.22*sin(x*.44)*cos(z*.3)-max(0,1-lake)*6.2+exp(-((x+3)**2+(z-1)**2)*.11)*6.2
def ground(p):
    t=clamp(.5+noise.noise_vector(Vector((p.x*.13,p.y*.13,4)))[0]*1.4)
    green=blend(rgb('619460'),rgb('B5C779'),t)
    stone=clamp((abs(noise.noise_vector(Vector((p.x*.16,p.y*.16,1)))[0])-.30)*4.5)
    return blend(green,rgb('ADB3B3'),stone*.78)
paths=[o for o in current.objects if o.name.startswith(('Winding ochre trail','Lookout • winding ledge path'))]
for index,ob in enumerate(paths):
    source=bpy.data.meshes.get('Path original • '+ob.name)
    if source is None:
        source=ob.data.copy();source.name='Path original • '+ob.name;source.use_fake_user=True
    verts=[];faces=[];colors=[];rows=len(source.vertices)//2;columns=9
    centres=[(source.vertices[row*2].co+source.vertices[row*2+1].co)*.5 for row in range(rows)]
    distances=[0.0]
    for a,b in zip(centres,centres[1:]):distances.append(distances[-1]+(b-a).length)
    for row in range(rows):
        left=source.vertices[row*2].co;right=source.vertices[row*2+1].co
        centre=(left+right)*.5
        end_distance=min(distances[row],distances[-1]-distances[row])
        end_t=clamp(end_distance/1.6)
        end_fade=1-end_t*end_t*(3-2*end_t)
        width_t=clamp(end_distance/.9)
        end_width=.12+.88*width_t*width_t*(3-2*width_t)
        for col in range(columns):
            t=col/(columns-1);p=left.lerp(right,t)
            edge=abs(2*t-1)
            # A few centimetres of irregularity along the earth edge keeps the
            # deliberately readable route from looking like a painted road.
            p=centre+(p-centre)*(1+.075*math.sin(row*1.7+index))*end_width
            p.z=H(p.x,p.y)+.038
            verts.append(tuple(p))
            fleck=clamp(.5+noise.noise_vector(Vector((p.x*3.5,p.y*3.5,9)))[0]*.85)
            earth=blend(rgb('AA9979'),rgb('D1BE95'),fleck)
            fade=clamp((edge-.58)/.42)**1.4
            colors.append(blend(earth,ground(p),max(fade*.90,end_fade)))
    for row in range(rows-1):
        for col in range(columns-1):
            k=row*columns+col;faces.append((k,k+1,k+1+columns,k+columns))
    data=bpy.data.meshes.new(ob.name+' • worn surface');data.from_pydata(verts,[],faces);data.update()
    if sum(poly.normal.z for poly in data.polygons)<0:data.flip_normals()
    for poly in data.polygons:poly.use_smooth=True
    attr=data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for entry,color in zip(attr.data,colors):entry.color=(*[linear(c) for c in color],1)
    data.materials.append(M['Painted natural albedo']);ob.data=data
    rng=random.Random(8810+index)
    for row in range(3,rows-2,4):
        a=source.vertices[row*2].co;b=source.vertices[row*2+1].co
        p=a.lerp(b,rng.uniform(.2,.8));size=rng.uniform(.025,.065)
        ell('Path • small rounded stone',(p.x,p.y,H(p.x,p.y)+.046),(size,size*.72,.018),M['Granite %02d'%(row%4)],8,4)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('WORN_PATHS_READY',len(paths))
