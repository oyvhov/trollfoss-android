"""Photo-inspired granite ridges with real depth and soft cumulus volumes.
Follow harmony_pass.py. Idempotent replacement of the distant scenery only.
All walkable terrain and landmarks keep their positions.
"""
import bpy, ast, pathlib, math, random
from math import sin, cos, pi, exp, sqrt, tanh
from mathutils import Vector, noise

ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
OUTSIDE=bpy.data.collections['Heileberget • landscape'];SKY=bpy.data.collections['Sky • painted atmosphere']
M=bpy.data.materials;vertexmat=M['Painted natural albedo'];current=OUTSIDE
random.seed(2506)

def clamp(t):return max(0,min(1,t))
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def paint(o,fn,emissive=False):
    attr=o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in o.data.vertices:
        attr.data[v.index].color=(*[linear(clamp(c)) for c in fn(v.co,v.normal)],1)
    o.data.materials.append(M['Sky gradient emission'] if emissive else vertexmat)

for o in list(OUTSIDE.objects):
    if o.name.startswith(('Heileberget granite massif','Heileberget • continuous granite','Heileberget • heather plateau','Distant blue mountains','Ridge •')):
        bpy.data.objects.remove(o,do_unlink=True)
for o in list(SKY.objects):
    if o.name.startswith(('Soft cloud','Cumulus •')):bpy.data.objects.remove(o,do_unlink=True)

# The long stepped crest is inspired by the supplied broad granite escarpments.
# Eroded ravines continue from foot to crest, instead of stacked cube-like rocks.
def crest(x):
    t=clamp((abs(x)-48)/62);envelope=1-t*t*(3-2*t)
    return (20+4.3*sin(x*.055+.7)+2.0*sin(x*.15)+.7*sin(x*.39)+.3*sin(x*.83))*envelope
def front(x):return 37+4*sin(x*.052)-1.3*cos(x*.19)
fractures=[(-125,0),(-103,2),(-87,-1),(-71,3),(-59,-1.5),(-48,2.5),(-40,-2.5),(-32,2.5),(-26,-1.8),(-17,3.7),(-8,-2.1),(-1,1.8),(8,-1.5),(16,3.2),(24,-2.8),(31,2.7),(41,-1.2),(53,3.2),(65,-1.4),(81,2.4),(100,-1),(125,0)]
def fracture(x):
    for (a,va),(b,vb) in zip(fractures,fractures[1:]):
        if x<=b:return va+(vb-va)*clamp((x-a)/(b-a))
    return 0
def facepoint(x,t):
    top=crest(x)
    # Broad fractured shelves, slanted seams and irregular erosion. Vertical sine
    # furrows alone read like a curtain in the actual game, regardless of texture.
    q=Vector((x*.16,4.3,t*5.7))
    broad=fracture(x+1.3*t)+noise.noise_vector(q)[0]*.38
    fine=noise.noise_vector(q*3.1)[1]*.14
    ledges=sum((.5+.5*tanh((t-level-.025*sin(x*.23+phase)-.018*x/65)*55))*depth
               for level,depth,phase in [(.19,1.5,.4),(.45,1.05,2),(.73,1.6,4),(.91,.65,1)])
    ravine=3.4*exp(-((x-18-3*t)/3.1)**2)+2.4*exp(-((x+31+2*t)/2.3)**2)
    talus=-6.5*max(0,1-t/.27)**1.4
    y=front(x)+ledges+broad+fine+ravine+talus
    z=-3+(top+3)*t
    return (x,y,z)
verts=[];faces=[];nx=321;nz=65
for j in range(nz):
    for i in range(nx):verts.append(facepoint(-120+i*.75,j/(nz-1)))
for j in range(nz-1):
    for i in range(nx-1):
        a=j*nx+i;faces.append((a,a+1,a+nx+1,a+nx))
o=mesh('Ridge • Heileberget granite escarpment',verts,faces,None,True)
def stone(p,n):
    vein=.55+noise.noise_vector(Vector((p.x*.17,p.y*.28,p.z*.22)))[0]*.55
    c=blend(rgb('737F92'),rgb('C4BDB1'),clamp(vein))
    seam=.97+.025*sin(p.z*2.3+p.x*.17+noise.noise_vector(p*.3)[1]*3)
    moss=clamp((n.z-.18)*1.7)*clamp(.6+noise.noise_vector(p*.26)[0])
    return blend(tuple(v*seam for v in c),rgb('7E956E'),moss*.72)
paint(o,stone)

# A thick, rounded plateau continues behind the crest and down the reverse face.
verts=[];faces=[];ny=29
for j in range(ny):
    t=j/(ny-1)
    for i in range(nx):
        x=-120+i*.75;edge=facepoint(x,1)
        y=edge[1]+t*47
        z=crest(x)*(1-t**1.7)+sin(t*pi)*2.8-3*t
        z+=sin(t*pi)*noise.noise_vector(Vector((x*.16,y*.2,4)))[0]*1.4
        verts.append((x,y,z))
for j in range(ny-1):
    for i in range(nx-1):
        a=j*nx+i;faces.append((a,a+1,a+nx+1,a+nx))
o=mesh('Ridge • rounded high plateau',verts,faces,None,True)
paint(o,lambda p,n:blend(stone(p,n),rgb('879D7E'),clamp((n.z-.4)*1.4)))

# Three-dimensional ranges wrap around the fjord side as well: no vertical cards.
for layer in range(2):
    verts=[];faces=[];na=256;nr=15
    for j in range(nr):
        t=j/(nr-1);radius=66+layer*37+t*35
        for i in range(na+1):
            a=i*2*pi/na
            h=11+4*sin(a*3+layer*.7)+2.2*sin(a*7+1.3)+1.1*sin(a*17)
            h*=.85+.15*sin(a)
            z=-4+max(0,sin(t*pi))**.75*h
            z+=sin(t*pi)*(.7*sin(a*31+t*4)+.4*sin(a*59-t*5))
            verts.append((cos(a)*radius,sin(a)*radius,z))
    for j in range(nr-1):
        for i in range(na):
            a=j*(na+1)+i;faces.append((a,a+na+1,a+na+2,a+1))
    o=mesh('Ridge • distant rounded range %d'%layer,verts,faces,None,True)
    base=rgb('6F8C9E') if layer==0 else rgb('A3B9C4')
    paint(o,lambda p,n,c=base:tuple(v*(.86+.18*max(0,n.dot(Vector((-.5,-.4,.85)).normalized()))) for v in c))

# Build softly overlapping cloud lobes directly as meshes, avoiding thousands of bpy ops.
current=SKY
for k in range(7):
    angle=.16+k*2*pi/7;cx=cos(angle)*108;cy=sin(angle)*108;cz=32+sin(k*2)*6
    verts=[];faces=[];sides=24;rings=12
    for l in range(5):
        off=(l-2)*4.7
        centre=Vector((cx+cos(angle+.8)*off,cy+sin(angle+.8)*off,cz+[0,1.5,3,1.7,-.4][l]))
        rx=[5.2,6,7,6,4.7][l];rz=[2.4,3.8,5.2,3.5,2.1][l]
        start=len(verts)
        for j in range(rings+1):
            v=j*pi/rings
            for i in range(sides+1):
                u=i*2*pi/sides
                wobble=1+.035*sin(u*5+v*3+k)
                verts.append(centre+Vector((sin(v)*cos(u)*rx*wobble,sin(v)*sin(u)*rx*.63,cos(v)*rz)))
        for j in range(rings):
            for i in range(sides):
                a=start+j*(sides+1)+i;faces.append((a,a+sides+1,a+sides+2,a+1))
    o=mesh('Cumulus • warm billowing cloud %02d'%k,verts,faces,None,True)
    # Union the overlapping lobes into a single smooth cloud surface so their
    # intersections cannot appear as stacked discs when the camera moves.
    r=o.modifiers.new('Joined cloud volume','REMESH');r.mode='VOXEL';r.voxel_size=.8;apply(o,r)
    s=o.modifiers.new('Soft cloud edges','SMOOTH');s.factor=1.0;s.iterations=5;apply(o,s)
    d=o.modifiers.new('Cloud mobile topology','DECIMATE');d.ratio=.5;apply(o,d)
    for poly in o.data.polygons:poly.use_smooth=True
    o.data.update()
    paint(o,lambda p,n:blend(rgb('C0D2DF'),rgb('FFF5E4'),clamp(.64+n.z*.26+n.x*-.08)),True)

# A shaped dark recess stays inside the arch; a rectangular card poked out above
# the left shoulder of the entrance when the Android camera moved sideways.
current=OUTSIDE
old=bpy.data.objects.get('Deep cave shadow')
if old:bpy.data.objects.remove(old,do_unlink=True)
verts=[(9.4,19.5,4.7),(16.6,19.5,4.7)]
verts.extend((13+3.6*cos(i*pi/24),19.5,6.2+5.2*sin(i*pi/24)) for i in range(25))
mesh('Deep cave shadow',verts,[tuple(range(len(verts)))],M['Smile interior'],False)

# The relocated pan must sit on its new bank, including all five gold nuggets.
pan=bpy.data.objects.get('Gold washing pan')
if pan:
    dz=H(pan.location.x,pan.location.y)+.1-pan.location.z
    pan.location.z+=dz
    for o in OUTSIDE.objects:
        if o.name.startswith('Gold in pan'):o.location.z+=dz

scene['art_version']='Round 7 • rounded ridge ends, shaped entrance and unified clouds'
scene.camera=bpy.data.objects['Landscape camera'];scene.camera.location=(10,-34,9)
scene.camera.rotation_euler=(Vector((0,8,7))-scene.camera.location).to_track_quat('-Z','Y').to_euler()
scene.camera.data.lens=30;scene.render.filepath='C:/topa/dist/heileberget-blender/landscape-ridges-round7.png'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
def preview():bpy.ops.render.render(write_still=True);return None
if globals().get('RENDER_PREVIEW',True):bpy.app.timers.register(preview,first_interval=1)
print('RIDGES_AND_CLOUDS_READY')
