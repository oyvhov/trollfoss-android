"""Idempotently orient hair-cap normals away from the head before mobile export."""
import bpy,bmesh
from mathutils import Vector
for colname,capname,centre in [('Children • eira-body','Sculpted hair cap',(0,.018,1.565)),('Children • eilev-body','Sculpted hair cap',(0,.018,1.565)),('Family • oyvind-body','Oyvind continuous short hair',(0,.012,1.60))]:
    col=bpy.data.collections.get(colname)
    if not col:continue
    for ob in col.objects:
        if not ob.name.startswith(capname):continue
        radial=sum(p.normal.dot(p.center-Vector(centre)) for p in ob.data.polygons)
        if radial<0:
            bm=bmesh.new();bm.from_mesh(ob.data);bmesh.ops.reverse_faces(bm,faces=list(bm.faces));bm.to_mesh(ob.data);bm.free();ob.data.update()
            print('Corrected outward normals:',ob.name)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
