"""Curved meadow tufts, grouped around the paths instead of scattered shards.

Both mobile profiles share the readable main tufts. The full profile adds a
small second batch. Colours, blade bends and placement are baked in Blender.
"""
import ast, bpy, json, math, pathlib, random
from math import sin, cos, pi, sqrt, exp
from mathutils import Vector, noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
current=bpy.data.collections['Heileberget • landscape']
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene

def H(x,y):
    z=-y;ridge=max(0,min(1,(3-z)/24))*6
    lake=((x+3)/10)**2+((z-1)/8)**2
    return 1.65+ridge+.22*sin(x*.44)*cos(z*.3)-max(0,1-lake)*6.2+exp(-((x+3)**2+(z-1)**2)*.11)*6.2

# Retain the authored plant positions so repeated runs do not accumulate art.
if 'meadow_anchors' not in current:
    anchors=set()
    for ob in current.objects:
        if ob.name.startswith('Heather / grass'):
            p=min((ob.matrix_world@v.co for v in ob.data.vertices),key=lambda p:p.z)
            anchors.add((round(p.x,3),round(p.y,3)))
    assert len(anchors)>100,len(anchors)
    current['meadow_anchors']=json.dumps(sorted(anchors))
anchors=json.loads(current['meadow_anchors'])
for ob in list(current.objects):
    if ob.name.startswith(('Heather / grass','Fine heather grasses','Meadow •')):
        bpy.data.objects.remove(ob,do_unlink=True)

# Respect the actual authored routes, including the lookout branch.
path_points=[]
for ob in current.objects:
    if ob.name.startswith(('Winding ochre trail','Lookout • winding ledge path')):
        source=bpy.data.meshes.get('Path original • '+ob.name)
        if source:
            for i in range(0,len(source.vertices),2):
                a=source.vertices[i].co;b=source.vertices[i+1].co;p=(a+b)*.5
                path_points.append((p.x,p.y,(a-b).length*.40))

def clear(x,y):
    if H(x,y)<1.3 or abs(y+1)<1.2 or (x>8 and y>11):return False
    if any((x-a)**2+(y-b)**2<(w+.13)**2 for a,b,w in path_points):return False
    return True

def blend(a,b,t):return tuple(v*(1-t)+w*t for v,w in zip(a,b))

def make_tufts(name,points,seed,secondary=False):
    rng=random.Random(seed);verts=[];faces=[];cols=[]
    for x,y in points:
        if not clear(x,y):continue
        base=H(x,y)+.012;rotation=rng.random()*2*pi
        count=3 if secondary else 4
        for blade in range(count):
            a=rotation+blade*2.399;h=rng.uniform(.13,.25) if secondary else rng.uniform(.22,.38)
            width=rng.uniform(.018,.030) if secondary else rng.uniform(.028,.045)
            bend=h*rng.uniform(.35,.75)
            root=Vector((x+cos(a)*.035,y+sin(a)*.035,base))
            side=Vector((-sin(a),cos(a),0));out=Vector((cos(a),sin(a),0))
            tone=rng.random();bottom=blend(rgb('62824D'),rgb('819450'),tone)
            top=blend(rgb('9CB46E'),rgb('B7C482'),tone)
            idx=len(verts)
            # Two gently curved ribbon panels taper into a small bent tip.
            for t,w in [(0,width*.65),(.38,width),(.76,width*.56)]:
                p=root+out*(bend*t*t)+Vector((0,0,h*(t-.14*t*t)))
                verts.extend([p-side*w,p+side*w])
                cols.extend([blend(bottom,top,t)]*2)
            verts.append(root+out*bend+Vector((0,0,h*.84)));cols.append(top)
            faces.extend([(idx,idx+1,idx+3,idx+2),(idx+2,idx+3,idx+5,idx+4),(idx+4,idx+5,idx+6)])
    ob=mesh(name,verts,faces,bpy.data.materials['Painted natural albedo'],True)
    attr=ob.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for entry,color in zip(attr.data,cols):entry.color=(*[linear(c) for c in color],1)
    return len(faces)//3

main=make_tufts('Meadow • curved grass tufts',anchors,605)
rng=random.Random(606);small=[]
# Local groups leave quiet patches between them. No runtime wind or particles.
for i in range(85):
    x=rng.uniform(-24,24);y=rng.uniform(-23,21)
    for j in range(5):
        a=rng.random()*2*pi;r=rng.uniform(.12,.65)
        small.append((x+cos(a)*r,y+sin(a)*r))
fine=make_tufts('Fine heather grasses • curved meadow',small,607,True)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('CURVED_MEADOW_READY',main,fine)
