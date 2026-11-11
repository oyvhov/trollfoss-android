"""Round 8: rounded birch sprays, an open casting shore, and tactile flat pebbles.
Original meshes only. Rerunnable after the landscape passes; heights match Highlands.
"""
import bpy,ast,pathlib,random,math
from math import sin,cos,pi,exp,sqrt
from mathutils import Vector,noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
OUTSIDE=bpy.data.collections['Heileberget • landscape'];current=OUTSIDE;M=bpy.data.materials
for o in list(OUTSIDE.objects):
    if o.name.startswith(('Birch •','Shore •')):bpy.data.objects.remove(o,do_unlink=True)
vertexmat=M['Painted natural albedo'];bark=M['Birch bark'];dark=M['Birch scars']

def height(x,y):
    z=-y;ridge=max(0,min(1,(3-z)/24))*6
    lake=((x+3)/10)**2+((z-1)/8)**2
    return 1.65+ridge+.22*sin(x*.44)*cos(z*.3)-max(0,1-lake)*6.2+exp(-((x+3)**2+(z-1)**2)*.11)*6.2
def mix(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def paint(o,colors):
    attr=o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for entry,c in zip(attr.data,colors):entry.color=(*[linear(v) for v in c],1)
    o.data.materials.append(vertexmat)

# Each tree has six airy, uneven sprays around visible slender branches. Small
# rounded leaves break the silhouette; no long downward diamond-shaped cards.
# Keep the bridge corridor clear: the three original island trunks intersected
# the actual crossing and their crowns hid the children during the journey.
places=[(-17,-14,1.1),(-20,-10,.9),(-22,-5,.8),(-5,11,.75),(-2,12,.65),(0,10,.85),
 (14,-3,.9),(18,-7,1.25),(21,-4,1),(19,3,1.1),(8,8,.8),(7,13,.85),(-15,11,.85),(-20,16,1),(-22,7,1.2),(-18,1,.9),(22,17,.8)]
for index,(x,y,s) in enumerate(places):
    rng=random.Random(830+index);base=height(x,y);lean=rng.uniform(-.3,.3)
    tube('Birch • pale trunk',[(x,y,base),(x+.08,y,base+1.5*s),(x+lean,y+.08,base+3.9*s)],[.15*s,.10*s,.025*s],bark,8)
    for j in range(8):
        z=base+(.26+j*.42)*s
        curve('Birch • dark lenticel',[(x-.11*s,y-.095*s,z),(x+.035*s,y-.115*s,z+.025),(x+.085*s,y-.07*s,z)],.018*s,dark,4)
    vs=[];fs=[];cs=[]
    for j in range(6):
        a=j*2.4+index*.35
        root=Vector((x+lean*.5,y,base+(1.8+j*.25)*s))
        tip=root+Vector((cos(a)*1.0*s,sin(a)*1.0*s,(.64+(j%2)*.2)*s))
        tube('Birch • branch',[root,root.lerp(tip,.6)+Vector((0,0,.13*s)),tip],[.048*s,.025*s,.009*s],bark,6)
        centre=tip+Vector((0,0,.05*s));rx=(.61+rng.random()*.15)*s;ry=(.55+rng.random()*.12)*s;rz=(.41+rng.random()*.12)*s
        # A low-poly, irregular core gives the crown a readable volume in the
        # distance without multiplying hundreds of alpha cards and overdraw.
        start=len(vs);sides=10;rings=6
        for v in range(rings+1):
            t=v*pi/rings
            for u in range(sides):
                q=u*2*pi/sides
                wobble=1+.10*sin(q*3+t*4+j)+.07*cos(q*5-t*3)
                p=centre+Vector((sin(t)*cos(q)*rx*wobble,sin(t)*sin(q)*ry*wobble,cos(t)*rz))
                vs.append(p)
                shade=max(0,min(1,.4+.22*cos(t)+rng.random()*.14))
                cs.append(mix(rgb('4C795B'),rgb('B2C88B'),shade))
        for v in range(rings):
            for u in range(sides):
                k=start+v*sides+u;n=start+v*sides+(u+1)%sides
                fs.append((k,k+sides,n+sides,n))
        for k in range(24):
            q=rng.uniform(0,2*pi);t=rng.uniform(.25,2.8)
            wobble=1+.10*sin(q*3+t*4+j)+.07*cos(q*5-t*3)
            normal=Vector((sin(t)*cos(q)/rx,sin(t)*sin(q)/ry,cos(t)/rz)).normalized()
            p=centre+Vector((sin(t)*cos(q)*rx*wobble,sin(t)*sin(q)*ry*wobble,cos(t)*rz))+normal*.055*s
            # Lay each rounded blade along the crown, outside its surface.
            # Radial cards cut through the old core and made jagged dark slivers.
            direction=normal.cross(Vector((0,0,1))).normalized()
            across=normal.cross(direction).normalized()
            length=rng.uniform(.12,.19)*s;width=length*.57
            start=len(vs)
            vs.append(p+normal*.025*s)
            for u in range(8):
                q2=u*2*pi/8
                vs.append(p+direction*(cos(q2)*length)+across*(sin(q2)*width))
            color=mix(rgb('6A9365'),rgb('C4D59B'),rng.random())
            cs.extend([color]*9)
            for u in range(8):fs.append((start,start+1+u,start+1+(u+1)%8))
    o=mesh('Birch • soft leaf sprays %02d'%index,vs,fs,None,True);paint(o,cs)

# The stack is on dry ground beside the activity, low enough to step past.
stones=[(5.55,-7.45,.12,.39,.29),(5.53,-7.44,.28,.32,.25),(5.58,-7.43,.42,.25,.20),
        (4.95,-7.55,.08,.27,.21),(5.14,-8.0,.08,.30,.24),(6.0,-7.88,.09,.31,.23)]
for i,(x,y,h,rx,ry) in enumerate(stones):
    o=ell('Shore • smooth skipping pebble',(x,y,height(x,y)+h),(rx,ry,.09),M['Granite %02d'%(i%4)],16,8)
    o.rotation_euler[2]=i*.73
    curve('Shore • pale quartz seam',[(x-rx*.65,y,height(x,y)+h+.065),(x,y-.025,height(x,y)+h+.093),(x+rx*.65,y,height(x,y)+h+.065)],.008,M['Vanilla cream'],4)

scene['art_version']='Round 8 • open shore, rounded birch sprays and natural granite planes'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('BIRCHES_AND_SHORE_READY',len(places),'trees; smutt view opened')
