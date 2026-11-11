"""Author Rumle and the berry outing from the original Heileberget.blend in Blender.
Run in the mountain file. Writes a separate editable Rumle.blend and only this pass's Android assets.
"""
import ast
import bpy
import gzip
import hashlib
import json
import math
import random
import struct
from math import cos, sin, pi
from pathlib import Path
from mathutils import Vector, Matrix
from mathutils.bvhtree import BVHTree

HERE = Path(__file__).resolve().parent
DEST = HERE.parents[1] / "app/src/main/assets/heileberget"
assert Path(bpy.data.filepath).name == "Heileberget.blend"
source = bpy.data.collections["Rumle • sculpt"]
# Share the original sculpt's mesh and material tooling without running the landscape passes.
helpers = ast.parse((HERE / "build_scene.py").read_text(encoding="utf-8"))
exec(compile(ast.Module([n for n in helpers.body if isinstance(n, ast.FunctionDef)], []), "sculpt helpers", "exec"))
scene = bpy.data.scenes.new("Rumle ved grua")
bpy.context.window.scene = scene
parts = {}
def part(name):
    global current
    current = bpy.data.collections.new("Workshop • " + name)
    scene.collection.children.link(current)
    parts[name] = current
def xyz(p):
    return (p[0], -p[2], p[1])
def oval(name, p, r, material, seg=20, rings=12):
    return ell(name, xyz(p), (r[0], r[2], r[1]), material, seg, rings)
def rope(name, path, radii, material, sides=10):
    return tube(name, [xyz(p) for p in path], radii, material, sides)
def sheet(name, points, faces, material):
    return mesh(name, [xyz(p) for p in points], faces, material, True)
def ring_surface(name, profile, material, segments=36):
    points = [(rx*cos(i*2*pi/segments), h, rz*sin(i*2*pi/segments)) for h,rx,rz in profile for i in range(segments)]
    faces = [(j*segments+i, (j+1)*segments+i, (j+1)*segments+(i+1)%segments, j*segments+(i+1)%segments)
             for j in range(len(profile)-1) for i in range(segments)]
    return sheet(name,points,faces,material)

M = bpy.data.materials
skin = M["Warm granite skin"]
wool = mat("Rumle • pine wool", "496F69")
hem = mat("Rumle • petrol ribbing", "335752")
yarn = mat("Rumle • heather yarn", "A3B4A2")
brass = mat("Rumle • horn toggles", "CCA26B")
clay = mat("Rumle • honey glaze", "D69452")
rim = mat("Rumle • cream stoneware", "F9DDB3")
juice = mat("Rumle • bilberry cordial", "6B3354")
leafgreen = mat("Rumle • bilberry leaves", "608047")
leaflight = mat("Rumle • leaf tips", "98A95B")
blues = [mat("Rumle • bilberry " + str(i), c) for i,c in enumerate(["4F5686","747DAA","545C98"])]
bloom = mat("Rumle • berry blossom scar", "343D65")
wood = M["Cut timber"]
rng = random.Random(722)
part("troll")
body = parts["troll"]
part("rumle-eyes")
eyes = current
part("rumle-gaze")
gaze = current
continuous = None
for original in source.objects:
    name = original.name.split(".")[0]
    if name in {"Open happy smile","Sculpted smile rim","Lower lip","Little uneven tooth","Warm cheek",
                "Granite freckle","Moss lock","Overlapping moss frond","Moss eyebrow","Shoulder moss"}:
        continue
    target = eyes if name in {"Eye","Lower eyelid","Upper eyelid"} else gaze if name in {"Hazel iris","Pupil","Eye catchlight"} else body
    ob = original.copy()
    target.objects.link(ob)
    if name == "Rumle • continuous body sculpt":
        continuous = ob
    if target in (eyes,gaze):
        # Narrow the resting aperture about the eye line, including all fitted lids.
        centre=Vector((0,0,4.57))
        ob.matrix_basis=Matrix.Translation(centre) @ Matrix.Diagonal(Vector((1,1,.79,1))) @ Matrix.Translation(-centre) @ ob.matrix_basis
    if name == "Rosy pear nose":
        ob.scale *= .91
    if name == "Nostril":
        ob.location.y += .038
        ob.location.z += .041

current = body
# Fit the coat to the actual seated sculpt along each radial stitch line.
skin_matrix = continuous.matrix_basis
skin_tree = BVHTree.FromPolygons([skin_matrix @ v.co for v in continuous.data.vertices],
                                 [tuple(f.vertices) for f in continuous.data.polygons])
def cloth_point(a, y, extra=.05):
    direction = Vector((sin(a), -cos(a), 0))
    origin = Vector((0,.05,y))
    p, _, _, _ = skin_tree.ray_cast(origin, direction, 3.2)
    d = (p-origin).length if p is not None else 1
    if abs(sin(a))>.50:
        # The fused resting arm is outside the ribcage. Keep the armhole beneath it.
        core=0
        for cy,ry,rx,rz,cz in [(1.9,1.7,1.55,1.02,-.05),(3,1.12,1.80,.90,-.12)]:
            az=sin(a)**2/rx**2+cos(a)**2/rz**2
            bz=cos(a)*(-.05-cz)/rz**2
            cc=((- .05-cz)/rz)**2+((y-cy)/ry)**2-1
            if bz*bz-az*cc>=0: core=max(core,(-bz+math.sqrt(bz*bz-az*cc))/az)
        if core>0: d=min(d,core+.014)
    d += extra + .018*cos(12*a+.7*y) + .012*sin(21*a-1.3*y)
    return (sin(a)*d,y,cos(a)*d-.05)
rows, cols = 18, 64
vest = []
for j in range(rows+1):
    t = j/rows
    gap = .12 + .55*t**3
    for i in range(cols+1):
        a = gap + (2*pi-2*gap)*i/cols
        y = 1.12+2.46*t - 1.50*t**2.2*abs(sin(a))**7 + .06*cos(a*2)*(1-t)
        vest.append(cloth_point(a,y))
sheet("Soft split-front wool waistcoat",vest,
      [(j*(cols+1)+i,(j+1)*(cols+1)+i,(j+1)*(cols+1)+i+1,j*(cols+1)+i+1) for j in range(rows) for i in range(cols)],wool)
for edge in (vest[:cols+1],vest[-cols-1:],vest[::cols+1],vest[cols::cols+1]):
    rope("Bound wool edge",edge,[.048]*len(edge),hem)
for j in (6,10,14):
    left = vest[j*(cols+1)]; right=vest[j*(cols+1)+cols]
    z=max(left[2],right[2])+.045
    rope("Braided button loop",[(right[0],left[1],z),(0,left[1]-.023,z+.05),(left[0],left[1],z)],[.031,.04,.031],hem)
    oval("Horn toggle",(left[0],left[1],z+.03),(.115,.056,.065),brass,16,10)
for side in (-1,1):
    rail = [cloth_point(side*(.54+i*.062),1.76+.105*sin(i*pi/8),.085) for i in range(9)]
    rope("Pocket welt",rail,[.038]*9,hem)
    for i in range(0,8,2):
        rope("Sewn pocket stitch",rail[i:i+2],[.011,.011],yarn,5)
# A quieter smile with lifted corners, an actual curved inner surface and two small teeth.
def smile_z(x): return 1.355-.18*(x/.69)**2
mouth = [(0,3.80,1.36)]
for i in range(48):
    a=2*pi*i/48; x=.71*cos(a)
    mouth.append((x,3.80+.15*cos(a)**2 + (.095 if sin(a)>0 else .14)*sin(a),smile_z(x)))
sheet("Curved smile opening",mouth,[(0,1+(i+1)%48,1+i) for i in range(48)],M["Smile interior"])
rope("Soft lower lip",mouth[25:]+[mouth[1]],[.026+.023*sin(pi*i/24) for i in range(25)],M["Lids and lips"])
rope("Upper lip",mouth[1:26],[.017+.020*sin(pi*i/24) for i in range(25)],M["Lids and lips"])
for x,width in [(-.45,.15),(-.26,.14),(.40,.16)]:
    tooth=cube("Small ivory tooth",xyz((x,3.868+.15*(x/.71)**2,smile_z(x)+.007)),
               (width,.052,.090),M["Teeth • ivory"],.029)
    tooth.rotation_euler[1]=x*.13
# Seated weight is carried by the cushion, the knees project over its edge, and
# the cuffs meet the original sculpted feet at the front of the seat.
denim=mat("Rumle • slate twill","586775")
seam=mat("Rumle • worn blue seam","89939C")
for side in (-1,1):
    thigh=oval("Bent trouser leg",(side*.97,1.07,.63),(.70,.47,.96),denim,28,18)
    thigh.rotation_euler[2]=side*.10
    oval("Rounded knee",(side*1.03,1.01,1.37),(.61,.47,.47),denim,28,16)
    oval("Shin",(side*1.04,.65,1.39),(.47,.42,.37),denim,24,16)
    oval("Rolled trouser cuff",(side*1.04,.46,1.37),(.475,.135,.38),seam,24,12)
    rope("Outside trouser seam",[(side*1.49,1.12,.72),(side*1.56,1.11,1.25),
         (side*1.53,.89,1.46),(side*1.48,.57,1.43)],[.020,.025,.023,.016],seam,7)
# Moss fronds lie against the scalp in overlapping curved sweeps; both ends close gently.
def crown(a,t):
    return Vector((1.25*sin(t)*cos(a),4.55+1.14*cos(t),.1+.94*sin(t)*sin(a)))
def tuft(a,t,span,width,material):
    pts=[]; widths=(.06,.70,1,.76,.30,.035);sides=8
    for j,wid in enumerate(widths):
        u=j/(len(widths)-1);az=a+.25*u;theta=t+span*u
        centre=crown(az,theta)
        radial=Vector((sin(theta)*cos(az),cos(theta),sin(theta)*sin(az))).normalized()
        across=Vector((-sin(az),0,cos(az)))
        centre+=radial*(.045+.052*sin(u*pi))
        for k in range(sides):
            ang=k*2*pi/sides
            pt=centre+across*width*wid*cos(ang)+radial*(.065*wid*sin(ang))
            pts.append(tuple(pt))
    sheet("Curled moss frond",pts,[(j*sides+k,j*sides+(k+1)%sides,(j+1)*sides+(k+1)%sides,(j+1)*sides+k)
          for j in range(len(widths)-1) for k in range(sides)],material)
for i in range(102):
    a=i*2.39996
    # Shorter fringe at the face, long curls at the nape.
    limit=1.56-.36*sin(a)
    t=.06+(limit-.18)*math.sqrt((i+.4)/102)
    tuft(a,t,.26+rng.random()*.20,.12+rng.random()*.075,M["Moss %02d"%(1+i%4)])
for side in (-1,1):
    rope("Soft arched brow",[(side*.21,4.84,1.025),(side*.43,4.98,1.045),(side*.67,4.99,.975),(side*.91,4.86,.82)],
         [.065,.106,.10,.025],M["Moss 01"])

# Upper arm and forearm have local Y axes down the bone. The overlap is at the elbow.
part("arm")
oval("Upper arm",(0,.50,0),(.47,.76,.46),skin,40,28)
oval("Shoulder cap",(0,.10,0),(.54,.50,.50),skin,36,24)
part("rumle-forearm")
ring_surface("Tapered forearm",[(0,.34,.34),(.13,.38,.38),(.38,.40,.39),(.62,.35,.34),(.87,.26,.25),(1,.21,.23)],skin)
oval("Elbow",(0,.055,0),(.36,.31,.36),skin,24,14)
# Palm origin is the centre of the grip. Fingers fold around the cup and thumb opposes them.
for closed in (False,True):
    part("rumle-hand-grip" if closed else "rumle-hand-open")
    oval("Palm",(.11,-.02,-.12),(.34,.36,.195),skin)
    oval("Wrist",(.05,-.29,-.16),(.255,.22,.225),skin)
    for i in range(4):
        x=-.13+i*.153
        length=(.37,.48,.44,.33)[i]
        knots=[(x,.13,-.08),(x,.28,.05),(x-.035,.24,.20),(x-.065,.14,.24)] if closed else [
              (x,.16,-.10),(x+(i-1.5)*.025,.24+length*.32,-.09),(x+(i-1.5)*.052,.22+length,-.025)]
        rope("Finger",knots,[.099,.101,.089,.066] if closed else [.098,.093,.076],skin,12)
        oval("Fingertip",knots[-1],(.069,.077,.071),skin,14,10)
    thumb=[(-.08,-.16,-.10),(-.27,-.08,-.025),(-.28,.035,.105),(-.18,.075,.17)] if closed else [
          (-.07,-.14,-.10),(-.29,-.03,-.08),(-.417,.14,-.02)]
    rope("Thumb",thumb,[.14,.127,.115,.09] if closed else [.14,.125,.095],skin,14)
    oval("Thumb tip",thumb[-1],(.097,.098,.092),skin,16,10)

# Small servings are recognisable in profile while being raised to the mouth.
for fizzy in (False,True):
    part("rumle-urge" if fizzy else "rumle-pepsi")
    glass=M["Urge bottle" if fizzy else "Pepsi bottle"]
    label=M["Urge yellow label" if fizzy else "Pepsi label blue"]
    ring_surface("Contoured glass",[(.006,.115,.115),(.03,.16,.16),(.08,.173,.173),(.19,.151,.151),
                 (.31,.153,.153),(.41,.16,.16),(.50,.151,.151),(.58,.095,.095),(.64,.057,.057),
                 (.74,.053,.053),(.77,.064,.064),(.795,.059,.059),(.795,.046,.046),(.76,.044,.044)],glass,24)
    ring_surface("Paper label",[(.19,.154,.154),(.27,.158,.158),(.39,.163,.163)],label,24)
    ring_surface("Foil label edge",[(.194,.156,.156),(.214,.157,.157)],rim,24)
    oval("Printed fruit mark",(0,.305,.155),(.062,.066,.008),M["Berries"] if not fizzy else leafgreen,16,10)
part("berry-cup")
ring_surface("Wheel-thrown mug",[(.005,.125,.125),(.04,.18,.18),(.09,.206,.206),(.34,.235,.235),(.36,.221,.221),
                                  (.34,.20,.20),(.09,.16,.16),(.075,.005,.005)],clay)
ring_surface("Glazed rim",[(.322,.233,.233),(.353,.24,.24),(.372,.223,.223),(.35,.203,.203)],rim)
rope("Thumb handle",[(.18,.30,0),(.32,.31,0),(.38,.26,0),(.37,.16,0),(.32,.11,0),(.18,.105,0)],[.04]*6,clay,12)
part("berry-cup-empty")
# Same mug without liquid; its fill level is driven by the ladle when pouring.
for piece in list(parts["berry-cup"].objects):
    current.objects.link(piece)
current=parts["berry-cup"]
oval("Cordial surface",(0,.298,0),(.202,.014,.202),juice,32,8)
twig=mat("Rumle • bilberry twig","807548")
part("berry-bush")
stems=[]
for i in range(13):
    a=i*2.4; r=.19+.49*((i%5)/4); h=.45+.32*((i%4)/3)
    ex=r*cos(a); ez=r*sin(a)
    rope("Bilberry twig",[(ex*.15,0,ez*.15),(ex*.4,h*.52,ez*.4),(ex*.77,h*.77,ez*.77),(ex,h,ez)],[.024,.020,.013,.009],twig,7)
    for k in range(4):
        az=a+(-1 if k%2 else 1)*1.2
        q=.47+k*.16; bx=ex*q; bz=ez*q; by=h*(.24+q*.72)
        tip=(bx+.27*cos(az),by+.11,bz+.27*sin(az))
        mid=((bx+tip[0])*.5,by+.09,(bz+tip[2])*.5)
        dx=-sin(az)*.086; dz=cos(az)*.086
        sheet("Veined bilberry leaf",[(bx,by,bz),(mid[0]+dx,mid[1]-.026,mid[2]+dz),mid,
              (mid[0]-dx,mid[1]-.026,mid[2]-dz),tip],[(0,1,2),(0,2,3),(1,4,2),(2,4,3)],leafgreen if k%3 else leaflight)
    stems.append((ex,h,ez))
part("berry-fruit")
for i,(x,y,z) in enumerate(stems):
    for k in range(2):
        p=(x+k*.083,y-.052-k*.077,z+.042)
        oval("Blueberry",p,(.055,.055,.054),blues[(i+k)%3],10,6)
        oval("Blossom scar",(p[0],p[1]+.019,p[2]+.049),(.020,.017,.006),bloom,8,5)
part("berry-basket")
ring_surface("Splint picking basket",[(.018,.19,.125),(.055,.24,.17),(.315,.32,.225),(.338,.312,.222),
                                     (.32,.288,.198),(.075,.21,.145),(.067,.006,.006)],wood,28)
for h in (.11,.17,.23,.29):
    r=.24+(h-.055)*.31
    ring_surface("Woven willow",[(h,r+.008,r*.72),(h+.018,r+.014,r*.72+.006)],brass,28)
rope("Bent willow handle",[(.288*cos(i*pi/16),.27+.30*sin(i*pi/16),0) for i in range(17)],[.026]*17,wood)
for i in range(13):
    a=i*2.4;r=.19*math.sqrt(i/13)
    oval("Picked blueberry",(r*cos(a),.29+.025*cos(i),r*.72*sin(a)),(.072,.067,.07),blues[i%3],12,8)

# The travellers carry a working trail satchel on their saved outfits.
strap=mat("Lantern satchel • oiled leather","8C6046")
saddle=mat("Lantern satchel • waxed ochre canvas","B58552")
paper=mat("Lantern satchel • folded map","E9D4A4")
turq=mat("Moon bird • blue glaze","528D92")
nightwing=mat("Lantern moth • plum velvet","795C83")
silk=mat("Lantern moth • petal silk","D9B4CF")
glimmer=mat("Lantern moth • lamplight","F8CF89")
part("traveller-kit")
rope("Cross-body leather strap",[(.23,1.22,-.13),(.25,1.28,.02),(.20,1.19,.215),(.10,1.01,.279),
     (-.03,.88,.286),(-.18,.72,.241),(-.31,.60,.14)],[.034]*7,strap,8)
rope("Back of shoulder strap",[(.23,1.22,-.13),(.14,1.01,-.222),(-.09,.79,-.25),(-.28,.62,-.15)],
     [.033]*4,strap,8)
cube("Rounded explorer satchel",xyz((-.32,.62,.025)),(.225,.31,.25),saddle,.045)
cube("Saddle leather flap",xyz((-.33,.704,.051)),(.240,.315,.11),strap,.035)
cube("Brass hasp",xyz((-.366,.632,.183)),(.076,.026,.074),brass,.011)
for i in range(6):
    oval("Saddle stitch",(-.417+i*.035,.697,.205),(.010,.005,.004),paper,8,6)
rope("Rolled map",[(-.41,.753,-.10),(-.30,.762,-.10),(-.206,.77,-.10)],[.051]*3,paper,12)
rope("Map ribbon",[(-.335,.759,-.10),(-.32,.76,-.10)],[.055]*2,turq,12)
oval("Trail pin",(.145,1.085,.264),(.040,.041,.015),brass,14,10)
oval("Enamel centre",(.145,1.085,.279),(.024,.025,.008),turq,14,8)

# Rumle wears the ceramic bird on a cord. The tail is the mouthpiece.
current=body
rope("Bird whistle cord",[(-.62,3.52,.64),(-.55,3.14,.93),(-.23,2.61,1.02),
     (.02,2.45,1.04),(.28,2.73,1.015),(.58,3.17,.91),(.65,3.51,.67)],[.023]*7,strap,8)
part("rumle-whistle")
oval("Bird whistle belly",(0,.04,0),(.26,.20,.19),turq,24,16)
oval("Bird whistle head",(-.15,.18,.01),(.157,.146,.145),turq,22,14)
rope("Whistle tail mouthpiece",[(.18,.08,-.05),(.32,.15,-.13),(.39,.22,-.21)],[.105,.076,.054],turq,14)
rope("Glazed beak",[(-.26,.17,.02),(-.38,.145,.03)],[.07,.012],brass,12)
for side in (-1,1):
    oval("Bird eye",(-.19,.21,side*.132),(.025,.027,.012),M["Pupils"],12,8)
    oval("Etched wing",(.045,.045,side*.165),(.144,.087,.021),leaflight,18,12)
for xx,zz in [(-.08,.06),(.02,-.03),(.11,.04)]:
    oval("Finger hole",(xx,.229,zz),(.032,.009,.029),M["Smile interior"],14,8)

# Three silhouettes can be recognised and played by touch independently of colour.
for tone,(label,hue,h) in enumerate([("sun","CA9C73",.71),("leaf","7E9E8A",.97),("moon","958BAF",1.17)]):
    part("echo-"+label)
    shale=mat("Singing stone • "+label,hue)
    ring_surface("Weathered bell stone",[(.008,.42,.32),(.09,.49,.36),(.23,.46,.345),
                 (h*.73,.34-.02*tone,.29),(h*.94,.26-.04*tone,.22),(h,.15-.03*tone,.11),
                 (h+.018,.004,.004)],shale,13)
    facez=.28; cy=h*.54
    if tone==0:
        oval("Sun medallion",(0,cy,facez),(.107,.107,.04),brass,18,10)
        for j in range(8):
            a=j*pi/4
            rope("Sun ray",[(cos(a)*.13,cy+sin(a)*.13,facez),(cos(a)*.178,cy+sin(a)*.178,facez-.005)],[.017,.013],rim,7)
    elif tone==1:
        oval("Leaf medallion",(.014,cy,facez),(.093,.170,.027),rim,18,10)
        rope("Carved leaf vein",[(-.04,cy-.14,facez+.032),(.018,cy+.035,facez+.034),(.036,cy+.125,facez+.030)],[.012]*3,turq,7)
    else:
        rope("Crescent medallion",[(.055+.144*cos((i*.84/12+.55)*pi*2),cy+.16*sin((i*.84/12+.55)*pi*2),facez) for i in range(13)],
             [.014+.028*sin(i*pi/12) for i in range(13)],rim,10)
    for j in range(tone+1):
        oval("Resonance hole",((j-tone/2)*.105,.18,.34),(.036,.046,.012),M["Smile interior"],12,8)
part("echo-moth")
oval("Velvet moth body",(0,.015,0),(.080,.068,.205),nightwing,18,12)
oval("Downy collar",(0,.040,.095),(.103,.088,.102),rim,16,10)
for side in (-1,1):
    rope("Feathery feeler",[(side*.042,.075,.15),(side*.083,.13,.22),(side*.127,.15,.25)],
         [.014,.011,.005],brass,8)
    for j in range(4):
        rope("Antenna filament",[(side*(.068+j*.016),.105+j*.009,.18+j*.013),
              (side*(.071+j*.018),.14+j*.011,.154+j*.014)],[.005,.002],rim,5)
part("echo-wing")
wingpoints=[(.025,.05,.14),(.13,.095,.22),(.31,.15,.31),(.445,.134,.295),(.478,.108,.25),
            (.373,.08,.065),(.318,.076,-.022),(.357,.083,-.106),(.298,.067,-.16),(.233,.06,-.18),
            (.187,.05,-.341),(.149,.045,-.192),(.079,.045,-.133),(.025,.045,-.11)]
center=(.164,.122,.041)
sheet("Silk with a scalloped edge",wingpoints+[center],[(len(wingpoints),j,(j+1)%len(wingpoints)) for j in range(len(wingpoints))],silk)
for j in (2,4,6,8,10):
    rope("Fine wing vein",[(.049,.061,.012),center,wingpoints[j]],[.008,.006,.003],nightwing,7)
oval("Lantern eye spot",(.29,.135,.142),(.070,.012,.050),nightwing,16,8)
oval("Luminous pupil",(.29,.148,.142),(.042,.010,.034),glimmer,16,8)

# Bake soft contact shadow into the host around the nose, chin, hair and clothes.
# A common full-detail occluder gives the two detail levels the same shading.
ao_points=[];ao_faces=[]
for group in (body,eyes,gaze):
    for ob in group.objects:
        base=len(ao_points);matx=ob.matrix_basis
        ao_points.extend(matx@v.co for v in ob.data.vertices)
        ao_faces.extend(tuple(base+i for i in face.vertices) for face in ob.data.polygons)
occlusion=BVHTree.FromPolygons(ao_points,ao_faces)
def ambient(p,n):
    side=n.cross(Vector((0,0,1)) if abs(n.z)<.82 else Vector((0,1,0))).normalized()
    tangent=n.cross(side)
    start=p+n*.018
    shade=0
    for a,b in [(.72,.39),(-.72,-.39),(.39,-.72),(-.39,.72)]:
        hit=occlusion.ray_cast(start,(n*.52+side*a+tangent*b).normalized(),.8)[3]
        if hit is not None: shade+=.079*(1-hit/.8)
    return 1-shade

# The extra paint is baked into the published meshes, so both detail levels share it.
def tint(o,p,base):
    x,y,z=p # Blender coordinates
    if o.name.startswith(("Rumle • continuous body sculpt","Upper arm","Shoulder cap","Tapered forearm","Elbow","Palm","Finger","Thumb","Wrist","Rosy pear nose","Nose bridge")):
        stone=.018*sin(x*18+y*6+z*11)*cos(y*12-z*7)+.026*sin(x*3.2+y*.7+z*1.8)
        blush=0
        if o.name.startswith("Rumle • continuous"):
            blush=math.exp(-((abs(x)-.83)/.38)**2-((z-4.15)/.27)**2)*min(1,max(0,-y))
        return (base[0]+.027+stone+blush*.10,base[1]-.012+stone-blush*.065,base[2]-.008+stone-blush*.05)
    if o.name.startswith("Soft split-front"):
        fleck=.026*sin(z*61+cos(x*34))+.018*sin(x*37+y*53+z*23)
        return tuple(c+fleck for c in base)
    return base

def export(name, objects, low=False):
    data=bytearray()
    for obj in objects:
        if obj.type!="MESH": continue
        temp=None
        frond=obj.name.startswith("Curled moss frond")
        if low and (len(obj.data.polygons)>150 or frond):
            temp=obj.copy(); scene.collection.objects.link(temp)
            d=temp.modifiers.new("Handheld mesh","DECIMATE"); d.ratio=.58 if frond else (.30 if name=="troll" else .38); d.use_collapse_triangulate=True
            deps=bpy.context.evaluated_depsgraph_get()
            ev=temp.evaluated_get(deps); m=ev.to_mesh()
        else: m=obj.data
        m.calc_loop_triangles()
        matrix=obj.matrix_basis
        normal_matrix=matrix.to_3x3().inverted().transposed()
        cache={}
        for face in m.loop_triangles:
            material=m.materials[face.material_index]
            base=tuple(material.get("game_color",[.7,.7,.7]))
            for vi in face.vertices:
                key=(vi,face.material_index, -1 if m.polygons[face.polygon_index].use_smooth else face.polygon_index)
                if key not in cache:
                    v=m.vertices[vi]; p=matrix@v.co; n=normal_matrix@(v.normal if key[2]==-1 else face.normal);n.normalize()
                    c=tint(obj,p,base)
                    if name in {"troll","rumle-eyes","rumle-gaze"}:
                        shade=ambient(p,n); c=(c[0]*shade,c[1]*shade,c[2]*(.07+.93*shade))
                    cache[key]=struct.pack("<3f4b4B",p.x,p.z,-p.y,*(round(t*127) for t in (n.x,n.z,-n.y)),0,
                                           *(max(0,min(255,round(t*255))) for t in c),255)
                data.extend(cache[key])
        if temp:
            ev.to_mesh_clear();bpy.data.objects.remove(temp,do_unlink=True)
    name=name+("-low" if low else "")
    path=DEST/(name+".hbg.gz")
    path.write_bytes(gzip.compress(struct.pack(">4sII",b"HBG2",len(data)//20,0)+data,compresslevel=7,mtime=0))
    return dict(name=name,triangles=len(data)//60,gpu_bytes=len(data),asset_bytes=path.stat().st_size,
                format="HBG2",sha256=hashlib.sha256(path.read_bytes()).hexdigest())

manifest_path=DEST/"manifest.json"
manifest=json.loads(manifest_path.read_text(encoding="utf-8"))
for name,group in parts.items():
    for low in ([False,True] if name in {"troll","arm"} else [False]):
        entry=export(name,group.objects,low)
        key=("mobile_meshes" if low else "meshes") if name in {"troll","arm"} else "props"
        rows=manifest.setdefault(key,[])
        for idx,old in enumerate(rows):
            if old["name"]==entry["name"]:
                rows[idx]=entry;break
        else: rows.append(entry)
        print(entry["name"],entry["triangles"])
manifest_path.write_text(json.dumps(manifest,indent=2),encoding="utf-8")
# Stage collection instances in their assembled rest pose. Source coordinates remain local to each bone.
from mathutils import Matrix
def assemble_preview():
    for col in parts.values():
        scene.collection.children.unlink(col)
    def instance(key, location, rotation=0, scale=(1,1,1)):
        ob=bpy.data.objects.new(key, None)
        scene.collection.objects.link(ob);ob.instance_type="COLLECTION";ob.instance_collection=parts[key]
        ob.location=xyz(location);ob.rotation_euler[1]=rotation;ob.scale=(scale[0],scale[2],scale[1])
        return ob
    for key in ("troll","rumle-eyes","rumle-gaze"):
        instance(key,(0,0,0))
    angle=math.radians(-143)
    grip=Vector((1.86,1.50,1.07))
    wrist=grip+Vector((.05*cos(angle)+.29*sin(angle),.05*sin(angle)-.29*cos(angle),-.16))
    shoulder=Vector((1.46,3.01,.31));axis=(wrist-shoulder).normalized()
    d=(wrist-shoulder).length;along=(1.30**2-1.50**2+d*d)/(2*d)
    pole=Vector((1.25,-.60,-.45)); elbow=shoulder+axis*along+(pole-axis*axis.dot(pole)).normalized()*math.sqrt(1.30**2-along**2)
    for key,a,b in [("arm",shoulder,elbow),("rumle-forearm",elbow,wrist)]:
        ob=instance(key,a)
        delta=Vector(xyz(b-a));ob.rotation_euler=delta.to_track_quat('Z','Y').to_euler()
        ob.scale.z=delta.length
    instance("rumle-hand-open",grip,-angle)
    for key in ("berry-bush","berry-fruit"):
        instance(key,(-2.40,.02,1.90),scale=(1.5,1.5,1.5))
    instance("berry-basket",(-1.50,.02,2.18))
    instance("berry-cup",(1.35,.02,2.10),scale=(1.5,1.5,1.5))
    instance("rumle-whistle",(0,2.39,1.12))
    # Keep the alternative grip mesh in the file even though the resting pose uses an open hand.
    for source_part in parts.values(): source_part.use_fake_user=True
    scene.world=bpy.data.worlds.new("Soft daylight");scene.world.color=(.32,.37,.43)
    ground=bpy.data.meshes.new("Studio floor")
    ground.from_pydata([(-30,-30,-.08),(30,-30,-.08),(30,30,-.08),(-30,30,-.08)],[],[(0,1,2,3)])
    go=bpy.data.objects.new("Studio floor",ground);scene.collection.objects.link(go)
    ground.materials.append(mat("Rumle • studio backdrop","788582"))
    def aim(ob,point):
        ob.rotation_euler=(Vector(point)-ob.location).to_track_quat('-Z','Y').to_euler()
    cam=bpy.data.cameras.new("Three-quarter portrait");co=bpy.data.objects.new(cam.name,cam);scene.collection.objects.link(co)
    co.location=(3.2,-13.0,5.20);aim(co,(0,0,2.65));cam.lens=41;scene.camera=co
    for name,p,energy,size,color in [("Window",(-5,-6,9),1150,6,(1,.85,.69)),("Sky",(4,-3,6),770,5,(.74,.86,1)),("Rim",(1,4,8),1450,4,(1,.83,.60))]:
        light=bpy.data.lights.new(name,"AREA");light.energy=energy;light.shape="DISK";light.size=size;light.color=color
        ob=bpy.data.objects.new(name,light);scene.collection.objects.link(ob);ob.location=p;aim(ob,(0,0,2.5))
    scene.render.engine="CYCLES";scene.cycles.samples=24;scene.cycles.use_denoising=True
    scene.render.resolution_x=900;scene.render.resolution_y=800;scene.render.resolution_percentage=100
assemble_preview()
bpy.data.libraries.write(str(HERE/"Rumle.blend"),{scene,*parts.values()},compress=True)
print("Rumle and the berry outing exported")
