"""Granite massifs, lake basin, horizon and material polish for mobile export."""
import bpy,math,random,ast,pathlib
from math import sin,cos,pi,exp,sqrt
from mathutils import Vector,noise
source=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale/art/heileberget/build_scene.py').read_text(encoding='utf-8')
exec(compile(ast.Module(body=[n for n in ast.parse(source).body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.context.scene;OUT='C:/topa/dist/heileberget-blender';random.seed(125)
OUTSIDE=bpy.data.collections['Heileberget • landscape'];CAVE=bpy.data.collections['Inside the mountain'];STUDIO=bpy.data.collections['Cameras & light'];current=OUTSIDE
M=bpy.data.materials;granite=[M['Granite %02d'%i] for i in range(5)];moss=[M['Moss %02d'%i] for i in range(5)];grass=[M['Heather ground %02d'%i] for i in range(4)];gem=M['Icy diamonds'];gold=M['Old gold'];wood=M['Weathered timber'];cream=M['Vanilla cream']
vertexmat=M['Painted natural albedo']
def clamp(v):return max(0,min(1,v))
def mix(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def colourmesh(o,fn):
    attr=o.data.color_attributes.get('Albedo') or o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in o.data.vertices:attr.data[v.index].color=(*[linear(c) for c in fn(o.matrix_world@v.co,v.normal)],1)
    o.data.materials.clear();o.data.materials.append(vertexmat)
    for p in o.data.polygons:p.material_index=0
# The original lake was too small; deepen its bowl, retaining the island's centre height.
def dh(x,y):
    lake=((x+3)/10)**2+((y+1)/8)**2
    return 1.7*(exp(-((x+3)**2+(y+1)**2)*.11)-max(0,1-lake))
for o in OUTSIDE.objects:
    if o.type!='MESH' or o.name.startswith(('Bridge','Rope','Carved post')):continue
    inv=o.matrix_world.inverted()
    for v in o.data.vertices:
        p=o.matrix_world@v.co
        if -14<p.x<8 and -10<p.y<8:p.z+=dh(p.x,p.y);v.co=inv@p
# Remove the broad, thin wall and build thick, layered cliff masses with real depth.
for o in list(OUTSIDE.objects):
    if o.name.startswith(('Heileberget • continuous','Heileberget • heather plateau')):bpy.data.objects.remove(o,do_unlink=True)
def cliff(name,center,scale,seed):
    verts=[];faces=[];steps=15
    # Six welded rounded cuboid faces; broad planes survive the sculpt noise.
    index={}
    def vert(p):
        key=tuple(round(v,5) for v in p)
        if key in index:return index[key]
        q=Vector(p);rounded=q.lerp(q.normalized(),.23)
        rough=noise.noise_vector(q*2.1+Vector((seed,3,1)))[0]
        rounded*=1+rough*.13
        rounded.x+=.045*sin(q.z*3+seed);rounded.y+=.035*sin(q.x*11+q.z*.8)
        p=Vector(center)+Vector((rounded.x*scale[0],rounded.y*scale[1],rounded.z*scale[2]))
        idx=len(verts);index[key]=idx;verts.append(p);return idx
    for axis in range(3):
        for sign in [-1,1]:
            grid=[]
            for j in range(steps+1):
                for i in range(steps+1):
                    p=[0,0,0];p[axis]=sign;p[(axis+1)%3]=-1+2*i/steps;p[(axis+2)%3]=-1+2*j/steps;grid.append(vert(p))
            for j in range(steps):
                for i in range(steps):
                    a=j*(steps+1)+i;f=(grid[a],grid[a+1],grid[a+steps+2],grid[a+steps+1]);faces.append(f if sign>0 else f[::-1])
    o=mesh(name,verts,faces,None,True)
    def color(p,n):
        a=mix(rgb('858B94'),rgb('B7AD97'),clamp(.5+noise.noise_vector(p*.4)[0]*1.8))
        fine=.96+noise.noise_vector(p*3)[0]*.08
        if n.z>.5:a=mix(a,rgb('778646'),clamp((n.z-.5)*1.2))
        return tuple(clamp(v*fine) for v in a)
    colourmesh(o,color);return o
for i,(p,s) in enumerate([
 ((-22,28,9),(10,6,8)),((-9,29,11),(8,7,10)),((5,31,12),(9,8,11)),((21,29,10),(10,7,9)),
 ((-15,24,5),(8,3,4)),((0,25,6),(7,4,4.5)),((25,23,5),(6,4,4)),
 ((-9,32,19),(7,7,3.2)),((7,35,21),(9,8,3.8)),((23,32,18),(8,7,3))]):cliff('Heileberget granite massif %02d'%i,p,s,2+i*.71)
# A wide skirt hides the edge of the playable height field and gives the fjord real depth.
verts=[];faces=[]
for j in range(81):
    y=-65+j*1.5
    for i in range(81):
        x=-60+i*1.5;edge=max(abs(x)-24,abs(y+2)-24,0)
        z=H(max(-26,min(26,x)),max(-26,min(26,y)))-edge*.22+sin(x*.12)*sin(y*.09)*min(edge*.04,1.2)-.08
        verts.append((x,y,z))
for j in range(80):
    for i in range(80):
        x=-60+(i+.5)*1.5;y=-65+(j+.5)*1.5
        if -25<x<25 and -25<y<25:continue
        a=j*81+i;faces.append((a,a+1,a+82,a+81))
o=mesh('Valley beyond the paths',verts,faces,None,True)
colourmesh(o,lambda p,n:mix(rgb('546D50'),rgb('8E965A'),clamp(.5+noise.noise_vector(p*.12)[0])))
# Native sky colours are also used for Blender's backdrop; render without AgX desaturation.
scene.view_settings.view_transform='Standard';scene.view_settings.look='None';scene.view_settings.exposure=0
bpy.data.lights['Evening sun'].energy=2.1;bpy.data.lights['Evening sun'].color=(1,.88,.73)
scene.world.node_tree.nodes['Background'].inputs[1].default_value=.5
# Gem puzzle stone and a tiny pan of gold are visible at the actual interaction locations.
cliff('Diamond socket stone',(-9,-8,H(-9,-8)+.7),(1.1,.45,1.1),31)
tube('Diamond socket recess',[(-9,-8.5,H(-9,-8)+.9),(-9,-8.52,H(-9,-8)+1.5)],[.22,0],gem,6)
ell('Gold washing pan',(-12,-3,H(-12,-3)+.1),(.7,.7,.12),M['Forged iron'],20,10)
for i in range(5):ell('Gold in pan',(-12+sin(i*2)*.3,-3+cos(i*2)*.3,H(-12,-3)+.23),(.14,.12,.12),gold,10,6)
# Short steps and a clear opening invite the player into the mountain.
for i in range(4):
    y=10+i*.65;cube('Cave threshold stone',(13,y,H(13,y)+.02),(3.7,.61,.12),granite[2],.08)
OUTSIDE.hide_viewport=False;OUTSIDE.hide_render=False;CAVE.hide_viewport=True;CAVE.hide_render=True
bpy.data.collections['Tarn • water'].hide_render=False;bpy.data.collections['Tarn • water'].hide_viewport=False
scene.camera=bpy.data.objects['Landscape camera'];scene.camera.location=(16,-34,13);scene.camera.data.lens=32
scene.camera.rotation_euler=(Vector((0,6,5))-scene.camera.location).to_track_quat('-Z','Y').to_euler()
scene.render.filepath=OUT+'/landscape-final.png';scene.cycles.samples=48
bpy.ops.wm.save_as_mainfile(filepath=OUT+'/Heileberget.blend')
print('LANDSCAPE_FINAL')
def render_preview():bpy.ops.render.render(write_still=True);return None
bpy.app.timers.register(render_preview,first_interval=1)
