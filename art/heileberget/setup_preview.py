import bpy
from mathutils import Vector
s=bpy.context.scene
for c in list(s.collection.children):
    if c.name.startswith('Rumle'):
        s.collection.children.unlink(c) # referenced by collection instances, editable in asset browser
c=bpy.data.collections.get('Inside the mountain');c.hide_viewport=True;c.hide_render=True
for area in bpy.context.screen.areas:
    if area.type=='VIEW_3D':
        area.spaces.active.overlay.show_overlays=False
        area.spaces.active.shading.type='MATERIAL'
        area.spaces.active.region_3d.view_perspective='CAMERA'
prefs=bpy.context.preferences.addons['cycles'].preferences
prefs.compute_device_type='OPTIX';prefs.get_devices()
for d in prefs.devices:d.use=d.type!='CPU'
s.cycles.device='GPU';s.cycles.samples=32
s.render.filepath='C:/topa/dist/heileberget-blender/landscape-v1.png'
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('Visible Heileberget scene; devices',[(d.name,d.use) for d in prefs.devices])
def render_preview():
    bpy.ops.render.render(write_still=True)
    return None
bpy.app.timers.register(render_preview,first_interval=1)
