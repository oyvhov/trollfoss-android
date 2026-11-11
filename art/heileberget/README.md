# Heileberget – Blender source

Original art authored in Blender 5.2.1 through the local MCP for Blender server, at the user's
explicit request. No downloaded asset packs, paid generation service, or private reference photo
is included. `Heileberget.blend` has three editable scenes: **Heileberget**, **Trollhola**,
and **Eira og Eilev** (articulated models and a 25-frame walk preview).
The user's original unsaved Blender process was preserved; authoring used a separate process.

The `.blend` is the reviewable source. The scripts record the successive art passes:

1. `build_scene.py` creates original geometry and materials in a separately owned factory scene.
2. `setup_preview.py` organises source collections and GPU preview settings.
3. `refine_scene.py` paints continuous vertex colours, vegetation, sky and sculpt details.
4. `preview_lighting.py` fixes sky visibility and lighting.
5. `cave_interior.py` creates the 40 × 42 metre interior and its contents.
6. `final_landscape.py` sculpts granite massifs and deepens the lake.
7. `finish_cave.py` closes the cavern envelope and paints interior surfaces.
8. `export_android.py` bakes AO/lighting and exports regular meshes.
9. `export_low_detail.py` derives smaller mobile meshes. It must never move the authored art.
10. `presentation.py` creates the two named presentation scenes.
11. `characters.py` creates Eira and Eilev as five pivoted parts each. Their soft oval faces replace
    the initial cheek/chin sculpt after the user's feedback. `export_characters.py` exports them.
12. `harmony_pass.py` unifies the sage/lilac/honey palette, opens up the sky, adds domestic detail,
    meadow flowers and hares, and corrects the gallery's ramp support. Then rerun all three exports.
13. `ridges_and_clouds.py` replaces the block massifs and flat cloud volumes with continuous
    granite, rounded plateaus and true horizon ranges. This replacement pass can be rerun.
14. `export_hare.py` extracts a reusable hare from the authored scenery and leaves Blender
    preview instances. Run after the regular exporter; it also records the creature in the manifest.
15. `oyvind.py` creates an articulated adult with smaller head proportions, fitted trimmed beard,
    dusty rose shirt and smooth short hair. It can be rerun independently of the children.
16. `portrait_normals.py` checks hair-cap normals and flips only inward-facing caps. Idempotent.
17. `pack_mobile_assets.py` converts exports to the compact HBG2 vertex format and updates the manifest.
18. `birches_and_shore.py` replaces the angular leaf cards with rounded birch sprays and adds flat
    skipping pebbles. It relocates one tree to leave the actual throwing view open. Idempotent.
19. `entrance_and_lookout.py` embeds the entrance in a continuous granite spur and moves the cairn
    to the west ledge, with a walking path and a clear fjord view. Idempotent. Keep the outside
    navigation/camera clearance in `Highlands.kt` aligned with this spur.
20. `rumle_smile.py` restores a rounded, non-overlapping lip cross-section after the old planar
    smile adjustment. Idempotent; it does not replace the sculpt or the teeth.
21. `bird.py` models a woodland bird and its pivoted wing. `export_bird.py` exports both parts;
    the renderer mirrors the wing and animates it. Idempotent.
22. `terrain_skirt.py` joins the exact half-metre edge of the playable plateau to the distant
    valley. The inner overlap is 8 mm below the main surface, avoiding gaps and z-fighting.
23. `singing_crystals.py` creates three playable clusters and stone plinths, with separate
    collection instances for animation. `export_crystals.py` appends their manifest entries.
24. `soft_hair_and_shoulder.py` replaces Eilev's bulky radial locks with a continuous softly
    waved cap and shallow crests, and rounds Rumle's articulated shoulder. Idempotent.
25. `granite_planes.py` replaces the distant escarpment with broad fractured planes and local
    shelves, using fewer triangles. `cavern_stone.py` paints minerals and reshapes the pillars.
26. `stone_and_shoulder.py` unions the shoulder into the waving arm and softens the cairn stones.
    It retains an unlinked original arm mesh so repeating this pass does not erode the fingers.
    Run this after `soft_hair_and_shoulder.py` if rebuilding that pass.
27. `daylight_colour.py` brings clearer blue air and richer meadow greens without added geometry.
    The plateau and skirt use the same colour function at their shared edge.
28. `gold_pan.py` models a shallow enamel bowl with rolled rim and inner riffles. The separate
    source collection lets Android rock it during washing. `export_gold_pan.py` appends the prop.
29. `worn_paths.py` adds earth variation, grass-softened edges and small worn pebbles, retaining
    the route centres and an unlinked original ribbon for deterministic reruns. Round 18 also
    moves the birch in front of gold washing, in `birches_and_shore.py`.
    Round 26 tapers and blends the path ends into the meadow without changing the walking surface.
30. `treasury_gems.py` gives the cave minerals faceted shafts, shorter tips and cut gems.
    It retains the original dimensions and an unlinked source mesh for safe reruns.
31. `family_blinks.py` separates each family's original eyes into a nested collection and
    models relaxed closed eyelid curves. Blender previews retain the open eyes; Android
    switches between exported alternatives briefly and respects reduced motion.
32. `meadow_grass.py` replaces scattered flat leaves with curved, coloured tufts and
    clears the actual path ribbons. Original anchors are saved for deterministic reruns.
    Main tufts retain their authored topology on mobile; the small secondary batch is omitted.
33. `family_complexion.py` paints a subtle peach/rose gradient directly onto Eira/Eilev's
    existing smooth face mesh. It adds no cheek spheres or other facial geometry.
34. `butterfly.py` models a soft blue butterfly with separate body and pivoted wing collections.
    `export_butterfly.py` appends the creature after the hare export. Android mirrors/flaps the
    wing and brings the butterfly to the free left hand during a voluntary flower-patch greeting.

35. `rumle_workshop.py` opens its own scene in the original mountain file. It builds Rumle's
    seated clothing, face and posed hand variants, the guest's trail satchel, the kettle outing props,
    three echo stones, the lantern moth and the bird ocarina. It bakes vertex colours and contact
    shading and exports its own HBG2 meshes, including the two troll detail levels. It also writes
    the separately editable `Rumle.blend` alongside the script. Run this pass last from a fresh
    session with the original `Heileberget.blend` open.

For a fresh export of the finished mountain and family assets: `export_android.py`, `export_low_detail.py`,
`portrait_normals.py`, `export_characters.py`, `export_hare.py`, `export_butterfly.py`, `export_bird.py`, `export_crystals.py`, `export_gold_pan.py`,
then `pack_mobile_assets.py`, followed by `rumle_workshop.py` for the visit and music assets.
Regular export resets the manifest; later exports append the mobile meshes, family and creature metadata.

These are versioned **one-time art passes**, not idempotent commands to repeatedly apply to the
finished file. To rebuild, start a separate factory scene and run them in this order. Output paths
are explicit local authoring paths. No script opens, replaces or saves the user's unrelated scene.

Android loads the original geometry from `app/src/main/assets/heileberget`. Coordinates map from
Blender `(x,y,z)` to game `(x,z,-y)`. The walkable height function and cave bounds must stay aligned
with `domain/Highlands.kt`. Props do not define the navigation height field.

Both formats have a 12-byte header (magic, big-endian vertex count, lighting mode).
Intermediate `HBG1` stores nine little-endian float32 values per vertex. Final `HBG2` retains exact
float32 XYZ positions, then normal XYZ as three normalized signed bytes plus padding, and colour RGB
as three normalized unsigned bytes plus padding: 20 bytes instead of 36. Packing verifies positions
bit-for-bit and checks the quantization error. Source files use gzip; Android's asset merge unpacks
`.gz` and stores `.hbg` in the APK. Runtime supports both versions and uploads HBG2 directly without
an intermediate float array; the staging buffer is released after GPU upload. This saves 44.4% of
vertex storage for identical geometry; it is not a measured claim of faster FPS on real devices.

The Android scenery uses baked vertex lighting and a lightweight water shader. Children and Rumle
use smooth directional lighting without per-vertex AO speckles. Blender stills use
Cycles and have softer lighting/reflections; they are explicitly not Android screenshots.
Mobile topology is a separate export from the same models. The app does not require Blender,
MCP, a server, or an internet connection to play.

Character rest pivots in game coordinates: shoulders `(±0.33,1.16,0)`, hips `(±0.145,0.60,0)`.
The Kotlin renderer swings these parts while walking. Look/ID and carried items are retained.
Named Eira/Eilev/Øyvind with their standard looks use the authored models, scaled to their saved heights.
Workshop edits currently use the generic renderer so a fixed model never silently erases them.
