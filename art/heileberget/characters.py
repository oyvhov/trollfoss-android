"""Eira and Eilev: original articulated storybook models, based on Residents' approved looks.
Run in the owned Blender MCP session. No photos or downloaded meshes are embedded.
Five reusable parts per child; shoulder/hip pivots match BergCharacters.kt.
"""
import bpy, ast, pathlib, math, random, json
from mathutils import Vector
from math import sin, cos, pi
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
# Reuse only the modelling functions, never the scene-reset code.
tree=ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'modelling helpers','exec'))
scene=bpy.data.scenes.get('Eira og Eilev') or bpy.data.scenes.new('Eira og Eilev')
bpy.context.window.scene=scene
for col in list(scene.collection.children):
    if col.name.startswith('Children •'):
        for o in list(col.objects):bpy.data.objects.remove(o,do_unlink=True)
        bpy.data.collections.remove(col)
stage=bpy.data.collections.new('Children • display');scene.collection.children.link(stage)
current=stage
skin=mat('Children • warm skin','F4C6A2');skinrose=mat('Children • cheeks','E7A28E')
white=mat('Children • ivory','FFF9EB');dark=mat('Children • ink','343342');lip=mat('Children • smile','9D625A')
lilac=mat('Eira • lilac cotton','B699CE');lilacDark=mat('Eira • seams','856D9D');orange=mat('Eira • apricot','E99D53')
yellow=mat('Eira • sunshine','FFD464');pink=mat('Eira • flower petals','FFE5EC')
teal=mat('Eilev • teal kit','158783');tealDark=mat('Eilev • collar','09615E');navy=mat('Eilev • shorts','2F3552')
lime=mat('Eilev • lime cleats','A4D43B');sole=mat('Children • rubber soles','66717B')
hairEira=mat('Eira • natural blonde','CBB58D');hairEilev=mat('Eilev • light brown','A67B50')
hairLight=mat('Children • warm hair glints','E3C69C')
irisEira=mat('Eira • grey blue iris','6C778E');irisEilev=mat('Eilev • hazel iris','805535')

def silhouette(name,rings,material):
    vertices=[];faces=[];n=24
    for z,rx,ry in rings:
        vertices.extend([(rx*cos(i*2*pi/n),ry*sin(i*2*pi/n),z) for i in range(n)])
    for j in range(len(rings)-1):
        for i in range(n):a=j*n+i;b=j*n+(i+1)%n;faces.append((a,b,b+n,a+n))
    faces.extend([tuple(reversed(range(n))),tuple(range((len(rings)-1)*n,len(rings)*n))])
    return mesh(name,vertices,faces,material,True)

def hair_cap(material):
    # An open cap, with a high forehead and lower back. No sphere over the face.
    vertices=[];faces=[];n=32;rows=10
    for j in range(rows+1):
        for i in range(n):
            a=i*2*pi/n;front=max(0,-sin(a));end=1.80-.86*front
            t=.015+(end-.015)*j/rows
            vertices.append((.354*sin(t)*cos(a),.292*sin(t)*sin(a)+.018,1.565+.407*cos(t)))
    for j in range(rows):
        for i in range(n):a=j*n+i;b=j*n+(i+1)%n;faces.append((a,a+n,b+n,b))
    mesh('Sculpted hair cap',vertices,faces,material,True)

def strand(name,points,radius,material):
    # Catmull-Rom locks avoid angular, chopped hair when viewed close up.
    p=[Vector(points[0])]+[Vector(v) for v in points]+[Vector(points[-1])];smooth=[]
    for k in range(1,len(p)-2):
        for j in range(5):
            t=j/5;smooth.append(.5*((2*p[k])+(-p[k-1]+p[k+1])*t+(2*p[k-1]-5*p[k]+4*p[k+1]-p[k+2])*t*t+(-p[k-1]+3*p[k]-3*p[k+1]+p[k+2])*t*t*t))
    smooth.append(Vector(points[-1]))
    tube(name,smooth,[radius*(.08+.92*sin(pi*(i+.5)/len(smooth))) for i in range(len(smooth))],material,10)

collections={}
for who in ['eira','eilev']:
    eira=who=='eira';hair=hairEira if eira else hairEilev;cloth=lilac if eira else teal
    groups={}
    for part in ['body','arm-l','arm-r','leg-l','leg-r']:
        name='Children • '+who+'-'+part
        old=bpy.data.collections.get(name)
        if old:
            for o in list(old.objects):bpy.data.objects.remove(o,do_unlink=True)
            bpy.data.collections.remove(old)
        groups[part]=bpy.data.collections.new(name);scene.collection.children.link(groups[part])
    current=groups['body']
    silhouette('Tailored shirt',[(.60,.25,.15),(.64,.28,.17),(.92,.29,.18),(1.12,.32,.17),(1.20,.25,.14),(1.23,.15,.11)],cloth)
    silhouette('Soft shorts waist',[(.53,.25,.14),(.65,.27,.16),(.69,.25,.16)],lilac if eira else navy)
    ell('Neck',(0,0,1.24),(.105,.10,.14),skin,20,12)
    # A single continuous oval face: no added cheek balls or separate chin lumps.
    head=ell(who.title()+' • soft oval face',(0,-.012,1.56),(.327,.277,.365),skin,40,28)
    for v in head.data.vertices:
        lower=max(0,min(1,(-v.co.z-.035)/.33))
        v.co.x*=1-.22*lower
    head.data.update()
    for sign in [-1,1]:
        ell('Small ear',(sign*.324,.005,1.53),(.054,.05,.078),skin,20,16)
    ell('Tiny button nose',(0,-.278,1.505),(.039,.037,.038),skin,24,16)
    for sign in [-1,1]:
        ell('Ear hollow',(sign*.342,-.038,1.53),(.023,.010,.039),skinrose,16,10)
        ell('Eye white',(sign*.117,-.250,1.585),(.073,.026,.087),white,28,20)
        ell('Iris',(sign*.117,-.275,1.58),(.040,.010,.055),irisEira if eira else irisEilev,24,16)
        ell('Pupil',(sign*.117,-.283,1.58),(.023,.006,.034),dark,20,16)
        ell('Eye sparkle',(sign*.117-.012,-.290,1.603),(.011,.004,.014),white,16,10)
        strand('Gentle arched brow',[(sign*.117-.052,-.235,1.702),(sign*.117-.018,-.252,1.715),(sign*.117+.024,-.249,1.714),(sign*.117+.055,-.233,1.703)],.010,hair)
    strand('Small warm smile',[(-.066,-.247,1.412),(-.034,-.257,1.399),(0,-.260,1.395),(.034,-.257,1.399),(.066,-.247,1.412)],.008,lip)
    hair_cap(hair)
    if eira:
        for sign in [-1,1]:
            for j in range(5):
                a=(j/4)*1.7+.06;y=-.20+j*.08
                strand('Long side lock',[(sign*.28,y,1.82),(sign*.35,y,1.65),(sign*.36,y+.015,1.43),(sign*.34,y+.035,1.25),(sign*.29,y+.06,1.20)],.053,hair)
        for j in range(7):
            strand('Swept side part',[(.13+j*.012,.04,1.965),(.01-j*.035,-.16,1.943),(-.15-j*.025,-.245,1.85),(-.26-j*.006,-.235,1.74)],.041,hair if j%3 else hairLight)
        # Apricot panels, dimensional sun and two embroidered flowers.
        cube('Apricot panel',(-.20,-.151,.91),(.075,.028,.44),orange,.012)
        cube('Apricot pocket',(.15,-.179,.73),(.15,.025,.12),orange,.012)
        ell('Sun badge',(.075,-.190,.99),(.061,.014,.061),yellow,20,12)
        for i in range(10):
            a=i*pi/5;curve('Sun ray',[(.075+cos(a)*.074,-.19,.99+sin(a)*.074),(.075+cos(a)*.092,-.185,.99+sin(a)*.092)],.006,yellow)
        for x,z in [(-.06,.83),(.1,.69)]:
            for i in range(5):
                a=i*2*pi/5;ell('Embroidered petal',(x+cos(a)*.029,-.184,z+sin(a)*.029),(.021,.012,.023),pink,12,8)
            ell('Flower centre',(x,-.20,z),(.014,.01,.014),orange,12,8)
    else:
        for i in range(12):
            a=i*2*pi/12
            strand('Loose crown wave',[(.14*cos(a),.13*sin(a),1.94),(.26*cos(a),.23*sin(a),1.94),(.33*cos(a),.27*sin(a),1.84),(.31*cos(a+.14),.25*sin(a+.14),1.79)],.064,hair)
        for j in range(5):
            x=-.25+j*.10
            strand('Wavy fringe',[(x,-.21,1.89),(x+.04,-.28,1.85),(x+.025,-.30,1.77),(x-.025,-.27,1.76)],.042,hair)
        for x in [-.14,0,.14]:cube('White football stripe',(x,-.172,.91),(.045,.028,.49),white,.008)
        cube('Football crest',(-.19,-.19,1.08),(.065,.024,.08),yellow,.018)
    curve('Shirt hem',[(-.24,-.09,.63),(-.18,-.153,.62),(0,-.175,.62),(.18,-.153,.62),(.24,-.09,.63)],.009,lilacDark if eira else tealDark)
    curve('Round collar',[(-.14,-.05,1.22),(-.10,-.115,1.18),(0,-.15,1.16),(.10,-.115,1.18),(.14,-.05,1.22)],.022,lilacDark if eira else white)
    for sign,part in [(-1,'arm-l'),(1,'arm-r')]:
        current=groups[part]
        # Coordinates are authored in the rest pose, then moved to the shoulder pivot.
        tube('Short sleeve',[(sign*.26,0,1.12),(sign*.32,0,1.10),(sign*.36,0,1.06),(sign*.385,-.01,.98)],[.095,.111,.105,.093],cloth,20)
        tube('Forearm',[(sign*.38,-.01,.99),(sign*.405,-.02,.87),(sign*.41,-.045,.74)],[.074,.065,.058],skin,16)
        ell('Palm',(sign*.415,-.05,.715),(.075,.065,.088),skin,20,12)
        ell('Thumb',(sign*.36,-.085,.725),(.032,.038,.048),skin,16,10)
        for i in range(3):curve('Finger crease',[(sign*(.39+i*.024),-.111,.71),(sign*(.39+i*.024),-.108,.68)],.0035,skinrose)
        curve('Sleeve cuff',[(sign*.32,-.05,.99),(sign*.38,-.103,.99),(sign*.44,-.055,.99)],.013,lilacDark if eira else white)
        for o in current.objects:o.location-=Vector((sign*.33,0,1.16))
    for sign,part in [(-1,'leg-l'),(1,'leg-r')]:
        current=groups[part]
        tube('Shorts leg',[(sign*.145,0,.62),(sign*.15,0,.49),(sign*.15,0,.43)],[.126,.12,.115],lilac if eira else navy,20)
        tube('Knee and calf',[(sign*.15,0,.45),(sign*.15,-.015,.33),(sign*.15,0,.18)],[.09,.079,.065],skin,20)
        tube('Sock',[(sign*.15,0,.24),(sign*.15,0,.13)],[.076,.076],white,20)
        ell('Shoe sole',(sign*.15,-.065,.055),(.109,.193,.048),sole,24,12)
        ell('Rounded trainer',(sign*.15,-.07,.105),(.102,.18,.072),white if eira else lime,24,12)
        for i in range(3):curve('Shoelace',[(sign*.15-.048,-.05-i*.031,.168-i*.006),(sign*.15+.048,-.06-i*.031,.168-i*.006)],.007,white if not eira else lilacDark)
        for o in current.objects:o.location-=Vector((sign*.145,0,.60))
    # Linked display instances articulate the very same five parts exported to Android.
    offset=-.9 if eira else .9
    for part,col in groups.items():
        ob=bpy.data.objects.new(who.title()+' • '+part,None);stage.objects.link(ob);ob.instance_type='COLLECTION';ob.instance_collection=col
        pivot=(0,0,0) if part=='body' else ((-.33 if part=='arm-l' else .33),0,1.16) if part.startswith('arm') else ((-.145 if part=='leg-l' else .145),0,.60)
        scale=.975 if eira else 1.125
        ob.scale=(scale,scale,scale);ob.location=(offset+pivot[0]*scale,pivot[1]*scale,pivot[2]*scale)
        if part!='body':
            sign=-1 if part.endswith('l') else 1
            if part.startswith('arm'):sign=-sign
            for f,angle in [(1,0),(7,sign*.38),(13,0),(19,-sign*.38),(25,0)]:ob.rotation_euler[0]=angle;ob.keyframe_insert(data_path='rotation_euler',frame=f)
        scene.collection.children.unlink(col)
    collections[who]=groups

current=stage
ground=mat('Children • studio floor','D7D7C1')
cube('Presentation plinth',(0,0,-.08),(5,3,.16),ground,.08)
for x,label in [(-.9,'Eira'),(.9,'Eilev')]:
    font=bpy.data.curves.new(label,'FONT');font.body=label;font.align_x='CENTER';font.size=.16;font.extrude=.001
    ob=bpy.data.objects.new(label+' label',font);stage.objects.link(ob);ob.location=(x,-.8,-.001);ob.data.materials.append(dark)
camera=bpy.data.objects.new('Children portrait camera',bpy.data.cameras.new('Children portrait camera'));stage.objects.link(camera)
camera.location=(3,-7,3.1);camera.rotation_euler=(Vector((0,0,1.0))-camera.location).to_track_quat('-Z','Y').to_euler();camera.data.type='ORTHO';camera.data.ortho_scale=4.2;scene.camera=camera
for name,p,energy,size in [('Key',(-3,-4,6),550,5),('Fill',(4,-2,3),270,4),('Rim',(0,3,5),450,3)]:
    light=bpy.data.lights.new('Children '+name,'AREA');light.energy=energy;light.shape='DISK';light.size=size
    ob=bpy.data.objects.new(light.name,light);stage.objects.link(ob);ob.location=p;ob.rotation_euler=(Vector((0,0,1))-ob.location).to_track_quat('-Z','Y').to_euler()
scene.world=bpy.data.worlds.new('Children warm studio');scene.world.use_nodes=True;scene.world.node_tree.nodes['Background'].inputs[0].default_value=(.6,.7,.8,1);scene.world.node_tree.nodes['Background'].inputs[1].default_value=.35
scene.render.engine='CYCLES';scene.cycles.device='GPU';scene.cycles.samples=32;scene.cycles.use_denoising=True
scene.render.resolution_x=1440;scene.render.resolution_y=1000;scene.render.resolution_percentage=100
scene.view_settings.view_transform='AgX';scene.view_settings.look='AgX - Medium High Contrast'
scene.render.image_settings.file_format='PNG';scene.render.filepath='C:/topa/dist/heileberget-blender/eira-eilev-blender.png'
scene.frame_start=1;scene.frame_end=25;scene.frame_set(1)
for a in bpy.context.screen.areas:
    if a.type=='VIEW_3D':a.spaces.active.region_3d.view_perspective='CAMERA';a.spaces.active.shading.type='MATERIAL';a.spaces.active.overlay.show_overlays=False
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
def preview():bpy.ops.render.render(write_still=True);return None
bpy.app.timers.register(preview,first_interval=1)
print('EIRA_EILEV_MODELLED: 5 articulated parts each; scene Eira og Eilev')
