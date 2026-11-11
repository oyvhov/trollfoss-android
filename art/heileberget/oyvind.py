"""A matching articulated adult portrait, preserving Residents.oyvindLook().
Own Blender scene only. Rerunnable; does not alter either child or their source meshes.
"""
import bpy, ast, pathlib, math
from math import sin, cos, pi, acos
from mathutils import Vector
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
for filename in ['build_scene.py','characters.py']:
    tree=ast.parse((ROOT/'art/heileberget'/filename).read_text(encoding='utf-8'))
    exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef)],type_ignores=[]),'modelling helpers','exec'))
scene=bpy.data.scenes['Eira og Eilev'];bpy.context.window.scene=scene
stage=bpy.data.collections['Children • display']
for ob in list(stage.objects):
    if ob.name.startswith('Oyvind •'):bpy.data.objects.remove(ob,do_unlink=True)
groups={}
for part in ['body','arm-l','arm-r','leg-l','leg-r']:
    name='Family • oyvind-'+part
    col=bpy.data.collections.get(name)
    if col:
        for ob in list(col.objects):bpy.data.objects.remove(ob,do_unlink=True)
        bpy.data.collections.remove(col)
    col=bpy.data.collections.new(name);scene.collection.children.link(col);groups[part]=col

def material(name,h):return bpy.data.materials.get(name) or mat(name,h)
skin=material('Oyvind • warm skin','F9D0B0');rose=material('Oyvind • ear warmth','D99F8E')
hair=material('Oyvind • salt and pepper','6C675F');hairglint=material('Oyvind • soft grey','929085')
shirt=material('Oyvind • dusty rose shirt','C49894');seam=material('Oyvind • shirt stitching','9E7476')
navy=material('Eilev • shorts','2F3552');ivory=material('Children • ivory','FFF9EB')
ink=material('Children • ink','343342');lip=material('Children • smile','9D625A')
iris=material('Eilev • hazel iris','805535');sole=material('Children • rubber soles','66717B')
leather=material('Oyvind • warm leather','A0663B')
current=groups['body']
silhouette('Oyvind tailored shirt',[(.57,.27,.16),(.63,.30,.18),(.92,.30,.19),(1.13,.34,.18),(1.24,.27,.14),(1.27,.14,.105)],shirt)
silhouette('Oyvind trousers waist',[(.50,.26,.16),(.64,.28,.17),(.67,.27,.165)],navy)
ell('Oyvind neck',(0,0,1.28),(.11,.10,.14),skin,20,12)
ell('Oyvind smooth oval face',(0,-.008,1.60),(.292,.25,.335),skin,40,28)
for sign in [-1,1]:
    ell('Oyvind small ear',(sign*.29,.006,1.57),(.05,.044,.074),skin,20,14)
    ell('Oyvind ear hollow',(sign*.308,-.029,1.57),(.021,.009,.038),rose,16,10)
    ell('Oyvind eye white',(sign*.107,-.226,1.63),(.063,.023,.072),ivory,24,16)
    ell('Oyvind iris',(sign*.107,-.247,1.625),(.033,.009,.043),iris,20,14)
    ell('Oyvind pupil',(sign*.107,-.255,1.625),(.019,.005,.027),ink,16,12)
    ell('Oyvind eye glint',(sign*.107-.01,-.261,1.644),(.009,.004,.011),ivory,12,8)
    strand('Oyvind brow',[(sign*.107-.048,-.219,1.723),(sign*.107-.016,-.235,1.733),(sign*.107+.022,-.231,1.732),(sign*.107+.049,-.215,1.723)],.011,hair)
ell('Oyvind small nose',(0,-.256,1.555),(.046,.039,.045),skin,24,16)
strand('Oyvind friendly smile',[(-.061,-.217,1.466),(-.03,-.23,1.452),(0,-.234,1.448),(.03,-.23,1.452),(.061,-.217,1.466)],.008,lip)

# Trimmed beard hugs the face instead of adding a second oval under the chin.
vertices=[];faces=[];columns=36;rows=9
for j in range(rows+1):
    for i in range(columns+1):
        a=-pi+.13+i*(pi-.26)/columns
        front=max(0,-sin(a));top=1.60-.18*front**3
        t0=acos((top-1.60)/.338);t=t0+(2.78-t0)*j/rows
        vertices.append((.297*sin(t)*cos(a),-.008+.255*sin(t)*sin(a),1.60+.338*cos(t)))
for j in range(rows):
    for i in range(columns):
        k=j*(columns+1)+i;faces.append((k,k+columns+1,k+columns+2,k+1))
mesh('Oyvind fitted trimmed beard',vertices,faces,hair,True)

# Short, gently swept hair with one continuous cap and a few soft combed ridges.
vertices=[];faces=[];columns=36;rows=12
for j in range(rows+1):
    for i in range(columns):
        a=i*2*pi/columns;front=max(0,-sin(a));end=1.85-.80*front
        t=.01+(end-.01)*j/rows
        vertices.append((.30*sin(t)*cos(a),.012+.262*sin(t)*sin(a),1.60+.347*cos(t)))
for j in range(rows):
    for i in range(columns):
        k=j*columns+i;b=j*columns+(i+1)%columns;faces.append((k,k+columns,b+columns,b))
mesh('Oyvind continuous short hair',vertices,faces,hair,True)
for j in range(5):
    # Follow the actual cap instead of identical free-standing comb teeth.
    points=[]
    for k in range(12):
        t=1.04-k*.066
        a=-2.15+j*.23+k*.035
        points.append((.302*sin(t)*cos(a),.012+.264*sin(t)*sin(a),1.60+.349*cos(t)))
    strand('Oyvind swept hair',points,.012,hairglint if j==1 else hair)

# Open shirt, ivory inner shirt, curved collar, seam and little buttons.
cube('Oyvind inner shirt',(0,-.187,.92),(.14,.016,.53),ivory,.012)
for sign in [-1,1]:
    strand('Oyvind opening seam',[(sign*.09,-.16,1.20),(sign*.075,-.198,1.08),(sign*.075,-.203,.81),(sign*.078,-.184,.64)],.01,seam)
    strand('Oyvind folded collar',[(sign*.11,-.07,1.267),(sign*.20,-.14,1.19),(sign*.13,-.195,1.13)],.025,shirt)
for j in range(4):ell('Oyvind button',(-.105,-.207,.72+j*.115),(.012,.006,.012),ivory,10,6)
cube('Oyvind shirt pocket',(.21,-.181,1.04),(.09,.025,.10),shirt,.014)
strand('Oyvind pocket seam',[(.165,-.202,1.08),(.21,-.205,1.075),(.255,-.19,1.08)],.005,seam)

for sign,part in [(-1,'arm-l'),(1,'arm-r')]:
    current=groups[part]
    tube('Oyvind shirt sleeve',[(sign*.28,0,1.16),(sign*.35,0,1.10),(sign*.39,-.012,.94),(sign*.41,-.03,.85)],[.10,.114,.095,.084],shirt,20)
    tube('Oyvind wrist',[(sign*.412,-.03,.85),(sign*.423,-.045,.75)],[.07,.058],skin,16)
    ell('Oyvind hand',(sign*.426,-.05,.715),(.077,.066,.088),skin,20,14)
    ell('Oyvind thumb',(sign*.37,-.087,.727),(.033,.036,.049),skin,16,12)
    strand('Oyvind cuff',[(sign*.35,-.05,.86),(sign*.41,-.115,.86),(sign*.47,-.05,.86)],.014,seam)
    for ob in current.objects:ob.location-=Vector((sign*.33,0,1.16))
for sign,part in [(-1,'leg-l'),(1,'leg-r')]:
    current=groups[part]
    tube('Oyvind trouser leg',[(sign*.145,0,.63),(sign*.15,0,.43),(sign*.15,-.012,.28),(sign*.15,0,.14)],[.125,.116,.097,.09],navy,20)
    ell('Oyvind shoe sole',(sign*.15,-.066,.05),(.115,.19,.045),sole,24,12)
    ell('Oyvind leather shoe',(sign*.15,-.068,.105),(.108,.18,.073),leather,24,14)
    for j in range(3):curve('Oyvind shoelace',[(sign*.15-.044,-.05-j*.028,.169-j*.005),(sign*.15+.044,-.05-j*.028,.169-j*.005)],.006,ivory,6)
    for ob in current.objects:ob.location-=Vector((sign*.145,0,.60))

for part,col in groups.items():
    ob=bpy.data.objects.new('Oyvind • '+part,None);stage.objects.link(ob);ob.instance_type='COLLECTION';ob.instance_collection=col
    pivot=(0,0,0) if part=='body' else ((-.33 if part=='arm-l' else .33),0,1.16) if part.startswith('arm') else ((-.145 if part=='leg-l' else .145),0,.60)
    scale=1.425;ob.scale=(scale,scale,scale);ob.location=(2.8+pivot[0]*scale,pivot[1]*scale,pivot[2]*scale)
    if part!='body':
        sign=-1 if part.endswith('l') else 1
        if part.startswith('arm'):sign=-sign
        for f,angle in [(1,0),(7,sign*.38),(13,0),(19,-sign*.38),(25,0)]:ob.rotation_euler[0]=angle;ob.keyframe_insert(data_path='rotation_euler',frame=f)
    scene.collection.children.unlink(col)
scene.frame_set(1)
scene.camera.location=(4.6,-8.5,3.8);scene.camera.rotation_euler=(Vector((.9,0,1.3))-scene.camera.location).to_track_quat('-Z','Y').to_euler();scene.camera.data.ortho_scale=6.0
scene.render.filepath='C:/topa/dist/heileberget-blender/family-round6.png'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
def preview():bpy.ops.render.render(write_still=True);return None
if globals().get('RENDER_PREVIEW',True):bpy.app.timers.register(preview,first_interval=1)
print('OYVIND_MODELLED: smooth adult proportions, fitted beard, five articulated parts')
