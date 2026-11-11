"""Broad fractured granite planes inspired by the user's long mountain escarpment.

Replace only the two distant ridge surfaces. The playable ground, entrance,
clouds and horizon remain separate. Idempotent; fewer triangles than the soft ridges.
"""
import bpy, ast, pathlib, math
from math import sin, cos, pi, exp
from mathutils import Vector, noise

ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
current=bpy.data.collections['Heileberget • landscape'];M=bpy.data.materials
for ob in list(current.objects):
    if ob.name in ('Ridge • Heileberget granite escarpment','Ridge • rounded high plateau'):
        bpy.data.objects.remove(ob,do_unlink=True)

def clamp(v):return max(0,min(1,v))
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def linear_profile(points,x):
    for (a,va),(b,vb) in zip(points,points[1:]):
        if x<=b:return va+(vb-va)*clamp((x-a)/(b-a))
    return points[-1][1]

heights=[(-120,-2),(-100,0),(-78,6),(-61,13),(-48,20),(-38,21),(-29,24),
         (-22,23),(-17,27),(-10,26.5),(-3,23),(5,23.5),(12,25),(20,24),
         (29,21),(35,22.5),(44,19),(55,14),(70,7),(92,0),(120,-2)]
faults=[(-120,0),(-90,1),(-70,-1),(-57,2),(-46,-2),(-37,2.6),(-29,-1.8),
        (-23,2),(-17,-2),(-10,1.7),(-4,-1.5),(3,2),(11,-2.1),(17,2.7),
        (24,-1.8),(32,2.5),(43,-1.7),(53,2),(67,-1.2),(84,1),(120,0)]
def crest(x):return linear_profile(heights,x)+.18*sin(x*.61)
def facepoint(x,t):
    # Faults lean slightly, but keep broad vertical faces rather than full-width folds.
    lean=x+1.4*t
    # Broken shelves vary across the cliff instead of forming a horizontal ribbon.
    rock=noise.noise_vector(Vector((x*.24,1.3,t*9.5)))[0]
    rough=math.copysign(abs(rock)**.65,rock)*.68
    shelves=(max(0,1-abs(t-(.30+.065*sin(x*.14)))/.045)*.55
             +max(0,1-abs(t-(.66+.08*cos(x*.11+.6)))/.06)*.75)
    shelves*=.35+.65*max(0,sin(x*.21+1))
    ravine=3.7*exp(-((x-19-2*t)/2.6)**2)+2.3*exp(-((x+32+2*t)/2.0)**2)
    talus=-5.5*max(0,1-t/.24)**1.25
    y=38+3.6*sin(x*.052)+linear_profile(faults,lean)+ravine+talus+rough
    y+=t*.65+.18*sin(t*12+x*.035)-shelves
    return Vector((x,y,-3+(crest(x)+3)*t))

def finish(ob,plateau=False):
    # Keep a welded surface. Splitting sharp folds before the mobile decimator
    # opened fine sky-coloured cracks when neighbouring islands simplified differently.
    ob.data.update()
    attr=ob.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in ob.data.vertices:
        p=v.co;n=v.normal
        grain=noise.noise_vector(Vector((p.x*.40,p.y*.25,p.z*.10)))[0]
        patch=noise.noise_vector(p*.12)[1]
        tone=clamp(.53+grain*.65+patch*.34)
        c=blend(rgb('697686'),rgb('D0C1A8'),tone)
        # A few mineral streaks travel down the planes without a tiled stripe pattern.
        streak=max(0,noise.noise_vector(Vector((p.x*.72,p.z*.045,7.3)))[0]-.3)
        c=tuple(q*(1-streak*.45) for q in c)
        moss=clamp((n.z-.33)*1.5)*clamp(.75+patch)
        if plateau:moss=max(moss,clamp((n.z-.25)*.85))
        c=blend(c,rgb('84976D'),moss*.8)
        attr.data[v.index].color=(*[linear(q) for q in c],1)
    ob.data.materials.append(M['Painted natural albedo'])

nx=241;nz=33;verts=[];faces=[]
for j in range(nz):
    for i in range(nx):verts.append(facepoint(-120+i,j/(nz-1)))
for j in range(nz-1):
    for i in range(nx-1):
        a=j*nx+i;faces.append((a,a+1,a+nx+1,a+nx))
finish(mesh('Ridge • Heileberget granite escarpment',verts,faces,None,True))

ny=20;verts=[];faces=[]
for j in range(ny):
    t=j/(ny-1)
    for i in range(nx):
        x=-120+i;edge=facepoint(x,1);y=edge.y+47*t
        z=crest(x)*(1-t**1.7)+sin(t*pi)*2.1-3*t
        z+=sin(t*pi)*noise.noise_vector(Vector((x*.13,y*.17,4)))[0]*.8
        verts.append((x,y,z))
for j in range(ny-1):
    for i in range(nx-1):
        a=j*nx+i;faces.append((a,a+1,a+nx+1,a+nx))
finish(mesh('Ridge • rounded high plateau',verts,faces,None,True),True)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('GRANITE_PLANES_READY',2*(nx-1)*(nz-1+ny-1),'triangles in ridge and plateau')
