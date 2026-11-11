"""Export articulated family and alternate eyelids. One topology for phone and tablet."""
import ast,pathlib,bpy,json
ROOT=pathlib.Path('C:/topa/.gradle-tmp/rainbow-finale')
tree=ast.parse((ROOT/'art/heileberget/export_android.py').read_text(encoding='utf-8'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,(ast.Import,ast.ImportFrom,ast.FunctionDef))],type_ignores=[]),'export helpers','exec'))
DEST=ROOT/'app/src/main/assets/heileberget'
results=[]
for who in ['eira','eilev','oyvind']:
    for part in ['body','arm-l','arm-r','leg-l','leg-r','eyes-open','eyes-closed']:
        prefix='Family • ' if who=='oyvind' else 'Children • '
        results.append(export(who+'-'+part,prefix+who+'-'+part,0))
manifest=json.loads((DEST/'manifest.json').read_text(encoding='utf-8'));manifest['characters']=results
(DEST/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print('ARTICULATED_CHARACTERS_EXPORTED')
