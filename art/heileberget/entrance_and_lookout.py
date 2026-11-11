"""Embed the cave portal in a true rock spur and open a fjord-facing lookout.
Idempotent replacement pass; authored in the separately owned Blender MCP session.
The spur footprint is shared with Highlands.walkable. Blender coordinates are Z up.
"""
import bpy,ast,pathlib,math,random
from math import sin,cos,pi,exp
from mathutils import Vector,noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
OUTSIDE=bpy.data.collections['Heileberget • landscape'];current=OUTSIDE;M=bpy.data.materials
for o in list(OUTSIDE.objects):
    if o.name.startswith(('Entrance spur •','Lookout cairn','Lookout •')) or o.name=='Deep cave shadow':
        bpy.data.objects.remove(o,do_unlink=True)

def clamp(v):return max(0,min(1,v))
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def colour(o,inside=False):
    attr=o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in o.data.vertices:
        p=v.co;n=v.normal
        grain=clamp(.5+.5*noise.noise_vector(p*.33)[0])
        c=blend(rgb('778398'),rgb('B7BAB9'),grain)
        if inside:c=tuple(x*(.67-clamp((p.y-18)/10)*.35) for x in c)
        else:c=blend(c,rgb('7A936F'),clamp((n.z-.35)*1.5)*.7)
        attr.data[v.index].color=(*[linear(x) for x in c],1)
    o.data.materials.append(M['Painted natural albedo'])

# The rocky fan starts behind the existing stone arch and merges into the main
# escarpment. Its back is a full volume rather than a free-standing dark card.
def spur_edge(a,t):
    width=9.3+8*t;height=11.5+7*t
    # An uneven escarpment with broad shoulders; a perfect half-circle looked
    # like a concrete tunnel rather than a cave carved into the mountain.
    c=cos(a);s=sin(a)
    x=13+math.copysign(abs(c)**.72,c)*width
    y=18.2+t*26+(1-t)*(.65*sin(a*3)+.4*sin(a*7))
    z=4.4+s**.62*height
    z+=s**.5*(1.1*sin(a*3+.3)+.55*sin(a*9+t*4))
    return Vector((x,y,z))

steps=64;rows=28;verts=[];faces=[]
for j in range(rows+1):
    t=j/rows
    for i in range(steps+1):
        a=pi*i/steps
        verts.append(spur_edge(a,t))
for j in range(rows):
    for i in range(steps):
        k=j*(steps+1)+i;faces.append((k,k+steps+1,k+steps+2,k+1))
o=mesh('Entrance spur • continuous granite roof',verts,faces,None,True);colour(o)

# Radial stone surface around the opening, with a short, shaded inner tunnel.
verts=[];faces=[];radial=8
for j in range(radial+1):
    t=j/radial
    for i in range(steps+1):
        a=pi*i/steps
        inner=Vector((13+3.5*cos(a),18.2,6.05+5.25*sin(a)))
        outer=spur_edge(a,0)
        p=inner.lerp(outer,t)
        p.y-=sin(pi*t)*(1.2*sin(a*5+.5)+.45*sin(a*11))
        p.z+=sin(pi*t)*.38*cos(a*9+t*2)
        verts.append(p)
for j in range(radial):
    for i in range(steps):
        k=j*(steps+1)+i;faces.append((k,k+steps+1,k+steps+2,k+1))
o=mesh('Entrance spur • carved stone face',verts,faces,None,True);colour(o)
verts=[];faces=[]
for j in range(9):
    t=j/8
    for i in range(steps+1):
        a=pi*i/steps;verts.append((13+(3.5-.7*t)*cos(a),18.2+9*t,6.05+(5.25-.7*t)*sin(a)))
for j in range(8):
    for i in range(steps):
        k=j*(steps+1)+i;faces.append((k,k+1,k+steps+2,k+steps+1))
o=mesh('Entrance spur • shaded tunnel',verts,faces,None,True);colour(o,True)
verts=[(10.2,27.2,4.4),(15.8,27.2,4.4)]
verts.extend((13+2.8*cos(i*pi/32),27.2,6.05+4.55*sin(i*pi/32)) for i in range(33))
mesh('Entrance spur • dark inner chamber',verts,[tuple(range(len(verts)))],M['Smile interior'],False)

# A low, stable cairn and a softly worn path invite the player onto the west ledge.
granite=[M['Granite %02d'%i] for i in range(5)]
for j in range(5):
    rock('Lookout • cairn',(-21.5,20.3,H(-21.5,20.3)+j*.22),(.72-j*.105,.55-j*.075,.17),1800+j)
verts=[];faces=[]
for j in range(52):
    t=j/51;x=-13-5*t-2*sin(pi*t);y=-1+19*t
    dx=-5-2*pi*cos(pi*t);dy=19;length=math.hypot(dx,dy)
    for side in [-1,1]:
        px=x+side*dy/length*.54;py=y-side*dx/length*.54
        verts.append((px,py,H(px,py)+.025))
for j in range(51):
    k=j*2;faces.append((k,k+1,k+3,k+2))
mesh('Lookout • winding ledge path',verts,faces,M['Ochre footpath'],True)
scene['art_version']='Round 10 • cave embedded in granite and west fjord lookout'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('ENTRANCE_AND_LOOKOUT_READY')
