"""A continuous softly waved hair silhouette and a rounded articulated troll shoulder.

Idempotent finishing pass: preserves all faces, clothes, animation pivots and materials.
Run in the owned Blender session after characters.py and harmony_pass.py.
"""
import bpy, ast, pathlib, math
from math import sin, cos, pi
from mathutils import Vector

ROOT = pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree = ast.parse((ROOT/'art/heileberget/build_scene.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n, ast.FunctionDef)], type_ignores=[]), 'helpers', 'exec'))
scene = bpy.data.scenes['Eira og Eilev']
bpy.context.window.scene = scene
current = bpy.data.collections['Children • eilev-body']
for ob in list(current.objects):
    if ob.name.startswith(('Sculpted hair cap', 'Loose crown wave', 'Wavy fringe', 'Eilev • soft wave')):
        bpy.data.objects.remove(ob, do_unlink=True)
hair = bpy.data.materials['Eilev • light brown']

def surface(a, t, inset=0):
    # The modest continuous ripple replaces twelve separate sausage-shaped locks.
    ripple = .010*sin(7*a+2.4*t)*sin(t)**.7 + .005*sin(3*a-2*t)*sin(t)
    r = 1 + ripple/.36
    return Vector(((.358-inset)*r*sin(t)*cos(a),
                   (.299-inset)*r*sin(t)*sin(a)+.018,
                   1.565+(.419-inset)*cos(t)))

vertices = []; faces = []; sides = 40; rows = 14
for j in range(rows+1):
    for i in range(sides):
        a = i*2*pi/sides
        end = 1.80-.84*max(0, -sin(a)) + .035*sin(5*a)*max(0, -sin(a))
        vertices.append(surface(a, .008+(end-.008)*j/rows))
for j in range(rows):
    for i in range(sides):
        a = j*sides+i; b = j*sides+(i+1)%sides
        faces.append((a, a+sides, b+sides, b))
cap = mesh('Sculpted hair cap', vertices, faces, hair, True)
assert sum(p.normal.dot(p.center-Vector((0,.018,1.565))) for p in cap.data.polygons) > 0

# Shallow swept crests sit partly inside the cap; only the soft ridge is exposed.
for j in range(9):
    base = -pi + (j+.5)*pi/9
    end = 1.10 + .43*abs(cos(base))
    points = []; radii = []
    for k in range(19):
        u = k/18; t = .17+(end-.17)*u
        a = base + .13*sin(u*pi*1.55) + .07*(1-u)
        points.append(surface(a, t, .008))
        radii.append(.003 + .023*sin(pi*u)**.7)
    tube('Eilev • soft wave', points, radii, hair, 8)

# A ball-shaped shoulder remains round as the existing arm pivots to wave.
# It is one small overlapping surface, avoiding a destructive resculpt of the hand.
current = bpy.data.collections['Rumle • waving hand']
for ob in list(current.objects):
    if ob.name.startswith('Rumle • rounded shoulder'):
        bpy.data.objects.remove(ob, do_unlink=True)
# Link the source temporarily so Blender's transform operator can apply the scale.
linked = current.name not in scene.collection.children
if linked: scene.collection.children.link(current)
ell('Rumle • rounded shoulder', (0, 0, -.015), (.575, .545, .53),
    bpy.data.materials['Warm granite skin'], 24, 16)
if linked: scene.collection.children.unlink(current)
bpy.ops.wm.save_as_mainfile(filepath='C:/topa/dist/heileberget-blender/Heileberget.blend')
print('SOFT_CONTINUOUS_EILEV_HAIR_AND_ROUNDED_RUMLE_SHOULDER')
