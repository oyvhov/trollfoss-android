"""Second sculpt pass: natural granite, continuous colour, full birches and atmosphere."""
import bpy,math,random,ast,pathlib
from math import sin,cos,pi,exp,sqrt
from mathutils import Vector,noise
random.seed(408)
source=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale/art/heileberget/build_scene.py').read_text(encoding='utf-8')
tree=ast.parse(source)
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.context.scene;OUT='C:/topa/dist/heileberget-blender'
OUTSIDE=bpy.data.collections['Heileberget • landscape'];CAVE=bpy.data.collections['Inside the mountain']
TROLL=bpy.data.collections['Rumle • sculpt'];ARM=bpy.data.collections['Rumle • waving hand'];STUDIO=bpy.data.collections['Cameras & light']
current=OUTSIDE
M=bpy.data.materials
granite=[M['Granite %02d'%i] for i in range(5)];grass=[M['Heather ground %02d'%i] for i in range(4)]
moss=[M['Moss %02d'%i] for i in range(5)]
wood=M['Weathered timber'];woodLight=M['Cut timber'];bark=M['Birch bark'];barkDark=M['Birch scars'];pathmat=M['Ochre footpath']
skin=M['Warm granite skin'];skinLight=M['Lids and lips'];rose=M['Nose • soft rose'];cream=M['Vanilla cream'];berry=M['Berries'];gold=M['Old gold'];gem=M['Icy diamonds'];iron=M['Forged iron'];lantern=M['Lantern glass']
for o in list(OUTSIDE.objects):
    if o.name.startswith(('Heileberget • striated ridge','Mossy upper ledge','Cave roof moss')):bpy.data.objects.remove(o,do_unlink=True)
# Colour attributes remove the square material patches on the height field.
vertexmat=mat('Painted natural albedo','FFFFFF')
nodes=vertexmat.node_tree.nodes;p=next(n for n in nodes if n.type=='BSDF_PRINCIPLED')
attr=nodes.new('ShaderNodeVertexColor');attr.layer_name='Albedo';vertexmat.node_tree.links.new(attr.outputs['Color'],p.inputs['Base Color'])
def colors(o,fn):
    attr=o.data.color_attributes.get('Albedo') or o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in o.data.vertices:
        col=fn(o.matrix_world@v.co,v.normal)
        attr.data[v.index].color=(*[linear(max(0,min(1,c))) for c in col],1)
    o.data.materials.clear();o.data.materials.append(vertexmat)
    for f in o.data.polygons:f.material_index=0
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def clamp(v):return max(0,min(1,v))
def terraincolor(p,normal):
    x,y,z=p;v=noise.noise_vector(Vector((x*.18,y*.18,2)))[0]
    g=blend(rgb('607848'),rgb('9A9B50'),clamp(.5+v*1.6))
    r=blend(rgb('8A9197'),rgb('B7B5A6'),clamp(.5+noise.noise_vector(p*.3)[0]*1.5))
    stone=clamp((abs(noise.noise_vector(Vector((x*.16,y*.16,1)))[0])-.24)*6)
    return tuple(v*(.96+noise.noise_vector(p*3)[0]*.08) for v in blend(g,r,stone))
o=bpy.data.objects['Continuous walkable highland'];colors(o,terraincolor)
def rockcolor(p,normal):
    v=noise.noise_vector(p*.38)[0];a=blend(rgb('8F9297'),rgb('BAB7AA'),clamp(.5+v*1.7))
    striation=.97+.025*sin(p.x*7+p.z*.8+sin(p.z*.9))
    return tuple(c*striation for c in a)
# Granite meshes are polished, with broad eroded planes and restrained variation.
for o in list(OUTSIDE.objects):
    if o.type=='MESH' and o.name.startswith(('Tarn shoreline','Heather outcrop','Cave arch','Cave jamb','Lookout cairn')):
        s=o.modifiers.new('Rounded weathering','SUBSURF');s.levels=1;apply(o,s)
        for v in o.data.vertices:
            q=o.matrix_world@v.co;v.co+=v.normal*noise.noise_vector(q*2.7)[0]*.07
        for p in o.data.polygons:p.use_smooth=True
        colors(o,rockcolor)
# One continuous rock face, with vertical seams, broad ledges and a softly rolling crest.
verts=[];faces=[];nx=161;nz=33
def crest(x):return 17+2.8*sin(x*.085)+1.2*sin(x*.26)+.7*sin(x*.49)
def front(x):return 24+1.3*sin(x*.15)+.45*sin(x*.64)
for j in range(nz):
    t=j/(nz-1)
    for i in range(nx):
        x=-50+i*.625;z=3+(crest(x)-3)*t
        fissure=(abs(sin(x*1.4+sin(z*.16)*.4))**18)*.5
        ledge=.6*sin(t*pi*3)**5
        y=front(x)+t*1.8+fissure+ledge+noise.noise_vector(Vector((x*.6,t*4,3)))[0]*.28
        verts.append((x,y,z))
for j in range(nz-1):
    for i in range(nx-1):a=j*nx+i;faces.append((a,a+nx,a+nx+1,a+1))
o=mesh('Heileberget • continuous granite strata',verts,faces,granite[0],True);colors(o,rockcolor)
verts=[];faces=[];ny=21
for j in range(ny):
    t=j/(ny-1)
    for i in range(nx):
        x=-50+i*.625;y=front(x)+1.8+t*35;z=crest(x)-sin(t*pi/2)*7+sin(t*pi)*2
        verts.append((x,y,z))
for j in range(ny-1):
    for i in range(nx-1):a=j*nx+i;faces.append((a,a+1,a+nx+1,a+nx))
o=mesh('Heileberget • heather plateau',verts,faces,grass[0],True);colors(o,terraincolor)
# Distant mountain ranges wrap around the horizon; lower on the fjord side.
for layer in range(3):
    verts=[];faces=[];radius=75+layer*28
    for i in range(193):
        a=i*2*pi/192;x=cos(a)*radius;y=sin(a)*radius
        h=7+sin(a*3+layer)*5+sin(a*11+1)*2+noise.noise_vector(Vector((a*2,layer,1)))[0]*5
        verts.extend([(x,y,-9),(x,y,h)])
    for i in range(192):faces.append((i*2,i*2+1,i*2+3,i*2+2))
    mesh('Distant blue mountains %d'%layer,verts,faces,mat('Haze %d'%layer,['6B8B9A','8DA6B1','ADBFC7'][layer]),True)
# Sky is exported too: a gradient hemisphere without reliance on a flat clear colour.
SKY=bpy.data.collections.get('Sky • painted atmosphere') or collection('Sky • painted atmosphere');current=SKY
verts=[];faces=[];ns=96;nr=16
for j in range(nr+1):
    e=-.12+j*(pi/2+.12)/nr
    for i in range(ns+1):a=i*2*pi/ns;verts.append((cos(a)*180*cos(e),sin(a)*180*cos(e),180*sin(e)))
for j in range(nr):
    for i in range(ns):a=j*(ns+1)+i;faces.append((a,a+1,a+ns+2,a+ns+1))
o=mesh('Sky dome',verts,faces,None,True)
colors(o,lambda p,n:blend(rgb('DDE4DB'),rgb('73AED0'),clamp(p.z/130)))
o['unlit']=True
# Long soft clouds are hand-shaped continuous volumes, not repeated ball chains.
cloud=mat('Cloud cream','EDF0E5')
for k in range(8):
    a=.2+k*.76;cx=cos(a)*100;cy=sin(a)*100;cz=32+sin(k*2)*8
    verts=[];faces=[]
    for j in range(9):
        v=-pi/2+j*pi/8
        for i in range(33):
            u=i*2*pi/32;r=1+.15*sin(u*5+k)+.1*sin(u*9)
            verts.append((cx+cos(v)*cos(u)*r*(12+k%3*4),cy+cos(v)*sin(u)*r*3.5,cz+sin(v)*2.2))
    for j in range(8):
        for i in range(32):a0=j*33+i;faces.append((a0,a0+1,a0+34,a0+33))
    o=mesh('Soft cloud %d'%k,verts,faces,cloud,True);o['unlit']=True
current=OUTSIDE
# Full, layered birch crowns in one batched mesh. The visible branches remain.
for o in list(OUTSIDE.objects):
    if o.name.startswith('Birch • leaf'):bpy.data.objects.remove(o,do_unlink=True)
leafverts=[];leaffaces=[];leafcols=[]
def addleaf(a,b,w,col,vs=leafverts,fs=leaffaces,cs=leafcols):
    a=Vector(a);b=Vector(b);d=b-a;u=d.cross(Vector((0,0,1)))
    if u.length<.001:u=Vector((1,0,0))
    u.normalize();u*=w;m=(a+b)*.5;idx=len(vs)
    vs.extend([a,m+u,b,m-u,m+Vector((0,0,w*.28))]);fs.extend([(idx,idx+1,idx+4),(idx+1,idx+2,idx+4),(idx+2,idx+3,idx+4),(idx+3,idx,idx+4)]);cs.extend([col]*5)
for trunk in [o for o in OUTSIDE.objects if o.name.startswith('Birch • pale trunk')]:
    coords=[trunk.matrix_world@v.co for v in trunk.data.vertices];top=max(coords,key=lambda p:p.z);base=min(p.z for p in coords);s=(top.z-base)/3.7
    for i in range(260):
        a=random.uniform(0,2*pi);r=sqrt(random.random())*1.65*s;z=top.z+random.uniform(-1.5,.35)*s
        p=Vector((top.x+cos(a)*r,top.y+sin(a)*r,z));tip=p+Vector((cos(a)*.25*s,sin(a)*.25*s,-.4*s))
        col=blend(rgb('4F733D'),rgb('B2BA59'),random.uniform(.1,.95));addleaf(p,tip,.18*s,col)
o=mesh('Birch • full leaf canopy',leafverts,leaffaces,None,True)
attr=o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
for i,c in enumerate(leafcols):attr.data[i].color=(*[linear(v) for v in c],1)
o.data.materials.append(vertexmat)
# Grass, tiny heather and moss along the shore; kept to one GPU batch.
vs=[];fs=[];cs=[]
for i in range(7200):
    x=random.uniform(-25,25);y=random.uniform(-24,22);z=H(x,y)
    if z<1.12 or abs(y+1)<1.2 or (x>8 and y>11):continue
    # Keep the principal paths legible.
    if y<-5 and abs(x-(1+sin((y+23)/.6*.21)*3))<.95:continue
    a=random.uniform(0,2*pi);h=random.uniform(.08,.27)
    addleaf((x,y,z+.025),(x+cos(a)*h*.5,y+sin(a)*h*.5,z+h),.025,blend(rgb('7A8A43'),rgb('B4AC5C'),random.random()),vs,fs,cs)
o=mesh('Fine heather grasses',vs,fs,None,True);attr=o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
for i,c in enumerate(cs):attr.data[i].color=(*[linear(v) for v in c],1)
o.data.materials.append(vertexmat)
# Warm cabin beside the valley path, with a sod roof and a chimney.
cube('Trail cabin',(-19,-12,H(-19,-12)+1),(3.6,2.8,2),wood,.08)
for j in range(8):cube('Cabin horizontal cladding',(-19,-13.43,H(-19,-12)+.15+j*.24),(3.7,.12,.19),woodLight,.035)
cube('Cabin door',(-19,-13.51,H(-19,-12)+.8),(.8,.08,1.5),iron,.03)
for x in [-20.1,-17.9]:
    cube('Cabin window frame',(x,-13.55,H(-19,-12)+1.3),(.64,.12,.7),woodLight,.04)
    cube('Cabin warm window',(x,-13.63,H(-19,-12)+1.3),(.5,.04,.55),lantern,.02)
for sign in [-1,1]:
    o=cube('Cabin sod roof',(-19+sign*.95,-12,H(-19,-12)+2.25),(2.2,3.2,.23),moss[2],.12);o.rotation_euler[1]=sign*.32
cube('Cabin chimney',(-19.8,-11.6,H(-19,-12)+2.9),(.4,.45,1.2),granite[1],.06)
# Warm lanterns outside the secret entrance.
def hanging_lantern(x,y,z):
    tube('Bent lantern post',[(x,y,z),(x+.15,y,z+2.3),(x+.6,y,z+2.65),(x+1,y,z+2.5)],[.16,.13,.12,.10],wood,8)
    curve('Lantern hanger',[(x+.9,y,z+2.5),(x+.9,y,z+2.12)],.025,iron)
    cube('Lantern glowing glass',(x+.9,y,z+1.82),(.32,.32,.48),lantern,.035)
    for a in [-1,1]:
        for b in [-1,1]:cube('Lantern iron frame',(x+.9+a*.18,y+b*.18,z+1.82),(.035,.035,.53),iron,.01)
    cube('Lantern cap',(x+.9,y,z+2.1),(.46,.46,.1),iron,.03)
for x,y in [(8.4,11.7),(17.3,12.5),(-12,-7)]:hanging_lantern(x,y,H(x,y))
# Fix the smile so the sculpt's cheeks do not swallow it.
for o in TROLL.objects:
    if o.name.startswith(('Open happy smile','Sculpted smile rim')):
        for v in o.data.vertices:v.co.y=-1.22-.16*(1-min(1,abs(v.co.x)/.85)**2)
    elif o.name.startswith('Little uneven tooth'):o.location.y=-1.37+.12*(abs(o.location.x)/.8)**2
    elif o.name.startswith('Lower lip'):o.location.y=-1.34
# A lush scalp joins the finer fern and moss locks.
current=TROLL
verts=[];faces=[]
for j in range(13):
    t=.02+j*1.53/12
    for i in range(49):
        a=i*pi/24;r=1+.025*sin(a*11+t*9)
        verts.append((1.27*sin(t)*cos(a)*r,-.10+.91*sin(t)*sin(a)*r,4.55+1.13*cos(t)+.025*sin(a*8)))
for j in range(12):
    for i in range(48):a=j*49+i;faces.append((a,a+1,a+50,a+49))
o=mesh('Soft moss scalp',verts,faces,moss[1],True)
for i in range(85):
    a=random.uniform(0,2*pi);t=random.uniform(.2,1.6)
    p=Vector((1.27*sin(t)*cos(a),-.1+.92*sin(t)*sin(a),4.55+1.13*cos(t)))
    tip=p+Vector((cos(a)*.18,sin(a)*.18,-.25))
    leaf('Overlapping moss frond',p,tip,.16,random.choice(moss[1:]))
# Better silhouette and less fish-eyed expression.
for o in TROLL.objects:
    if o.name.startswith(('Hazel iris','Pupil','Eye catchlight')):o.location.z-=.035
current=STUDIO
camera=scene.camera;cam=camera.data
point_camera((24,-39,23),(0,3,5),40)
scene.world.node_tree.nodes['Background'].inputs[0].default_value=(.55,.69,.85,1)
scene.world.node_tree.nodes['Background'].inputs[1].default_value=.65
bpy.data.lights['Evening sun'].energy=3;bpy.data.lights['Evening sun'].color=(1,.87,.68)
scene.view_settings.look='AgX - Medium High Contrast';scene.view_settings.exposure=.4
scene.render.resolution_x=1600;scene.render.resolution_y=1000;scene.cycles.samples=40
scene.render.filepath=OUT+'/landscape-v2.png'
bpy.ops.wm.save_as_mainfile(filepath=OUT+'/Heileberget.blend')
print('REFINED',sum(len(o.data.polygons) for o in bpy.data.objects if o.type=='MESH'),'polygons')
def render_preview():bpy.ops.render.render(write_still=True);return None
bpy.app.timers.register(render_preview,first_interval=1)
