"""Heileberget art source. Run in the dedicated Blender 5.2 MCP session.
Original meshes/materials, no external assets. Coordinates: Blender Z up, front -Y.
The Android exporter converts (x,y,z) to (x,z,-y).
"""
import bpy, math, random, os, json
from mathutils import Vector, noise
from math import sin, cos, pi, exp, sqrt

OUT='C:/topa/dist/heileberget-blender'
os.makedirs(OUT,exist_ok=True)
random.seed(247)
# Only this separately launched factory scene is owned by the authoring task.
assert bpy.context.scene.name in ('Scene','Heileberget'), 'Unexpected scene; refusing to replace it'
bpy.ops.object.select_all(action='SELECT'); bpy.ops.object.delete(use_global=False)
for c in list(bpy.data.collections):
    if c.name != 'Collection' and c.users==0: bpy.data.collections.remove(c)
scene=bpy.context.scene; scene.name='Heileberget'
def collection(name):
    old=bpy.data.collections.get(name)
    if old: bpy.data.collections.remove(old)
    c=bpy.data.collections.new(name); scene.collection.children.link(c); return c
TROLL=collection('Rumle • sculpt'); ARM=collection('Rumle • waving hand')
OUTSIDE=collection('Heileberget • landscape'); CAVE=collection('Inside the mountain')
STUDIO=collection('Cameras & light'); current=TROLL
def link(obj):
    for c in list(obj.users_collection): c.objects.unlink(obj)
    current.objects.link(obj); return obj
def rgb(h):
    return tuple(int(h[i:i+2],16)/255 for i in (0,2,4))
def linear(v): return v/12.92 if v<.04045 else ((v+.055)/1.055)**2.4
def mat(name,h,rough=.8,metal=0,emit=0):
    m=bpy.data.materials.new(name); c=rgb(h)
    m.diffuse_color=(*[linear(v) for v in c],1)
    m['game_color']=list(c); m['emission']=emit
    p=next(n for n in m.node_tree.nodes if n.type=='BSDF_PRINCIPLED')
    p.inputs['Base Color'].default_value=m.diffuse_color
    p.inputs['Roughness'].default_value=rough; p.inputs['Metallic'].default_value=metal
    if emit:
        p.inputs['Emission Color'].default_value=m.diffuse_color
        p.inputs['Emission Strength'].default_value=emit
    return m
skin=mat('Warm granite skin','A5A18E'); skinLight=mat('Lids and lips','B6AC95')
rose=mat('Nose • soft rose','CC9684'); inner=mat('Ear warmth','887B70')
dark=mat('Smile interior','342E38'); nostril=mat('Nose dimples','735E53')
ivory=mat('Teeth • ivory','F2DEB3'); eye=mat('Eye whites','F9EACB',.35)
iris=mat('Hazel eyes','69492D',.35); black=mat('Pupils','171E26',.25)
moss=[mat('Moss %02d'%i,h) for i,h in enumerate(['435638','637C3C','819340','A1A44D','B3B65B'])]
granite=[mat('Granite %02d'%i,h) for i,h in enumerate(['858C91','9DA3A3','B0B0A7','C0B8A8','777F83'])]
grass=[mat('Heather ground %02d'%i,h) for i,h in enumerate(['667643','7E8549','969450','596B43'])]
pathmat=mat('Ochre footpath','BBAB82'); bark=mat('Birch bark','E5DEBF'); barkDark=mat('Birch scars','4B514A')
wood=mat('Weathered timber','937355'); woodLight=mat('Cut timber','BDA078'); iron=mat('Forged iron','414E50',.5,.3)
gold=mat('Old gold','EDB64B',.28,.65); gem=mat('Icy diamonds','74DDE9',.22,.15,.12)
cream=mat('Vanilla cream','F7DDAA'); berry=mat('Berries','C54D63'); cake=mat('Chocolate sponge','725040')
sodaBlack=mat('Pepsi bottle','222E36',.25); sodaGreen=mat('Urge bottle','5A9046',.28)
labelBlue=mat('Pepsi label blue','315784'); labelRed=mat('Pepsi label red','DD5353'); labelGreen=mat('Urge yellow label','CFD858')
watermat=mat('Tarn water','33798C',.16,.2)
lantern=mat('Lantern glass','FFD279',.3,0,3)

def mesh(name,verts,faces,material,smooth=False):
    d=bpy.data.meshes.new(name);d.from_pydata(verts,[],faces);d.update()
    o=bpy.data.objects.new(name,d);current.objects.link(o)
    if material: d.materials.append(material)
    for p in d.polygons: p.use_smooth=smooth
    return o
def active(o):
    bpy.ops.object.select_all(action='DESELECT');o.select_set(True);bpy.context.view_layer.objects.active=o
def apply(o,mod): active(o);bpy.ops.object.modifier_apply(modifier=mod.name)
def ell(name,p,s,m,segments=24,rings=16):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=segments,ring_count=rings,location=p)
    o=link(bpy.context.object);o.name=name;o.scale=s
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    o.data.materials.append(m)
    for f in o.data.polygons:f.use_smooth=True
    return o
def cube(name,p,s,m,bevel=.05):
    bpy.ops.mesh.primitive_cube_add(size=1,location=p);o=link(bpy.context.object);o.name=name;o.scale=s
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True);o.data.materials.append(m)
    if bevel:
        b=o.modifiers.new('Soft hand-worked edges','BEVEL');b.width=bevel;b.segments=2;apply(o,b)
        n=o.modifiers.new('Weighted normals','WEIGHTED_NORMAL');apply(o,n)
    return o
def tube(name,points,radii,m,sides=8):
    verts=[];faces=[]
    for i,p in enumerate(points):
        p=Vector(p);t=Vector(points[min(i+1,len(points)-1)])-Vector(points[max(0,i-1)])
        t.normalize();u=t.cross(Vector((0,1,0)) if abs(t.y)<.9 else Vector((1,0,0))).normalized();v=t.cross(u)
        for j in range(sides):verts.append(p+radii[i]*(u*cos(j*2*pi/sides)+v*sin(j*2*pi/sides)))
    for i in range(len(points)-1):
        for j in range(sides):a=i*sides+j;b=i*sides+(j+1)%sides;faces.append((a,b,b+sides,a+sides))
    faces.extend([tuple(reversed(range(sides))),tuple(range((len(points)-1)*sides,len(points)*sides))])
    return mesh(name,verts,faces,m,True)
def curve(name,points,r,m,sides=7):return tube(name,points,[r]*len(points),m,sides)
def leaf(name,base,tip,width,m):
    a=Vector(base);b=Vector(tip);d=b-a;u=d.cross(Vector((0,0,1))).normalized()*width
    mid=a+d*.48+Vector((0,0,width*.25))
    return mesh(name,[a,mid+u,b,mid-u,mid+Vector((0,0,width*.34))],[(0,1,4),(1,2,4),(2,3,4),(3,0,4)],m,True)
def merge_sculpt(parts,name,voxel=.055):
    bpy.ops.object.select_all(action='DESELECT')
    for o in parts:o.select_set(True)
    bpy.context.view_layer.objects.active=parts[0];bpy.ops.object.join();o=bpy.context.object;o.name=name
    r=o.modifiers.new('Unified clay volume','REMESH');r.mode='VOXEL';r.voxel_size=voxel;apply(o,r)
    s=o.modifiers.new('Sculpt polish','SMOOTH');s.factor=1.5;s.iterations=5;apply(o,s)
    d=o.modifiers.new('Mobile sculpt topology','DECIMATE');d.ratio=.48;apply(o,d)
    for p in o.data.polygons:p.use_smooth=True
    return o

# The silhouette is sculpted into one continuous surface, including cheeks, ears and toes.
parts=[]
for name,p,s in [
 ('Belly',(0,.05,1.9),(1.55,1.02,1.7)),('Chest',(0,.12,3),(1.8,.9,1.12)),
 ('Neck',(0,-.02,3.6),(1.05,.8,.9)),('Head',(0,-.12,4.35),(1.27,.9,1.25)),
 ('Jaw',(0,-.48,3.8),(.95,.65,.7)),('Cheek L',(-.78,-.72,4.12),(.51,.39,.45)),('Cheek R',(.78,-.72,4.12),(.51,.39,.45)),
 ('Brow L',(-.57,-.66,4.82),(.64,.35,.32)),('Brow R',(.57,-.66,4.82),(.64,.35,.32)),
 ('Ear L',(-1.27,-.02,4.48),(.53,.27,.66)),('Ear R',(1.27,-.02,4.48),(.53,.27,.66)),
 ('Haunch L',(-1.05,.18,.7),(.8,.85,.8)),('Haunch R',(1.05,.18,.7),(.8,.85,.8)),
 ('Foot L',(-1,-.73,.31),(.72,1,.35)),('Foot R',(1,-.73,.31),(.72,1,.35))]:parts.append(ell(name,p,s,skin))
for sign in [-1,1]:
    for i in range(4):parts.append(ell('Toe',(sign*(.55+i*.28),-1.48+abs(i-1.5)*.05,.25),(.19,.36,.22),skin,16,10))
parts.append(tube('Resting arm',[(-1.35,.1,3.08),(-1.9,-.02,2.55),(-2,-.45,1.95),(-1.75,-.98,1.56)],[.63,.57,.46,.5],skin,12))
parts.append(ell('Resting palm',(-1.62,-1.06,1.48),(.58,.37,.32),skin))
for i in range(4):parts.append(tube('Fingers',[(-2+i*.25,-1.12,1.47),(-2+i*.25,-1.5,1.3),(-1.95+i*.25,-1.58,1.12)],[.16,.15,.12],skin))
merge_sculpt(parts,'Rumle • continuous body sculpt')
for sign in [-1,1]:
    o=ell('Recessed ear', (sign*1.4,-.245,4.5),(.25,.055,.37),inner)
    o.rotation_euler[1]=sign*-.28
    ell('Warm cheek',(sign*.91,-.99,4.13),(.25,.045,.14),rose,20,12)
    # Wide-set friendly eyes with fitted upper and lower lids.
    ell('Eye',(sign*.51,-.925,4.57),(.325,.18,.30),eye)
    ell('Hazel iris',(sign*.49,-1.096,4.565),(.145,.035,.17),iris)
    ell('Pupil',(sign*.49,-1.126,4.57),(.072,.018,.105),black,16,12)
    ell('Eye catchlight',(sign*.49-.035,-1.143,4.64),(.035,.015,.039),eye,12,8)
    curve('Lower eyelid',[(sign*.51+cos(t)*.322,-.99,4.57+sin(t)*.30) for t in [pi+i*pi/12 for i in range(13)]],.056,skinLight)
    curve('Upper eyelid',[(sign*.51+cos(t)*.328,-.99,4.57+sin(t)*.31) for t in [i*pi/12 for i in range(13)]],.073,skinLight)
    curve('Laugh crease',[(sign*(.91+i*.045),-1.005+i*.025,4.37-i*.028) for i in range(5)],.025,inner,5)
ell('Nose bridge',(0,-.99,4.48),(.32,.28,.52),skinLight)
ell('Rosy pear nose',(0,-1.2,4.26),(.51,.43,.39),rose,32,20)
for sign in [-1,1]:ell('Nostril',(sign*.22,-1.47,4.08),(.10,.04,.063),nostril,16,10)
# Smile is a shaped inset panel, following the face, framed by a curled lip.
v=[(0,-1.108,3.88)];outline=[]
for i in range(41):
    t=2*pi*i/40;x=.77*cos(t);z=3.91+.37*sin(t)+.18*abs(cos(t))
    outline.append((x,-1.09+.28*(x/.77)**2,z))
v+=outline;mesh('Open happy smile',v,[(0,i+1,i+2) for i in range(40)],dark,True)
curve('Sculpted smile rim',outline,.075,skinLight)
for i,(x,w,h) in enumerate([(-.49,.18,.21),(-.25,.22,.17),(.03,.23,.19),(.31,.19,.14),(.51,.16,.18)]):
    o=cube('Little uneven tooth',(x,-1.105+.28*(x/.77)**2,4.04+.1*abs(x)-h/2),(w,.13,h),ivory,.045);o.rotation_euler[1]=x*.16
ell('Lower lip',(0,-1.08,3.56),(.46,.08,.075),skinLight)
for i in range(32):
    x=random.uniform(-1.15,1.15);z=random.uniform(1.05,3.2)
    y=-sqrt(max(.05,1-(x/1.65)**2-((z-2)/1.85)**2))*.98
    ell('Granite freckle',(x,y-.015,z),(.022,.01,.018),inner,8,6)
# Pointed, overlapping moss locks, with ferns growing out of the scalp.
for i in range(135):
    a=random.uniform(0,2*pi);t=random.uniform(.08,1.47)
    x=1.23*sin(t)*cos(a);y=-.1+.88*sin(t)*sin(a);z=4.5+1.1*cos(t)
    base=Vector((x,y,z));tip=base+Vector((cos(a)*.25,sin(a)*.28,-random.uniform(.15,.5)))
    leaf('Moss lock',base,tip,random.uniform(.09,.17),random.choice(moss[1:]))
for sign in [-1,1]:
    for i in range(13):
        x=sign*(.18+i*.055);leaf('Moss eyebrow',(x,-.91,4.99),(x+sign*.05,-1.04,4.86),.06,moss[2+i%3])
for a in [0,.9,2.7,4.1,5.4]:
    base=Vector((.7*cos(a),.5*sin(a),5.35));tip=base+Vector((.3*cos(a),.3*sin(a),.6))
    curve('Fern stalk',[base,(base+tip)/2,tip],.018,moss[1],5)
    for j in range(1,6):
        center=base+(tip-base)*j/7
        for side in [-1,1]:leaf('Fern leaflet',center,center+Vector((side*.24*(1-j/8),-.03,.04)),.035,moss[2+j%3])
for i in range(25):
    x=random.choice([-1,1])*random.uniform(1.1,1.6);z=random.uniform(2.5,3.5)
    leaf('Shoulder moss',(x,.05,z),(x*1.12,-.18,z-.3),.12,random.choice(moss))

# Separate seamless waving arm, in local shoulder coordinates used by the game.
current=ARM;parts=[tube('Arm',[(0,0,0),(.36,-.12,-.52),(.52,-.5,-1.02)],[.58,.48,.36],skin,12),ell('Palm',(.49,-.6,-1.26),(.42,.29,.42),skin)]
for i in range(4):parts.append(tube('Finger',[(.18+i*.22,-.64,-1.4),(.18+i*.22,-.73,-1.72),(.17+i*.22,-.77,-1.85)],[.13,.12,.08],skin))
parts.append(tube('Thumb',[(.12,-.54,-1.17),(-.14,-.72,-1.31),(-.17,-.83,-1.51)],[.18,.14,.10],skin))
merge_sculpt(parts,'Rumle • articulated arm',.045)

# Build reusable terrain assets from the same height field as collision/navigation.
current=OUTSIDE
def H(x,y):
    z=-y;ridge=max(0,min(1,(3-z)/24))*6
    lake=((x+3)/10)**2+((z-1)/8)**2;island=exp(-((x+3)**2+(z-1)**2)*.11)*4.5
    return 1.65+ridge+.22*sin(x*.44)*cos(z*.3)-max(0,1-lake)*4.5+island
def ground(x,y):return max(2.55,H(x,y)) if -13<=x<=8 and abs(y+1)<.9 else H(x,y)
def n(p):return noise.noise_vector(Vector(p))[0]
def terrain():
    size=52;step=.5;verts=[];faces=[]
    for j in range(105):
        y=-26+j*step
        for i in range(105):x=-26+i*step;verts.append((x,y,H(x,y)))
    for j in range(104):
        for i in range(104):a=j*105+i;faces.append((a,a+1,a+106,a+105))
    o=mesh('Continuous walkable highland',verts,faces,grass[0],True)
    for m in grass[1:]+granite+[pathmat]:o.data.materials.append(m)
    for p in o.data.polygons:
        x,y,z=p.center # update below uses vertices to avoid stale centers
        co=sum((o.data.vertices[k].co for k in p.vertices),Vector())/len(p.vertices);x,y,z=co
        ridge=abs(n((x*.16,y*.16,1)))
        p.material_index=4+int(abs(n((x*.8,y*.8,2)))*4)%5 if ridge>.33 or z<.9 else int(abs(n((x*.18,y*.18,4)))*10)%4
    return o
terrain()
def rock(name,p,s,seed=0,material=None,sub=2):
    rng=random.Random(seed);bpy.ops.mesh.primitive_ico_sphere_add(subdivisions=sub,radius=1,location=p)
    o=link(bpy.context.object);o.name=name
    for v in o.data.vertices:
        q=v.co.copy();d=noise.noise_vector(q*2+Vector((seed*.13,3,1)))[0]
        # Vertical fissures and broad planes read as granite, rather than round bubbles.
        q*=1+d*.21;q.x+=sin(q.z*2.5+seed)*.06;q.z=max(q.z,-.6)
        v.co=Vector((q.x*s[0],q.y*s[1],q.z*s[2]))
    for m in granite:o.data.materials.append(m)
    for f in o.data.polygons:f.material_index=(seed+f.index//7)%len(granite);f.use_smooth=False
    if material:o.data.materials.clear();o.data.materials.append(material)
    b=o.modifiers.new('Eroded edges','BEVEL');b.width=.06*min(s);b.segments=1;apply(o,b)
    return o
# Cliff forms are radial fans of vertical strata with connected brows.
for i in range(29):
    x=-27+i*1.9;y=23+sin(i*.8)*1.4;h=8+4*(.5+.5*sin(i*.33))
    rock('Heileberget • striated ridge',(x,y,5+h*.38),(2.4,3.7,h),500+i,sub=2)
    rock('Mossy upper ledge',(x,y-1.3,7+h),(2.1,3.1,.65),600+i,grass[i%4],2)
for i in range(35):
    a=i*2*pi/35;x=-3+cos(a)*10.3;y=-1+sin(a)*8.4
    if abs(y+1)<1.2:continue
    rock('Tarn shoreline',(x,y,max(.7,H(x,y))),(random.uniform(.7,1.8),random.uniform(.5,1.3),random.uniform(.3,.9)),i+20)
for i in range(30):
    x=random.uniform(-24,24);y=random.uniform(-23,19)
    if H(x,y)<1.15 or (abs(y+1)<1.8 and -14<x<9) or (x>8 and y>7):continue
    rock('Heather outcrop',(x,y,H(x,y)-.25),(random.uniform(1,2.4),random.uniform(.9,1.9),random.uniform(.5,1.1)),i+80)
# Cave portal: carefully placed voussoirs around a genuinely open dark entrance.
for i in range(13):
    t=pi*i/12;x=13+4.4*cos(t);z=H(13,15)+.2+6*sin(t)
    rock('Cave arch • stone %02d'%i,(x,16,z),(1.15,2.8,1.45),900+i)
for sign in [-1,1]:
    rock('Cave jamb',(13+sign*4.4,15,7.3),(1.25,2.5,2.2),930+sign)
    rock('Cave roof moss',(13+sign*2,16,14),(2.3,2.8,.55),940+sign,grass[2])
cube('Deep cave shadow',(13,19.5,9.7),(8,.2,8),dark,0)
# Water stays a separate mesh so the Android shader can animate highlights.
WATER=collection('Tarn • water');current=WATER
mesh('Tarn surface',[(-14,-10,.85),(8,-10,.85),(8,8,.85),(-14,8,.85)],[(0,1,2,3)],watermat)
current=OUTSIDE
# Timber footbridge, pegged posts, sagging rope, individual slightly crooked planks.
for i in range(66):
    x=-13+i*.32;z=max(2.55,H(x,-1));o=cube('Bridge plank',(x,-1,z-.09),(.30,1.62,.16),woodLight if i%4==0 else wood,.035)
    o.rotation_euler[2]=sin(i*3)*.018
for x in range(-13,9,3):
    z=max(2.55,H(x,-1))
    for y in [-1.85,-.15]:
        tube('Bridge post',[(x,y,z-.6),(x+.06,y,z+1)],[.11,.085],wood)
        ell('Carved post cap',(x+.06,y,z+1.03),(.14,.14,.11),woodLight,10,8)
        if x<8:
            curve('Rope rail',[(x+t*3,y,z+.82-sin(t*pi)*.18) for t in [i/8 for i in range(9)]],.045,woodLight,5)
# Continuous footpaths, lying close to the collision height, are legible from above.
def path(points,width=1.05):
    verts=[]
    for i,(x,y) in enumerate(points):
        a=Vector(points[max(0,i-1)]);b=Vector(points[min(len(points)-1,i+1)]);d=(b-a).normalized();side=Vector((-d.y,d.x))
        for sign in [-1,1]:
            xx=x+side.x*width*sign;yy=y+side.y*width*sign;verts.append((xx,yy,ground(xx,yy)+.035))
    return mesh('Winding ochre trail',verts,[(i*2,i*2+1,i*2+3,i*2+2) for i in range(len(points)-1)],pathmat,True)
path([(1+sin(i*.21)*3,-23+i*.6) for i in range(29)],.7)
path([(9+sin(i*.14)*6,-8+i*.55) for i in range(50)],.75)
path([(-14-sin(i*.12)*3,-17+i*.55) for i in range(65)],.65)

def birch(x,y,scale,seed):
    rng=random.Random(seed);base=H(x,y);lean=rng.uniform(-.35,.35)
    tube('Birch • pale trunk',[(x,y,base),(x+.1,y,base+1.3*scale),(x+lean,y+.1,base+3.7*scale)],[.16*scale,.115*scale,.035*scale],bark,7)
    for j in range(6):
        z=base+(.3+j*.46)*scale
        curve('Birch • dark lenticel',[(x-.12*scale,y-.09*scale,z),(x+.03*scale,y-.12*scale,z+.025),(x+.09*scale,y-.08*scale,z)],.023*scale,barkDark,4)
    for j in range(6):
        a=j*2.4;root=Vector((x+lean*.5,y,base+(1.7+j*.26)*scale));tip=root+Vector((cos(a)*1.15*scale,sin(a)*1.15*scale,.7*scale))
        tube('Birch • branch',[root,(root+tip)/2,tip],[.065*scale,.04*scale,.012*scale],bark,5)
        # Distinct flat leaf sprays, not spherical foliage.
        for k in range(20):
            p=tip+Vector((rng.uniform(-.65,.65)*scale,rng.uniform(-.6,.6)*scale,rng.uniform(-.28,.55)*scale))
            t=p+Vector((rng.uniform(-.32,.32)*scale,rng.uniform(-.32,.32)*scale,-.25*scale))
            leaf('Birch • leaf',p,t,.17*scale,moss[1+(k+j)%4])
for i,(x,y,s) in enumerate([(-17,-14,1.1),(-20,-10,.9),(-14,-8,.8),(-2,-1,.75),(-4,-.3,.65),(-2.5,.8,.85),
 (9,-8,.9),(18,-7,1.25),(21,-4,1),(19,3,1.1),(8,8,.8),(7,13,.85),(-15,11,.85),(-20,16,1),(-22,7,1.2),(-18,1,.9),(22,17,.8)]):birch(x,y,s,100+i)
# Dense small plants carry detail at ground level, in deliberately grouped patches.
for i in range(420):
    x=random.uniform(-24,24);y=random.uniform(-23,21);z=H(x,y)
    if z<1.1 or abs(y+1)<1.3 or (x>8 and y>11) or abs(x-(1+sin((y+23)/.6*.21)*3))<1:continue
    h=random.uniform(.15,.4)
    for j in range(3):
        a=j*2.2+i;leaf('Heather / grass',(x,y,z),(x+cos(a)*h,y+sin(a)*h,z+h),h*.22,moss[1+j])
    if i%5==0:
        for j in range(3):
            a=j*2.1;ell('Heather flower',(x+cos(a)*.13,y+sin(a)*.13,z+h),(.065,.065,.1),berry,6,4)
# Cairn on the high lookout.
for j in range(6):rock('Lookout cairn',(18,20,H(18,20)+j*.35),(.85-j*.105,.65-j*.08,.23),1200+j)

# Warm, spacious cave interior, raised side shelves and a generous clear walking area.
current=CAVE
verts=[];faces=[]
for j in range(57):
    y=-14+j*.5
    for i in range(49):x=-12+i*.5;z=max(0,min(4.4,(8+y)*.23)) if x>3 else 0;verts.append((x,y,z))
for j in range(56):
    for i in range(48):a=j*49+i;faces.append((a,a+1,a+50,a+49))
mesh('Cave walkable floor',verts,faces,granite[0],True)
for i in range(22):
    a=pi*.10+i*pi*1.8/21;x=13.4*cos(a);y=1+14*sin(a)
    if y<-10 and abs(x)<6:continue
    rock('Cavern fluted wall',(x,y,5),(2.4,2.3,6),1600+i)
    for j in range(2):
        if i%3==0:rock('Cavern ledge',(x*.93,y*.93,3+j*3.7),(2.1,2.1,.38),1700+i+j)
for i in range(8):
    x=random.uniform(-11,11);y=random.uniform(4,13)
    tube('Stalactite',[(x,y,12),(x+.13,y,10.9),(x+.24,y+.1,9.8)],[.5,.28,.015],granite[2],7)
for i in range(10):
    x=-11+i*.5;y=11+sin(i)*.5
    tube('Crystal column',[(x,y,0),(x,y,.3+random.random()*1.6),(x+.06,y,2+random.random())],[.25,.28,0],gem,6)
def bottle(x,y,z,urge=False,s=1):
    m=sodaGreen if urge else sodaBlack
    tube('Urge bottle' if urge else 'Pepsi Max bottle',[(x,y,z),(x,y,z+.07*s),(x,y,z+.65*s),(x,y,z+.78*s),(x,y,z+.95*s)],[.15*s,.19*s,.19*s,.09*s,.08*s],m,12)
    tube('Bottle label',[(x,y,z+.24*s),(x,y,z+.53*s)],[.193*s,.193*s],labelGreen if urge else labelBlue,12)
    tube('Bottle cap',[(x,y,z+.94*s),(x,y,z+1.01*s)],[.09*s,.09*s],labelGreen if urge else labelRed,12)
def gateau(x,y,z,s=1):
    tube('Cake layers',[(x,y,z),(x,y,z+.4*s)],[.55*s,.55*s],cake,20)
    tube('Cream icing',[(x,y,z+.39*s),(x,y,z+.48*s)],[.57*s,.57*s],cream,20)
    for i in range(8):a=i*pi/4;ell('Strawberries',(x+cos(a)*.4*s,y+sin(a)*.4*s,z+.55*s),(.08*s,.08*s,.12*s),berry,10,6)
for side in [-1,1]:
    x=side*8.5;y=6;floor=max(0,min(4.4,(8+y)*.23)) if x>3 else 0
    for j in range(3):
        z=floor+.8+j*1.25;cube('Larder • wooden shelf',(x,y,z),(4.8,1.3,.18),wood)
        for k in range(8):
            if side>0:bottle(x-2+k*.52,y-.15,z+.11,k%2==0,.78)
            elif j%2==0:gateau(x-1.6+(k%3)*1.5,y,z+.1,.62) if k<3 else None
            else:
                ell('Sweet jar',(x-1.9+k*.52,y,z+.4),(.21,.21,.3),[labelRed,labelGreen,gem][k%3],12,8)
    for dx in [-2.4,2.4]:cube('Larder posts',(x+dx,y,floor+2),( .18,1.5,4.2),wood)
# A rug, table, cake stand and piles of softly bevelled gold give the troll a home.
cube('Woven rug',(-3,-.6,.035),(7,5,.035),mat('Rug • terracotta','994F49'),.03)
for x in [-6.3,.3]:cube('Rug border',(x,-.6,.06),(.16,4.8,.02),cream,0)
cube('Cake table',(-8,2,.75),(3.5,2,.22),woodLight,.1)
for x in [-9.3,-6.7]:
    for y in [1.25,2.75]:cube('Table legs',(x,y,.4),(.22,.22,.8),wood,.04)
gateau(-8,2,.88,1.1)
for i in range(36):
    x=-9+random.uniform(-1.8,1.8);y=10+random.uniform(-1.6,1.6);z=.15+max(0,1-abs(x+9)/2-abs(y-10)/2)*.8
    o=cube('Gold ingot',(x,y,z),(.52,.29,.24),gold,.07);o.rotation_euler[2]=random.uniform(-pi,pi)
for i in range(9):
    x=-10+random.random()*2;y=9+random.random()*2
    tube('Loose diamond',[(x,y,.2),(x,y,.6),(x,y,.95)],[0,.25,0],gem,6)
# Bottles at the actual reachable interaction spots are always clear.
bottle(6,5,2.99,False,1.4);bottle(9,5,2.99,True,1.4)

def area(name,p,power,color,size,target):
    d=bpy.data.lights.new(name,'AREA');d.energy=power;d.color=color;d.shape='DISK';d.size=size
    o=bpy.data.objects.new(name,d);STUDIO.objects.link(o);o.location=p;o.rotation_euler=(Vector(target)-o.location).to_track_quat('-Z','Y').to_euler();return o
current=STUDIO
area('Large soft sun',(-18,-18,35),3000,(1,.84,.64),15,(0,0,2))
area('Sky fill',(10,0,27),2100,(.62,.79,1),25,(0,0,0))
sun=bpy.data.lights.new('Evening sun','SUN');sun.energy=2.2;sun.angle=.12
o=bpy.data.objects.new('Evening sun',sun);STUDIO.objects.link(o);o.rotation_euler=(.5,-.4,-.4)
scene.world.color=(.3,.3,.3);scene.world.use_nodes=True
bg=scene.world.node_tree.nodes.get('Background');bg.inputs[0].default_value=(.45,.64,.85,1);bg.inputs[1].default_value=.45
cam=bpy.data.cameras.new('Art camera');camera=bpy.data.objects.new('Art camera',cam);STUDIO.objects.link(camera);scene.camera=camera
def point_camera(p,target,lens=45):camera.location=p;camera.rotation_euler=(Vector(target)-camera.location).to_track_quat('-Z','Y').to_euler();cam.lens=lens
point_camera((30,-43,27),(1,3,4),43)
scene.render.engine='CYCLES';scene.cycles.samples=48;scene.cycles.use_denoising=True
scene.render.resolution_x=1600;scene.render.resolution_y=1000;scene.render.resolution_percentage=100
scene.view_settings.view_transform='AgX';scene.render.image_settings.file_format='PNG'
# Collection instances keep the editable sculpt in one place and supply an outside preview.
TROLL.hide_render=True;ARM.hide_render=True;CAVE.hide_render=True
for col,pos in [(TROLL,(13,15,H(13,15))),(ARM,(14.6,14.5,H(13,15)+2.8))]:
    obj=bpy.data.objects.new('Rumle outside • '+col.name,None);OUTSIDE.objects.link(obj);obj.instance_type='COLLECTION';obj.instance_collection=col;obj.location=pos
# Collection visibility also hides instances, so originals move to an excluded source collection later.
TROLL.hide_render=False;ARM.hide_render=False
for o in TROLL.objects:o.hide_render=False
for o in ARM.objects:o.hide_render=False
scene['art_version']='Blender sculpt 1';scene['user_request']='Proper Blender assets via MCP; free roaming native Android'
bpy.ops.wm.save_as_mainfile(filepath=OUT+'/Heileberget.blend')
print('ART_READY',len(bpy.data.objects),'objects',sum(len(o.data.polygons) for o in bpy.data.objects if o.type=='MESH'),'polygons')
print('ENGINES',[(e.identifier,e.name) for e in scene.render.bl_rna.properties['engine'].enum_items])
