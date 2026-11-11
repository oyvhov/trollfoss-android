package app.trollfoss.ui.berg

import app.trollfoss.domain.Residents

/** Blender-authored family share the saved identity and proportions of their dollhouse portraits. */
internal object BergCharacters {
    fun model(appearance:BergAppearance):String? {
        val standard=when(appearance.name) {
            "Eira" -> Residents.eiraLook()
            "Eilev" -> Residents.eilevLook()
            "Øyvind" -> Residents.oyvindLook()
            else -> return null
        }
        // A workshop edit must remain visible; don't silently replace it with a fixed portrait.
        if(appearance.look.copy(height=standard.height)!=standard) return null
        return if(appearance.name=="Øyvind") "oyvind" else appearance.name.lowercase()
    }
    fun load(appearance:BergAppearance,art:BergAssets):BergAvatar {
        val name=model(appearance)
        if(name==null) return BergAvatar(BergArt.person(appearance.look,appearance.carried),BergArt.legs(appearance.look),
            listOf(-1f,1f).map { side -> BergLimb(BergArt.arm(appearance.look,side),side*.37f,1.05f,side,true) },
            eyesOpen=BergArt.eyes(appearance.look,false),eyesClosed=BergArt.eyes(appearance.look,true),
            scarf=trailScarf(appearance,false))
        return BergAvatar(art.mesh("$name-body"),null,listOf(
            BergLimb(art.mesh("$name-arm-l"),-.33f,1.16f,-1f,true),
            BergLimb(art.mesh("$name-arm-r"),.33f,1.16f,1f,true),
            BergLimb(art.mesh("$name-leg-l"),-.145f,.60f,-1f,false),
            BergLimb(art.mesh("$name-leg-r"),.145f,.60f,1f,false),
        ),if(appearance.carried.isEmpty()) null else BergArt.accessories(appearance.carried),
            art.mesh("$name-eyes-open"),art.mesh("$name-eyes-closed"),trailScarf(appearance,true))
    }

    fun blinking(time:Float,id:Int,motion:Boolean):Boolean {
        if(!motion) return false
        val phase=(time+(id%11)*.83f)%(4.1f+(id%5)*.37f)
        return phase<.14f
    }
}

internal data class BergLimb(val mesh:BergMesh,val x:Float,val y:Float,val side:Float,val arm:Boolean)
internal class BergAvatar(val body:BergMesh,val leg:BergMesh?,val limbs:List<BergLimb> = emptyList(),val accessories:BergMesh?=null,
    val eyesOpen:BergMesh?=null,val eyesClosed:BergMesh?=null,val scarf:BergMesh) {
    private fun meshes()=listOfNotNull(body,leg,accessories,eyesOpen,eyesClosed,scarf)+limbs.map {it.mesh}
    fun upload() {meshes().forEach {it.upload()}}
    fun release() {meshes().forEach {it.release()}}
}
