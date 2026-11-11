"""Cohesion pass after mobile/tablet play tests: sage, lilac granite, honey timber, warm rooms.
Authored only in the dedicated Blender session; world heights and interaction positions stay fixed.
"""
import bpy,ast,pathlib,random,math
from math import sin,cos,pi,exp,sqrt
from mathutils import Vector,noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
scene=bpy.data.scenes['Heileberget'];bpy.context.window.scene=scene
OUTSIDE=bpy.data.collections['Heileberget • landscape'];CAVE=bpy.data.collections['Inside the mountain']
scene.view_layers[0].layer_collection.children[CAVE.name].exclude=False
bpy.context.view_layer.update()
M=bpy.data.materials;vertexmat=M['Painted natural albedo'];random.seed(9001)
def clamp(x):return max(0,min(1,x))
def blend(a,b,t):return tuple(x*(1-t)+y*t for x,y in zip(a,b))
def repaint(name,h):
    m=M.get(name)
    if not m:return
    c=rgb(h);m['game_color']=list(c);m.diffuse_color=(*[linear(v) for v in c],1)
    for p in m.node_tree.nodes:
        if p.type=='BSDF_PRINCIPLED':p.inputs['Base Color'].default_value=m.diffuse_color
def color(o,fn):
    attr=o.data.color_attributes.get('Albedo') or o.data.color_attributes.new(name='Albedo',type='FLOAT_COLOR',domain='POINT')
    for v in o.data.vertices:
        p=o.matrix_basis@v.co;attr.data[v.index].color=(*[linear(clamp(c)) for c in fn(p,v.normal)],1)
    o.data.materials.clear();o.data.materials.append(vertexmat)
    for p in o.data.polygons:p.material_index=0
for name,h in {'Warm granite skin':'B1BAA0','Lids and lips':'C7C9A9','Nose • soft rose':'D9A18E','Warm cheek':'D6A38F','Moss 00':'4B7151','Moss 01':'66895C','Moss 02':'88A363','Moss 03':'AEC075','Moss 04':'C5CD8C','Weathered timber':'A47D5B','Cut timber':'CEAA7C','Ochre footpath':'CEBC91','Birch bark':'EFE8D9','Birch scars':'69685F','Old gold':'E6B857','Icy diamonds':'88D8D9','Armchair • moss velvet':'75977C','Treasury runner':'6C9A9B','Granite 00':'9298AA','Granite 01':'A4ABBC','Granite 02':'B9BBC8','Granite 03':'D1C8BA','Granite 04':'737F93','Cavern warm limestone':'ADB0C0','Vault dusk':'777F99'}.items():repaint(name,h)
def groundpaint(p,n):
    t=clamp(.5+noise.noise_vector(Vector((p.x*.13,p.y*.13,4)))[0]*1.4)
    green=blend(rgb('7A9A70'),rgb('B4BE85'),t)
    rock=clamp((abs(noise.noise_vector(Vector((p.x*.16,p.y*.16,1)))[0])-.30)*4.5)
    return blend(green,rgb('B7B9B9'),rock)
for o in OUTSIDE.objects:
    if o.type!='MESH':continue
    if o.name.startswith(('Continuous walkable','Valley beyond')):color(o,groundpaint)
    elif o.name.startswith(('Heileberget granite massif','Tarn shoreline','Heather outcrop','Cave arch','Cave jamb','Lookout cairn','Diamond socket stone')):
        if o.name.startswith('Heileberget granite massif') and not o.get('harmony_positioned'):
            # Give the long mountain silhouette room against the sky. The walkable slope is unchanged.
            for v in o.data.vertices:v.co.y+=17;v.co.z*=.78
            o['harmony_positioned']=True
        color(o,lambda p,n:blend(rgb('8994AD'),rgb('C2C3CB'),clamp(.55+noise.noise_vector(p*.19)[0]*.65)))
    elif o.name.startswith('Birch • full leaf canopy'):
        # Preserve vertex variation but remove the old yellow-green cast.
        attr=o.data.color_attributes['Albedo']
        for i,entry in enumerate(attr.data):
            p=o.data.vertices[i].co;t=clamp(.5+noise.noise_vector(p*2)[0])
            c=blend(rgb('5E8A67'),rgb('BCCB8B'),t);entry.color=(*[linear(v) for v in c],1)
for o in bpy.data.collections['Sky • painted atmosphere'].objects:
    if o.name=='Sky dome':
        color(o,lambda p,n:blend(rgb('F7E8CF'),rgb('77B9E1'),clamp(p.z/95)))
        # Keep the sky emissive in Blender, as it is unlit on Android.
        o.data.materials.clear();o.data.materials.append(M['Sky gradient emission'])
for o in CAVE.objects:
    if o.type!='MESH':continue
    if o.name.startswith('Cavern • connected floor'):
        color(o,lambda p,n:blend(rgb('B5A99E'),rgb('D4C1A4'),clamp(.5+noise.noise_vector(p*.32)[0]*.55)))
    elif o.name.startswith(('Cavern • sculpted','Hall pillar','Vaulted connecting arch','Side arch foundation')):
        color(o,lambda p,n:blend(rgb('828A9D'),rgb('B2B0BC'),clamp(.5+noise.noise_vector(p*.24)[0]*.65)))
    elif o.name.startswith('Cavern • high vaulted'):
        color(o,lambda p,n:blend(rgb('68758D'),rgb('9399A9'),clamp(.5+noise.noise_vector(p*.22)[0]*.5)))

# Replace the gallery's rectangular fascia with the actual ramp profile.
for o in list(CAVE.objects):
    if o.name.startswith(('Balcony stone fascia','Harmony •')):bpy.data.objects.remove(o,do_unlink=True)
for o in list(OUTSIDE.objects):
    if o.name.startswith('Harmony •'):bpy.data.objects.remove(o,do_unlink=True)
current=CAVE;stone=M['Granite 01'];wood=M['Cut timber'];cream=M['Vanilla cream'];gold=M['Old gold'];gem=M['Icy diamonds']
verts=[];faces=[]
for i in range(37):
    y=-8+i;z=max(0,min(4.4,(8+y)*.23))
    verts.extend([(3.1,y,0),(3.1,y,z),(3.5,y,z),(3.5,y,0)])
for i in range(36):
    a=i*4;faces.extend([(a,a+4,a+5,a+1),(a+1,a+5,a+6,a+2),(a+2,a+6,a+7,a+3)])
mesh('Harmony • gallery support follows slope',verts,faces,stone,False)

# A family of woven motifs runs through the rug, cushions and the child's flower badge.
plum=mat('Harmony • plum wool','A77A94');honey=mat('Harmony • honey weave','DEBD80');mint=mat('Harmony • sage wool','8FAC93')
for x in [-4.4,-3.7,-3,-2.3,-1.6]:
    cube('Harmony • rug woven dash',(x,-1.2,.076),(.31,.07,.02),honey,.01)
for x,y in [(-3,-1.1),(-13,23)]:
    for i in range(8):
        a=i*pi/4
        o=cube('Harmony • rug rosette',(x+cos(a)*.43,y+sin(a)*.43,.081),(.18,.33,.015),honey,.025);o.rotation_euler[2]=-a
for x,y in [(-7,1),(-10,20)]:
    ell('Harmony • floor cushion',(x,y,.20),(.75,.62,.21),plum,20,12)
    ell('Harmony • cushion button',(x,y,.415),(.06,.06,.018),honey,12,8)
    for a in [0,pi/2,pi,pi*1.5]:curve('Harmony • cushion seam',[(x+cos(a)*.1,y+sin(a)*.1,.413),(x+cos(a)*.52,y+sin(a)*.52,.34)],.008,honey)
# The hearth is a usable-looking home, with cups, a kettle and a stack of books.
iron=M['Forged iron'];teapot=mat('Harmony • teal enamel','6B9A9B')
ell('Harmony • kettle',(-17,5.8,1.04),(.47,.37,.44),teapot,24,16)
tube('Harmony • kettle spout',[(-16.65,5.8,1),(-16.35,5.8,1.13),(-16.25,5.8,1.35)],[.15,.12,.09],teapot,10)
curve('Harmony • kettle handle',[(-17.35,5.8,1.17),(-17.3,5.8,1.65),(-17,5.8,1.78),(-16.7,5.8,1.65),(-16.65,5.8,1.17)],.045,iron,10)
for i,x in enumerate([-18.3,-17.4,-16.5]):
    tube('Harmony • hearth mug',[(x,5.7,3.37),(x,5.7,3.67)],[.15,.17],plum if i%2 else teapot,16)
    curve('Harmony • mug handle',[(x+.15,5.7,3.61),(x+.27,5.7,3.63),(x+.29,5.7,3.45),(x+.14,5.7,3.43)],.024,cream,8)
for i in range(4):
    o=cube('Harmony • story book',(-11.8,2.5,.13+i*.16),(.66,.9,.14),[plum,teapot,honey,mint][i],.035);o.rotation_euler[2]=i*.14
    cube('Harmony • book pages',(-11.8,2.05,.13+i*.16),(.54,.018,.08),cream,.01)
# Hanging root garlands and tiny crystal clusters repeat the woodland curves underground.
for x in [-18,-11,2,11,18]:
    curve('Harmony • root garland',[(x-3,14,10),(x-1.5,13.7,9.1),(x,13.6,8.7),(x+1.5,13.7,9.1),(x+3,14,10)],.045,M['Weathered timber'],8)
    for i in range(8):
        px=x-2.3+i*.65;z=8.8+abs(px-x)*.35
        leaf('Harmony • root leaf',(px,13.6,z),(px+.22,13.5,z-.44),.16,mint)
for x,y in [(-16,-7),(-18,12),(18,7),(1,26)]:
    z=max(0,min(4.4,(8+y)*.23)) if x>3 else 0
    for i in range(4):tube('Harmony • small crystal',[(x+sin(i)*.3,y+cos(i)*.3,z),(x+sin(i)*.3,y+cos(i)*.3,z+.35),(x+sin(i)*.3+.06,y+cos(i)*.3,z+.5)],[.09,.1,0],gem,5)

current=OUTSIDE
flower=mat('Harmony • lilac petals','B59CC7');leafmat=mat('Harmony • meadow leaf','73956E')
for bx,by in [(-16,-9),(-13,-5),(9,-7),(17,4),(-7,8),(9,9),(-3,-18),(5,-14)]:
    for i in range(7):
        x=bx+random.uniform(-.8,.8);y=by+random.uniform(-.6,.6);z=H(x,y)
        if z<1.1:continue
        h=random.uniform(.18,.38);tube('Harmony • flower stem',[(x,y,z),(x+.03,y,z+h)],[.015,.009],leafmat,5)
        for k in range(5):
            a=k*2*pi/5;ell('Harmony • meadow petal',(x+cos(a)*.065,y+sin(a)*.065,z+h),(.06,.045,.018),flower if i%2 else cream,8,4)
        ell('Harmony • flower heart',(x,y,z+h+.018),(.028,.028,.02),honey,8,4)
# A couple of quiet woodland neighbours, in the same rounded style as the children.
fur=mat('Harmony • hare fur','C8B8A4');nose=mat('Harmony • little noses','9C7A7A')
for x,y,angle in [(-15,-6,.4),(17,5,-.6)]:
    z=H(x,y)
    ell('Harmony • hare body',(x,y,z+.3),(.28,.42,.31),fur,20,12)
    ell('Harmony • hare head',(x,y-.31,z+.57),(.22,.2,.23),fur,20,12)
    for side in [-1,1]:
        o=ell('Harmony • hare ear',(x+side*.10,y-.28,z+.9),(.062,.07,.28),fur,16,10);o.rotation_euler[1]=side*.16
        ell('Harmony • hare eye',(x+side*.10,y-.49,z+.61),(.026,.014,.035),iron,12,8)
    ell('Harmony • hare nose',(x,y-.516,z+.53),(.04,.02,.026),nose,12,8)
    ell('Harmony • hare tail',(x,y+.36,z+.3),(.13,.13,.14),cream,16,10)

# Portraits share the evening's gentle material response.
scene['art_version']='Harmony pass • sage meadow, lilac granite, honey timber, warm cavern'
scene.view_layers[0].layer_collection.children[CAVE.name].exclude=True
scene.camera=bpy.data.objects['Landscape camera'];scene.camera.location=(10,-34,9)
scene.camera.rotation_euler=(Vector((0,5,5))-scene.camera.location).to_track_quat('-Z','Y').to_euler();scene.camera.data.lens=30
scene.render.resolution_x=1600;scene.render.resolution_y=1000;scene.render.resolution_percentage=100
scene.render.filepath='C:/topa/dist/heileberget-blender/landscape-harmony.png'
for a in bpy.context.screen.areas:
    if a.type=='VIEW_3D':a.spaces.active.region_3d.view_perspective='CAMERA';a.spaces.active.shading.type='MATERIAL'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
def preview():bpy.ops.render.render(write_still=True);return None
bpy.app.timers.register(preview,first_interval=1)
print('HARMONY_PASS_READY')
