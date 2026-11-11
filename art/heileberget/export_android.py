"""Export authored Blender meshes as batched, baked mobile geometry.

HBG1: magic, big-endian vertex count, lighting mode, little-endian float32
vertices (position3, normal3, colour3). pack_mobile_assets.py converts the final
exports to HBG2. Android's asset merge unpacks gzip before packaging the APK.
AO and sun visibility are baked here, never ray-traced on the phone.
"""
import bpy,gzip,struct,math,json,pathlib,time,hashlib
from mathutils import Vector
from mathutils.bvhtree import BVHTree
from mathutils import noise
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
DEST=ROOT/'app/src/main/assets/heileberget';DEST.mkdir(parents=True,exist_ok=True)
def srgb(v):return v*12.92 if v<=.0031308 else 1.055*v**(1/2.4)-.055
def collect(name):
    return [o for o in bpy.data.collections[name].objects if o.type=='MESH']
def export(name,collection,mode=1,cave=False):
    started=time.time();objects=collect(collection);points=[];polygons=[];records=[]
    for o in objects:
        m=o.data;m.calc_loop_triangles();base=len(points)
        # Source collections can be hidden/excluded in the presentation scenes. Root objects'
        # matrix_basis remains current even before a hidden dependency graph has evaluated them.
        matrix=o.matrix_basis if o.parent is None else o.matrix_world
        ps=[matrix@v.co for v in m.vertices];points.extend(ps)
        for tri in m.loop_triangles:polygons.append(tuple(base+k for k in tri.vertices))
        records.append((o,m,ps,matrix))
    tree=BVHTree.FromPolygons(points,polygons,all_triangles=True,epsilon=.0001) if mode!=2 else None
    sun=Vector((-.5,-.4,.85)).normalized();dirs=[Vector(d).normalized() for d in [(1,1,2),(-1,1,2),(1,-1,2),(-1,-1,2),(0,0,1)]]
    # Bake actual visible lantern positions into the mobile vertices. Broad fill
    # keeps the route readable; smaller amber pools and blue crystals give rooms depth.
    lanterns=[o.matrix_basis.translation.copy() for o in objects if o.name.startswith('Lantern pane')] if cave else []
    data=bytearray();count=0
    for o,m,ps,matrix in records:
        albedo=m.color_attributes.get('Albedo');normalmatrix=matrix.to_3x3().inverted().transposed()
        cache={}
        for tri in m.loop_triangles:
            material=m.materials[tri.material_index] if len(m.materials)>tri.material_index else None
            baseColor=tuple(material.get('game_color',[.7,.7,.7])) if material else (.7,.7,.7)
            emission=float(material.get('emission',0)) if material else 0
            for j,vi in enumerate(tri.vertices):
                normal=normalmatrix@(m.vertices[vi].normal if m.polygons[tri.polygon_index].use_smooth else tri.normal);normal.normalize()
                if o.name.startswith('Winding ochre trail') and normal.z<0:normal=-normal
                key=(vi,tri.material_index,tuple(round(n,3) for n in normal))
                if key not in cache:
                    p=ps[vi];color=tuple(srgb(v) for v in albedo.data[vi].color[:3]) if albedo and albedo.domain=='POINT' else baseColor
                    if mode==1:
                        origin=p+normal*.06;occluded=0
                        for d in dirs:
                            if d.dot(normal)<0:d=-d
                            hit=tree.ray_cast(origin,d,2.6)[0]
                            if hit is not None:occluded+=1
                        ao=1-occluded*.06
                        sunlight=0 if tree.ray_cast(origin,sun,60)[0] is not None else max(0,normal.dot(sun))
                        if o.name.startswith(('Meadow •','Fine heather grasses')):
                            # Thin leaves transmit daylight. Baking every neighbouring
                            # blade as an opaque blocker made the tufts nearly black.
                            ao=max(.9,ao);sunlight=max(.35,abs(normal.dot(sun))*.75)
                        if cave:
                            hearth=math.exp(-((p.x+17)**2+(p.y-5.7)**2+(p.z-1.5)**2*.5)/52)
                            room=max(math.exp(-((p.x+3)**2+(p.y-4)**2+(p.z-3)**2*.3)/150),math.exp(-((p.x-11)**2+(p.y-22)**2+(p.z-6)**2*.4)/135))
                            cool=math.exp(-((p.x+14)**2+(p.y-24)**2+(p.z-2)**2*.3)/65)
                            glow=0.0
                            for source in lanterns:
                                delta=source-p;dist=delta.length
                                if dist<9:
                                    facing=max(.25,normal.dot(delta/max(.01,dist)))
                                    glow+=math.exp(-dist*dist/17)*facing*.42
                            hemisphere=.04+.10*max(0,normal.z)+.035*normal.x
                            light=(.45+hemisphere+.25*room+.48*hearth+glow*1.2+.04*cool,
                                   .48+hemisphere+.18*room+.28*hearth+glow*.73+.18*cool,
                                   .58+hemisphere+.06*room+.06*hearth+glow*.26+.28*cool)
                        else:light=(.64+sunlight*.39,.69+sunlight*.33,.79+sunlight*.21)
                        color=tuple(min(1,c*(a*ao if emission<1 else 1.13)) for c,a in zip(color,light))
                        if material and material.name=='Old gold':
                            # Warm polished metal keeps its gold colour under blue cave fill.
                            # The small baked highlight costs no extra mobile shader work.
                            keylight=Vector((-.3,-.7,.65)).normalized()
                            facing=max(0,normal.dot(keylight));glint=facing**14*.24
                            color=tuple(min(1,c*(.73+.20*facing)*(1-(1-ao)*.45)+glint*g)
                                        for c,g in zip(baseColor,(1,.87,.53)))
                    cache[key]=(p.x,p.z,-p.y,normal.x,normal.z,-normal.y,*color)
                data.extend(struct.pack('<9f',*cache[key]));count+=1
    raw=struct.pack('>4sII',b'HBG1',count,mode)+data
    path=DEST/(name+'.hbg.gz')
    with gzip.open(path,'wb',compresslevel=7) as f:f.write(raw)
    result={'name':name,'triangles':count//3,'gpu_bytes':len(data),'asset_bytes':path.stat().st_size,'seconds':round(time.time()-started,2),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
    print(result,flush=True);return result
results=[]
for args in [('outside','Heileberget • landscape',1,False),('cave','Inside the mountain',1,True),('troll','Rumle • sculpt',0,False),('arm','Rumle • waving hand',0,False),('sky','Sky • painted atmosphere',2,False)]:results.append(export(*args))
(DEST/'manifest.json').write_text(json.dumps({'version':1,'coordinate_system':'X right, Y up, Z forward','source':'Blender 5.2; original authored art/heileberget scripts','meshes':results},indent=2),encoding='utf-8')
print('ANDROID_ASSETS_EXPORTED')
