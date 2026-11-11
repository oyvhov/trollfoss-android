"""A spacious, furnished cavern: hearth, larder balcony, and crystal treasury."""
import bpy,math,random,ast,pathlib
from math import sin,cos,pi,exp,sqrt
from mathutils import Vector,noise
source=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale/art/heileberget/build_scene.py').read_text(encoding='utf-8')
exec(compile(ast.Module(body=[n for n in ast.parse(source).body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'helpers','exec'))
random.seed(94);scene=bpy.context.scene;OUT='C:/topa/dist/heileberget-blender'
CAVE=bpy.data.collections['Inside the mountain'];OUTSIDE=bpy.data.collections['Heileberget • landscape'];STUDIO=bpy.data.collections['Cameras & light'];current=CAVE
TROLL=bpy.data.collections['Rumle • sculpt'];ARM=bpy.data.collections['Rumle • waving hand'];M=bpy.data.materials
granite=[M['Granite %02d'%i] for i in range(5)];moss=[M['Moss %02d'%i] for i in range(5)]
wood=M['Weathered timber'];woodLight=M['Cut timber'];iron=M['Forged iron'];gold=M['Old gold'];gem=M['Icy diamonds'];cream=M['Vanilla cream'];berry=M['Berries'];cake=M['Chocolate sponge'];lantern=M['Lantern glass']
sodaBlack=M['Pepsi bottle'];sodaGreen=M['Urge bottle'];labelBlue=M['Pepsi label blue'];labelRed=M['Pepsi label red'];labelGreen=M['Urge yellow label']
for o in list(CAVE.objects):
    if o.name.startswith(('Cavern fluted wall','Cavern ledge','Cave walkable floor','Stalactite')):bpy.data.objects.remove(o,do_unlink=True)
def floor(x,y):return max(0,min(4.4,(8+y)*.23)) if x>3 else 0
# Extended floor matches the native Kotlin height and movement contract exactly.
verts=[];faces=[];nx=81;ny=85
for j in range(ny):
    y=-14+j*.5
    for i in range(nx):x=-20+i*.5;verts.append((x,y,floor(x,y)))
for j in range(ny-1):
    for i in range(nx-1):a=j*nx+i;faces.append((a,a+1,a+nx+1,a+nx))
mesh('Cavern • connected floor',verts,faces,M['Granite 02'],True)
wallmat=mat('Cavern warm limestone','8B8175');ceiling=mat('Vault dusk','6A6C70')
# Continuous walls are sculpted with shallow horizontal shelves and vertical folds.
verts=[];faces=[];segments=180;levels=25
for j in range(levels):
    z=j*.62
    for i in range(segments+1):
        a=2*pi*i/segments;dx=cos(a);dy=sin(a)
        r=min(21.5/max(.001,abs(dx)),22.5/max(.001,abs(dy)))
        ripple=.23*sin(a*31+z*.4)+.13*sin(a*67-z*.7)+.25*sin(z*1.8)
        verts.append(((r+ripple)*dx,7+(r+ripple)*dy,z))
for j in range(levels-1):
    for i in range(segments):
        # Open entry portal at the front; returning outside is at (0,-12).
        a=2*pi*(i+.5)/segments
        if sin(a)<-.96 and j<10:continue
        k=j*(segments+1)+i;faces.append((k,k+1,k+segments+2,k+segments+1))
mesh('Cavern • sculpted continuous walls',verts,faces,wallmat,True)
verts=[];faces=[]
for j in range(43):
    y=-14+j
    for i in range(43):
        x=-21+i;z=13+3*cos(x*pi/43)+.3*sin(y*.5)+noise.noise_vector(Vector((x*.2,y*.2,4)))[0]*.6
        verts.append((x,y,z))
for j in range(42):
    for i in range(42):a=j*43+i;faces.append((a,a+43,a+44,a+1))
mesh('Cavern • high vaulted roof',verts,faces,ceiling,True)
# Stone arches connect, rather than close off, three spacious rooms.
for x in [-8,6]:
    z=floor(x,15)
    tube('Hall pillar',[(x,15,z),(x+.15,15.1,z+3),(x-.1,15,z+6),(x,15,z+9)],[1.35,1.05,1.1,1.3],wallmat,12)
for cx,r in [(-14,5),(-1,6),(13,6)]:
    z=floor(cx,15)
    points=[(cx+cos(t)*r,15,z+5+sin(t)*5) for t in [i*pi/24 for i in range(25)]]
    curve('Vaulted connecting arch',points,.65,wallmat,10)
    for i in range(9):
        t=i*pi/8;p=Vector((cx+cos(t)*r,14.7,z+5+sin(t)*5))
        curve('Arch masonry seam',[p+Vector((-.25*cos(t),0,-.25*sin(t))),p+Vector((.45*cos(t),0,.45*sin(t)))],.035,granite[4],4)
# Upper gallery follows the rising floor; broad entry at the foot of the ramp.
for i in range(12):
    y=-5+i*2.6;z=floor(3.5,y)
    tube('Balcony timber post',[(3.55,y,z),(3.55,y,z+1)],[.1,.085],wood,8)
    if i<11:
        nz=floor(3.5,y+2.6)
        curve('Balcony handrail',[(3.55,y,z+.95),(3.55,y+2.6,nz+.95)],.09,woodLight,8)
        curve('Balcony middle rail',[(3.55,y,z+.48),(3.55,y+2.6,nz+.48)],.05,wood,6)
# Large patched armchair behind Rumle, with a hand-stitched wool blanket.
fabric=mat('Armchair • moss velvet','5E7357');thread=mat('Blanket • ochre','D2AC61');rug=mat('Treasury runner','507F83')
cube('Rumle armchair back',(-3,6.1,2.4),(4.7,.75,4.4),fabric,.35)
cube('Rumle chair seat',(-3,5.2,.45),(4.8,3.2,.9),fabric,.3)
for x in [-5.25,-.75]:cube('Rumle armrest',(x,5.5,1.2),(.65,3.1,1.55),fabric,.24)
for i in range(7):cube('Blanket woven stripe',(-4.5+i*.42,6.55,3.6),(.07,.03,1.6),thread,.012)
# A large hearth built into the left wall, with mugs, stacked logs, and a kettle.
for dx in [-1.65,1.65]:cube('Hearth side',(-17+dx,6,1.5),(1,1.5,3),granite[1],.18)
cube('Hearth lintel',(-17,6,3),(4.4,1.8,.7),granite[2],.2)
cube('Hearth dark recess',(-17,6.7,1.3),(2.5,.2,2.4),M['Smile interior'],.1)
cube('Hearth slab',(-17,5.8,.2),(4.6,2.2,.4),granite[3],.12)
for i in range(6):
    a=i*pi/3;tube('Firewood',[(-17+cos(a)*.7,5.7+sin(a)*.6,.45),(-17-cos(a)*.7,5.7-sin(a)*.6,.45)],[.15,.15],wood,9)
flame=mat('Soft amber flame','FFAD48',.4,0,2.5)
for i in range(5):
    x=-17+sin(i*2)*.5;y=5.7+cos(i*2)*.3
    tube('Hearth flame',[(x,y,.5),(x+.06,y,.9),(x-.1,y,1.3+(i%2)*.25)],[.20,.18,0],flame,8)
for i in range(8):tube('Stacked dry log',[(-19,3.5+(i%4)*.35,.3+i//4*.3),(-17.8,3.5+(i%4)*.35,.3+i//4*.3)],[.14,.14],wood,10)
# Rear larder: shelves reach above the people, with readable groups of food.
for x in [8.5,14.5]:
    y=25;z0=4.4
    for j in range(4):
        z=z0+.6+j*1.15;cube('Great larder shelf',(x,y,z),(5,1.5,.16),woodLight,.05)
        for k in range(8):
            xx=x-2+k*.55
            if j<2:bottle(xx,y-.25,z+.09,x>10,.85)
            elif j==2 and k%2==0:gateau(xx,y-.2,z+.09,.6)
            elif j==3:
                tube('Larder candy jar',[(xx,y-.15,z+.09),(xx,y-.15,z+.65)],[.22,.22],[labelRed,labelGreen,gem][k%3],10)
                tube('Candy jar lid',[(xx,y-.15,z+.65),(xx,y-.15,z+.72)],[.25,.25],cream,10)
    for dx in [-2.5,2.5]:cube('Pantry upright',(x+dx,y,z0+2.4),(.17,1.6,5.2),wood,.04)
for x,y in [(11,20),(15,20)]:
    z=floor(x,y);cube('Soda picking crate',(x,y,z+.4),(1.6,1.3,.8),wood,.06)
    for i in range(4):bottle(x-.5+(i%2)*.9,y-.35+(i//2)*.6,z+.81,x>12,1)
# Flour sacks, a wheelbarrow, and barrels make the larder lived-in.
sack=mat('Flour sack linen','CEBE98')
for x,y in [(18,17),(18,19),(7,21)]:
    z=floor(x,y);ell('Flour sack',(x,y,z+.55),(.48,.4,.62),sack,16,12)
    curve('Sack drawstring',[(x-.22,y,z+1.05),(x,y-.05,z+1.1),(x+.2,y,z+1.02)],.035,wood,5)
for x,y in [(19,23),(18,12),(-18,19)]:
    z=floor(x,y);tube('Storage barrel',[(x,y,z),(x,y,z+.2),(x,y,z+1.1),(x,y,z+1.3)],[.48,.59,.59,.48],wood,14)
    for h in [.2,1.1]:tube('Barrel iron band',[(x,y,z+h),(x,y,z+h+.09)],[.60,.60],iron,14)
# Treasury: a generous carpet, opened chest, gold banks and crystals in the stone.
cube('Treasury rug',(-13,23,.035),(9,7,.035),rug,.05)
cube('Treasure chest',(-13,25,.65),(3,1.8,1.25),wood,.12)
lid=cube('Open treasure lid',(-13,25.7,1.8),(3.1,.25,1.7),wood,.14);lid.rotation_euler[0]=-.25
for x in [-14.15,-11.85]:cube('Chest gold straps',(x,24.05,.68),(.17,.08,1.25),gold,.03)
for i in range(85):
    x=-13+random.uniform(-3.8,3.8);y=23+random.uniform(-2.5,2.5);h=max(0,1-abs(x+13)/4-abs(y-23)/3)
    o=cube('Treasury gold ingot',(x,y,.15+h*.65),(.5,.32,.23),gold,.055);o.rotation_euler[2]=random.uniform(0,pi)
for i in range(32):
    x=-19+random.random()*8;y=25+random.random()*2;h=random.uniform(.6,2.4)
    tube('Treasury crystal',[(x,y,.1),(x,y,h*.7),(x+.1,y,h)],[.20,.28,0],gem,6)
# Small niches and sleeping mushrooms reward exploring the edges.
cap=mat('Mushroom caps','AC6557')
for i in range(30):
    side=-1 if i%2 else 1;x=side*random.uniform(18.2,19.7);y=random.uniform(-9,26);z=floor(x,y);h=random.uniform(.15,.5)
    tube('Mushroom stalk',[(x,y,z),(x,y,z+h)],[.055,.045],cream,7)
    ell('Mushroom cap',(x,y,z+h),(.24,.24,.11),cap,12,8)
# Lanterns follow the route, including the ramp, with copper brackets.
for i,(x,y) in enumerate([(-10,-7),(7,-7),(-11,6),(10,10),(-9,15),(7,15),(-17,24),(17,24),(2,24)]):
    z=floor(x,y)+2.2
    tube('Lantern standard',[(x,y,floor(x,y)),(x,y,z+.6)],[.075,.065],iron,7)
    cube('Lantern pane',(x,y,z),(.36,.36,.55),lantern,.035)
    for dx in [-.21,.21]:
        for dy in [-.21,.21]:cube('Lantern frame',(x+dx,y+dy,z),(.035,.035,.62),iron,.01)
    cube('Lantern roof',(x,y,z+.36),(.54,.54,.13),iron,.05)
    d=bpy.data.lights.new('Cave warm pool %d'%i,'POINT');d.energy=180;d.color=(1,.68,.35);d.shadow_soft_size=1
    ob=bpy.data.objects.new(d.name,d);CAVE.objects.link(ob);ob.location=(x,y,z+.2)
d=bpy.data.lights.new('Hearth fire glow','AREA');d.energy=350;d.color=(1,.5,.18);d.size=3
ob=bpy.data.objects.new(d.name,d);CAVE.objects.link(ob);ob.location=(-17,4,2);ob.rotation_euler=(Vector((-3,0,2))-ob.location).to_track_quat('-Z','Y').to_euler()
for name,p,col,power in [('Cave ceiling bounce',(0,3,12),(1,.81,.59),1900),('Pantry bounce',(12,20,12),(1,.8,.52),1500),('Treasury glow',(-13,24,8),(.39,.76,1),1000)]:
    d=bpy.data.lights.new(name,'AREA');d.energy=power;d.color=col;d.size=8
    ob=bpy.data.objects.new(name,d);CAVE.objects.link(ob);ob.location=p
for col,pos in [(TROLL,(-3,5,0)),(ARM,(-1.4,4.5,2.8))]:
    ob=bpy.data.objects.new('Rumle at home • '+col.name,None);CAVE.objects.link(ob);ob.instance_type='COLLECTION';ob.instance_collection=col;ob.location=pos
camera=scene.camera;camera.name='Landscape camera'
cam=bpy.data.cameras.new('Cave camera');c=bpy.data.objects.new('Cave camera',cam);STUDIO.objects.link(c);c.location=(1,-12,6.3);c.rotation_euler=(Vector((-2,10,4))-c.location).to_track_quat('-Z','Y').to_euler();cam.lens=24
scene.camera=c
OUTSIDE.hide_render=True;CAVE.hide_render=False;CAVE.hide_viewport=False;OUTSIDE.hide_viewport=True
bpy.data.collections['Tarn • water'].hide_render=True;bpy.data.collections['Tarn • water'].hide_viewport=True
scene.render.filepath=OUT+'/cave-v1.png';scene.cycles.samples=48
for area in bpy.context.screen.areas:
    if area.type=='VIEW_3D':area.spaces.active.region_3d.view_perspective='CAMERA'
bpy.ops.wm.save_as_mainfile(filepath=OUT+'/Heileberget.blend')
print('CAVE_READY',len(CAVE.objects),'objects; 40 x 42 m; connected halls')
def render_preview():bpy.ops.render.render(write_still=True);return None
bpy.app.timers.register(render_preview,first_interval=1)
