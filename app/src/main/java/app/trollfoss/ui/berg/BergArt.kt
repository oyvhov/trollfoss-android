package app.trollfoss.ui.berg

import app.trollfoss.domain.*
import kotlin.math.*
import kotlin.random.Random

/** Original, procedural 3D art. The concept PNG is documentation, never a scene backdrop. */
internal object BergArt {
    private const val ROCK=0x99939E
    private const val GRASS=0x82A857
    private const val WOOD=0xB98253
    private const val BIRCH=0xE6E3CF
    private const val INK=0x303244

    fun landscape(cave: Boolean): BergMesh = BergMeshBuilder().apply {
        val range=if(cave) -14..14 else -32..32
        val zRange=if(cave) -16..16 else -32..28
        val random=Random(if(cave) 51 else 37)
        for(z in zRange.first until zRange.last) for(x in range.first until range.last) {
            fun v(dx:Int,dz:Int)=V3((x+dx).toFloat(),Highlands.height((x+dx).toFloat(),(z+dz).toFloat(),cave),(z+dz).toFloat())
            fun color(p:V3):Int {
                if(cave) return if(p.x>3f) 0xB79A80 else 0xA78E78
                val a=if(p.y<1.12f) 0xB4B099 else 0x88A95B
                val t=((sin(p.x*.22f)+cos(p.z*.25f)+sin((p.x+p.z)*.16f))*.36f+.25f).coerceIn(0f,1f)
                val b=0xAAA5A6
                fun mix(shift:Int)=(((a shr shift and 255)*(1f-t)+(b shr shift and 255)*t).toInt() shl shift)
                return mix(16) or mix(8) or mix(0)
            }
            ground(v(0,0),v(0,1),v(1,1),v(1,0),::color)
        }
        if(cave) {
            // Tall outer walls leave the near doorway open; the camera remains inside the cavern.
            for(i in -3..3) {
                sphere(-16f,5f,i*5f,3.6f,9f,4.5f,0x53606B,10,7)
                sphere(16f,5f,i*5f,3.6f,9f,4.5f,0x656375,10,7)
                sphere(i*5f,6f,-18f,4f,10f,3.8f,0x4E5366,10,7)
            }
            for(i in -2..2) sphere(i*6f,16.5f,-5f,5f,3f,14f,0x424B5D,10,6)
            for(i in 0..9) {
                val x=-11.5f+random.nextFloat()*23f; val z=-13f+random.nextFloat()*25f
                if(x>4f || z< -8f) gem(x,Highlands.height(x,z,true),z,.4f+random.nextFloat()*.7f)
            }
            // Sloping terrace joins the lower floor at z=8. Its rail follows the very same heightfield.
            for(z in -12..7 step 2) {
                val h=Highlands.floor(3.2f,z.toFloat(),true)
                beam(V3(3.2f,h,z.toFloat()),V3(3.2f,h+1f,z.toFloat()),.08f,WOOD)
                val next=Highlands.floor(3.2f,z+2f,true)
                beam(V3(3.2f,h+.85f,z.toFloat()),V3(3.2f,next+.85f,z+2f),.065f,WOOD)
            }
            val bedY=Highlands.floor(8f,-11f,true)
            box(8f,bedY,-11f,4f,.5f,2.5f,WOOD)
            box(8f,bedY+.5f,-11f,3.9f,.35f,2.4f,0xB86383)
            sphere(8f,bedY+.9f,-11.6f,1.4f,.2f,.4f,0xF4DEC2)
            for(i in 0..2) {
                val y=1f+i*.95f
                box(-10f,y,-4.5f,3.8f,.14f,1f,WOOD)
                for(j in 0..3) cake(-11.2f+j*.8f,y+.15f,-4.5f,.48f)
            }
            // Colourful jars and wrapped sweets beside the cake pantry.
            for(level in 0..2) {
                val y=.55f+level*.85f
                box(-10f,y,1.7f,3.8f,.13f,1f,WOOD)
                for(j in 0..3) {
                    val x=-11.2f+j*.8f
                    sphere(x,y+.28f,1.7f,.29f,.27f,.27f,0xD8CBAD,10,6)
                    sphere(x,y+.49f,1.7f,.3f,.07f,.29f,0xA96758,10,5)
                    for(k in 0..5) sphere(x+cos(k*1.9f)*.19f,y+.25f+(k%2)*.12f,1.93f,.07f,.07f,.04f,listOf(0xE9999A,0xBBD05F,0xE9BE59,0x9ABCD3)[(j+k)%4],6,4)
                }
            }
            // Uneven stone paving, a patterned rug and small mushrooms soften the big room.
            for(z in -12..12 step 2) for(x in -11..11 step 2) {
                val xx=x+random.nextFloat()*.5f;val zz=z+random.nextFloat()*.5f
                val y=Highlands.floor(xx,zz,true)+.012f
                sphere(xx,y,zz,.7f,.012f,.65f,if((x+z)%4==0) 0xB19A83 else 0xAD967E,7,3)
            }
            box(-3f,.02f,1.3f,4.3f,.02f,2.6f,0xA65F63)
            for(i in -4..4) box(-3f+i*.45f,.05f,1.3f,.07f,.012f,2.5f,0xDEB875)
            for(i in 0..3) {
                val x=6f+i
                val y=Highlands.floor(x,-6f,true)
                box(x,y,-6f,.85f,.6f,1.3f,WOOD)
                for(j in 0..2) bottle(x,y+.6f,-6.4f+j*.4f,i>=2)
            }
            for(i in 0..7) lantern(-11f+(i%4)*7.2f,6f+(i%2),-12f+(i/4)*16f)
            box(-9f,0f,-10.5f,3.2f,.85f,1.7f,0x8D582F)
            box(-9f,.82f,-11.3f,3.2f,1.2f,.16f,WOOD)
            for(i in 0..20) sphere(-10.3f+random.nextFloat()*2.6f,.9f+random.nextFloat()*.25f,-11f+random.nextFloat(),.25f,.16f,.23f,0xF3C357,8,5)
            for(i in 0..4) gem(-10.4f+i*.6f,1.08f,-10.4f,.4f)
            // A real exit corridor with daylight at its far end.
            box(-3f,0f,14.5f,1f,7f,3f,ROCK);box(3f,0f,14.5f,1f,7f,3f,ROCK)
            box(0f,7f,15f,7f,2f,3f,ROCK)
            for(i in 0..5) tree(-7f+i*3f,17f+(i%2)*3f,.8f)
        } else {
            // Broad fjord and layered, rounded cliff ridges rather than pointed Alpine mountains.
            quad(V3(-180f,-1f,-180f),V3(-180f,-1f,-28f),V3(180f,-1f,-28f),V3(180f,-1f,-180f),0x6B9AC1)
            for(i in -6..6) {
                sphere(i*17f,3f,-70f-(i%3)*8f,20f,11f+(i%3),8f,0x8294AA,10,6)
            }
            // Continuous striated cliff faces: no detached grass caps or stacks of spheres.
            for(i in -12..12) {
                fun ridge(column:Int,row:Int):V3 {
                    val xx=column*4f
                    val crest=(13f+sin(column*.7f)*1.4f+cos(column*.3f))*((xx+28f)/35f).coerceIn(.1f,1f)
                    return when(row) {
                        0 -> V3(xx,3.5f+sin(column*.5f),-24f+sin(column*1.2f))
                        1 -> V3(xx,crest*.72f,-29f+sin(column*.8f))
                        2 -> V3(xx,crest,-34f+sin(column*.9f)*1.5f)
                        else -> V3(xx,7f,-44f)
                    }
                }
                for(row in 0..2) quad(ridge(i,row),ridge(i+1,row),ridge(i+1,row+1),ridge(i,row+1),if(row==2) 0x899279 else 0xA5A0A3)
            }
            // Small irregular ledges and birch groves, with broad empty walking surfaces between them.
            repeat(70) {
                val x=-29f+random.nextFloat()*58f;val z=-25f+random.nextFloat()*50f
                val h=Highlands.height(x,z)
                if(h>1.2f && !Highlands.bridge(x,z) && Highlands.spots.none { hypot(it.x-x,it.z-z)<3f }) {
                    if(it%3==0) tree(x,z,.6f+random.nextFloat()*.6f)
                    else sphere(x,h-.12f,z,.45f+random.nextFloat(),.3f+random.nextFloat()*.6f,.55f+random.nextFloat(),ROCK,8,5)
                }
            }
            for(i in -26..16) {
                val x=i*.5f;val y=Highlands.floor(x,1f,false)
                box(x,y-.13f,1f,.47f,.18f,1.8f,0xC99D6B)
                if(i%4==0) for(side in listOf(-1f,1f)) {
                    beam(V3(x,y,1f+side*.88f),V3(x,y+.9f,1f+side*.88f),.07f,WOOD)
                    beam(V3(x,y+.65f,1f+side*.88f),V3(x+2f,Highlands.floor(x+2f,1f,false)+.65f,1f+side*.88f),.05f,WOOD)
                }
            }
            val cy=Highlands.height(13f,-15f)
            // Faceted round opening, with a tall ridge wrapped behind it.
            for(i in 0..11) {
                val a=i*PI.toFloat()/6;val b=(i+1)*PI.toFloat()/6
                triangle(V3(13f,cy+2.7f,-17f),V3(13f+cos(a)*3.8f,cy+2.7f+sin(a)*3.7f,-17f),V3(13f+cos(b)*3.8f,cy+2.7f+sin(b)*3.7f,-17f),0x333C4F)
            }
            for(i in 0..8) {
                val a=i*PI.toFloat()/8
                sphere(13f+cos(a)*4.4f,cy+sin(a)*6f,-15.8f,1.1f,1.2f,1.3f,ROCK,10,6)
            }
            for(i in 0..14) {
                val x=7f+(i%5)*3.2f;val z=-20f-(i/5)*2.4f
                sphere(x,Highlands.height(x,z)+1.5f,z,2.3f,2f+(i%3)*.5f,2f,if(i%3==0) 0x9EA18D else 0xAAA4A4,9,6)
            }
            lantern(9f,cy+3f,-13f);lantern(17f,cy+3f,-13f)
            cabin(-18f,11f)
            tree(-3f,1f,.6f)
            // Cairn and flowering moss at the approach.
            for(i in 0..4) sphere(6f,Highlands.height(6f,18f)+i*.2f,18f,.55f-i*.07f,.14f,.4f-i*.04f,ROCK,9,5)
            // Clear sandy routes drawn above the same ground, without constraining free walking.
            path(listOf(BergPoint(0f,20f),BergPoint(7f,14f),BergPoint(12f,6f),BergPoint(14f,-4f),BergPoint(13f,-12f)),1.2f)
            path(listOf(BergPoint(14f,-4f),BergPoint(20f,-10f),BergPoint(20f,-17f),BergPoint(17f,-21f)),1f)
            path(listOf(BergPoint(0f,20f),BergPoint(-12f,13f),BergPoint(-18f,11f)),1f)
            val sy=Highlands.height(-9f,8f)
            sphere(-9f,sy+.55f,8f,.65f,.65f,.65f,0x777D8A,8,6)
            gem(-9f,sy+1.1f,8f,.25f,0x344C65)
            val gy=Highlands.height(-12f,3f)
            sphere(-12f,gy+.1f,3f,.8f,.12f,.8f,0x555568,12,5)
            for(i in 0..3) sphere(-12.3f+i*.2f,gy+.22f,3f,.13f,.1f,.13f,0xEAC15E,8,4)
            val lp=Highlands.lookout;val ly=Highlands.height(lp.x,lp.z)
            beam(V3(lp.x,ly,lp.z),V3(lp.x,ly+1.7f,lp.z),.1f,WOOD)
            beam(V3(lp.x,ly+1.7f,lp.z+.4f),V3(lp.x,ly+1.9f,lp.z-.5f),.2f,0xCCA969)
            for(i in 0..2) goat(20f-i*3f,-9f-i*4f)
            // Tiny scatter is baked into the mesh; it does not create per-frame objects.
            repeat(180) {
                val x=-23f+random.nextFloat()*46f;val z=-22f+random.nextFloat()*45f
                val h=Highlands.height(x,z)
                if(h>1.2f && !Highlands.bridge(x,z)) {
                    val color=if(it%3==0) 0xDAC3DB else 0xC4CA85
                    beam(V3(x,h,z),V3(x+.08f,h+.24f,z),.025f,0x618F55)
                    sphere(x+.08f,h+.24f,z,.08f,.06f,.08f,color,5,3)
                }
            }
            repeat(1100) {
                val x=-23f+random.nextFloat()*46f;val z=-22f+random.nextFloat()*45f
                val h=Highlands.height(x,z)
                if(h>1.2f && !Highlands.bridge(x,z) && sin(x*.8f+z*.5f)>.1f) {
                    val s=.1f+random.nextFloat()*.15f
                    triangle(V3(x-s,h,z),V3(x,h+s,z+.02f),V3(x+s,h,z),if(it%3==0) 0xB3B86B else 0x73944D)
                    triangle(V3(x,h,z-s),V3(x+.04f,h+s,z),V3(x,h,z+s),0x8DAA59)
                }
            }
        }
    }.build()

    private fun BergMeshBuilder.path(points:List<BergPoint>,width:Float) {
        for(i in 0 until points.lastIndex) {
            val a=points[i];val b=points[i+1];val length=hypot(a.x-b.x,a.z-b.z)
            val dx=(b.z-a.z)/length*width;val dz=-(b.x-a.x)/length*width
            val steps=ceil(length*2).toInt()
            for(s in 0 until steps) {
                fun v(t:Float,side:Float):V3 { val x=a.x+(b.x-a.x)*t+dx*side;val z=a.z+(b.z-a.z)*t+dz*side;return V3(x,Highlands.floor(x,z,false)+.025f,z) }
                quad(v(s.toFloat()/steps,-1f),v((s+1f)/steps,-1f),v((s+1f)/steps,1f),v(s.toFloat()/steps,1f),0xBFB08D)
            }
        }
    }
    private fun BergMeshBuilder.tree(x:Float,z:Float,scale:Float) {
        val y=Highlands.height(x,z)
        beam(V3(x,y,z),V3(x+.15f,y+3.2f*scale,z),.12f*scale,BIRCH)
        for(i in 0..3) {
            val a=i*2.4f;val xx=x+cos(a)*.65f*scale;val zz=z+sin(a)*.65f*scale
            beam(V3(x,y+1.6f*scale,z),V3(xx,y+2.8f*scale,zz),.045f,BIRCH)
            sphere(xx,y+(2.8f+i*.18f)*scale,zz,.85f*scale,.85f*scale,.7f*scale,if(i%2==0) 0x9BB651 else 0x789B51,9,6)
        }
        for(i in 0..3) box(x,y+.4f+i*.5f*scale,z+.12f*scale,.18f*scale,.04f,.02f,INK)
    }
    private fun BergMeshBuilder.cabin(x:Float,z:Float) {
        val y=Highlands.height(x,z)
        box(x,y,z,4f,2.8f,3.5f,0xA95142)
        box(x,y,z+1.76f,1f,2.2f,.1f,WOOD)
        for(side in listOf(-1f,1f)) { box(x+side*1.3f,y+1.1f,z+1.8f,.7f,.8f,.1f,0xF1CC83);box(x+side*1.3f,y+1.1f,z+1.87f,.06f,.8f,.04f,BIRCH) }
        quad(V3(x-2.4f,y+2.7f,z-2f),V3(x-2.4f,y+2.7f,z+2f),V3(x,y+4f,z+2f),V3(x,y+4f,z-2f),0x6E8D50)
        quad(V3(x,y+4f,z-2f),V3(x,y+4f,z+2f),V3(x+2.4f,y+2.7f,z+2f),V3(x+2.4f,y+2.7f,z-2f),GRASS)
        box(x+1f,y+3f,z-1f,.5f,1.5f,.6f,ROCK)
    }
    private fun BergMeshBuilder.lantern(x:Float,y:Float,z:Float) {
        beam(V3(x,y+1.2f,z),V3(x+.3f,y+.6f,z),.06f,WOOD)
        box(x+.3f,y,z,.34f,.6f,.34f,0xFFDA83)
        box(x+.3f,y+.6f,z,.48f,.12f,.48f,WOOD)
    }
    private fun BergMeshBuilder.goat(x:Float,z:Float) {
        val y=Highlands.height(x,z)
        sphere(x,y+.75f,z,.65f,.4f,.36f,0xE2DACA)
        sphere(x+.48f,y+1.05f,z,.28f,.34f,.28f,0xEEE3CF)
        for(dx in listOf(-.35f,.35f)) for(dz in listOf(-.23f,.23f)) beam(V3(x+dx,y,z+dz),V3(x+dx,y+.65f,z+dz),.065f,0x968A7D)
        for(dz in listOf(-.16f,.16f)) beam(V3(x+.48f,y+1.3f,z+dz),V3(x+.2f,y+1.6f,z+dz),.05f,0x81766E)
        sphere(x+.66f,y+1.1f,z+.2f,.045f,.055f,.035f,INK,6,4)
    }
    private fun BergMeshBuilder.bottle(x:Float,y:Float,z:Float,urge:Boolean) {
        sphere(x,y+.3f,z,.16f,.3f,.16f,if(urge) 0xBED445 else 0x3B3041,10,6)
        box(x,y+.15f,z,.31f,.2f,.31f,if(urge) 0xF3D358 else 0x283C63)
        beam(V3(x,y+.5f,z),V3(x,y+.7f,z),.07f,if(urge) 0x4E9258 else 0xD95345)
    }
    private fun BergMeshBuilder.cake(x:Float,y:Float,z:Float,s:Float) {
        sphere(x,y+s*.35f,z,s*.6f,s*.35f,s*.6f,0xBD8257,12,6)
        sphere(x,y+s*.65f,z,s*.61f,s*.13f,s*.61f,0xF7E2BB,12,5)
        for(i in 0..4) sphere(x+cos(i*1.3f)*s*.38f,y+s*.8f,z+sin(i*1.3f)*s*.38f,s*.1f,s*.1f,s*.1f,0xCF6A70,6,4)
    }

    fun troll()=BergMeshBuilder().apply {
        val skin=0x9C9990
        sphere(0f,2f,0f,1.75f,2f,1.25f,skin,18,12)
        sphere(0f,4.05f,.1f,1.5f,1.4f,1.2f,skin,18,12)
        for(side in listOf(-1f,1f)) {
            sphere(side*1.2f,.45f,1.1f,.8f,.45f,1f,skin)
            if(side<0) sphere(side*1.6f,2.1f,.5f,.55f,1f,.65f,skin)
            sphere(side*1.47f,4.15f,.1f,.5f,.65f,.3f,skin)
            sphere(side*.51f,4.24f,1.32f,.23f,.26f,.14f,0xECE4D2)
            sphere(side*.51f,4.22f,1.45f,.1f,.13f,.06f,INK,10,6)
            sphere(side*.5f,4.63f,1.26f,.4f,.14f,.16f,0x6C7751)
        }
        sphere(0f,4f,1.38f,.62f,.46f,.45f,0xBC9F91)
        sphere(0f,3.45f,1.29f,.67f,.30f,.13f,0x51414B)
        sphere(0f,3.64f,1.4f,.53f,.1f,.04f,0xEEE3CA)
        sphere(0f,3.3f,1.41f,.35f,.09f,.035f,0xC88D88)
        for(i in 0..46) {
            val a=i*2.4f;val r=.2f+(i%5)*.29f
            sphere(cos(a)*r,5.14f-(i%3)*.11f,sin(a)*r,.31f,.24f,.29f,if(i%2==0) 0x77964A else 0xA2B360,8,5)
        }
        for(i in 0..4) sphere(-1f+i*.5f,1.6f,1.17f,.12f,.08f,.06f,0xB2ADA0,6,4)
    }.build()

    fun trollArm()=BergMeshBuilder().apply {
        sphere(0f,-.5f,0f,.55f,1f,.6f,0x9C9990)
        sphere(0f,-1.2f,.13f,.58f,.5f,.57f,0x9C9990)
        for(i in 0..3) sphere(-.32f+i*.22f,-1.4f,.5f,.14f,.26f,.17f,0xA6A196,8,5)
    }.build()
    fun bird()=BergMeshBuilder().apply {
        sphere(0f,0f,0f,.15f,.13f,.3f,0x4C718B,8,5)
        sphere(0f,.15f,.15f,.12f,.12f,.12f,0xF5DC98,8,5)
        triangle(V3(-.07f,.13f,.24f),V3(.07f,.13f,.24f),V3(0f,.13f,.43f),0xC79246)
        sphere(-.3f,0f,0f,.3f,.045f,.13f,0x5E849B,8,4)
        sphere(.3f,0f,0f,.3f,.045f,.13f,0x5E849B,8,4)
    }.build()
    fun panWater()=BergMeshBuilder().apply {
        for(i in 0 until 32) {
            val a=i*2f*PI.toFloat()/32;val b=(i+1)*2f*PI.toFloat()/32
            triangle(V3(0f,.105f,0f),V3(cos(b)*.56f,.105f,sin(b)*.56f),V3(cos(a)*.56f,.105f,sin(a)*.56f),0x5B9292)
        }
    }.build()

    fun shadow()=BergMeshBuilder().apply {
        quad(V3(-.65f,0f,-.5f),V3(-.65f,0f,.5f),V3(.65f,0f,.5f),V3(.65f,0f,-.5f),0xFFFFFF)
    }.build()

    fun person(look:Look, carried:List<Pair<ThingType,Int>>):BergMesh = BergMeshBuilder().apply {
        val l=look.safe();val skin=Palette.skins[l.skin];val hair=Palette.hairs[l.hairColor]
        val shirt=Palette.cloth[l.topColor];val pants=Palette.cloth[l.bottomColor]
        sphere(0f,.78f,0f,.33f,.38f,.24f,shirt)
        sphere(0f,.42f,0f,.29f,.18f,.22f,pants)
        sphere(0f,1.47f,0f,.43f,.46f,.37f,skin,16,10)
        for(side in listOf(-1f,1f)) {
            sphere(side*.43f,1.44f,0f,.09f,.14f,.09f,skin,8,6)
            val eyeX=side*.16f*l.eyeSpacing
            beam(V3(eyeX-side*.068f,1.627f,.335f),V3(eyeX+side*.061f,1.649f,.319f),.014f,hair,8)
            sphere(side*.444f,1.44f,.066f,.047f,.077f,.026f,skin,10,6)
        }
        sphere(0f,1.38f,.39f,.09f,.07f,.065f,skin,8,5)
        for(i in 0..5) {
            fun lip(j:Int):V3 {val x=(j-3)*.042f;return V3(x,1.252f+x*x*1.8f,.337f-x*x*.60f)}
            beam(lip(i),lip(i+1),.014f,0x995E5B,8)
        }
        if(l.extra in listOf(1,2,3,4,9,10,11)) sphere(0f,1.18f,.12f,.35f,.18f,.29f,hair,12,7)
        if(l.hair!=0) {
            sphere(0f,1.79f,-.04f,.44f,.2f,.36f,hair,12,7)
            for(i in 0..7) {
                val a=i*PI.toFloat()/4
                sphere(cos(a)*.34f,1.78f+(i%2)*.04f,sin(a)*.27f,.16f,.15f,.14f,hair,8,5)
            }
            if(l.hair in listOf(4,5,9,10,14,17,19,20,23,24)) for(side in listOf(-1f,1f)) sphere(side*.33f,1.27f,-.13f,.17f,.45f,.19f,hair)
        }
        if(l.top==3 || l.pattern==8) for(i in -2..2) box(i*.1f,.55f,.232f,.035f,.5f,.015f,Palette.cloth[l.accent])
        box(0f,.7f,-.28f,.4f,.4f,.15f,0xB58C52)
        carriedAccessories(carried)
    }.build()

    fun accessories(carried:List<Pair<ThingType,Int>>)=BergMeshBuilder().apply {carriedAccessories(carried)}.build()
    private fun BergMeshBuilder.carriedAccessories(carried:List<Pair<ThingType,Int>>) {
        for((type,variant) in carried) {
            when(type.cat) {
                Cat.HAT -> { sphere(0f,1.88f,0f,.47f,.19f,.4f,Palette.cloth[variant.mod(Palette.cloth.size)]); if(type==ThingType.CAP) sphere(0f,1.81f,.36f,.4f,.06f,.2f,0x609BC5) }
                Cat.GLASSES -> for(side in listOf(-1f,1f)) sphere(side*.16f,1.5f,.405f,.12f,.12f,.025f,0x4B526B,10,6)
                else -> sphere(.48f,.68f,.2f,.13f,.16f,.13f,if(type.cat==Cat.FOOD) 0xDFA166 else 0x73A6D6,8,6)
            }
        }
    }

    fun eyes(look:Look,closed:Boolean)=BergMeshBuilder().apply {
        val l=look.safe()
        for(side in listOf(-1f,1f)) {
            val x=side*.16f*l.eyeSpacing
            val radius=.074f*l.eyeSize
            if(closed) {
                beam(V3(x-radius,1.515f,.351f),V3(x,1.499f,.366f),.011f,INK)
                beam(V3(x,1.499f,.366f),V3(x+radius,1.515f,.351f),.011f,INK)
            } else {
                sphere(x,1.515f,.341f,radius,radius*1.02f,.042f,0xFFF5E3,16,10)
                sphere(x,1.51f,.38f,radius*.61f,radius*.73f,.018f,Palette.eyes[l.eyeColor],14,8)
                sphere(x,1.512f,.396f,radius*.31f,radius*.47f,.009f,INK,12,8)
                sphere(x-.013f,1.534f,.405f,.011f,.012f,.006f,0xFFF8E3,8,6)
            }
        }
    }.build()

    fun arm(look:Look,side:Float)=BergMeshBuilder().apply {
        val l=look.safe()
        sphere(0f,-.23f,.03f,.12f,.30f,.12f,Palette.cloth[l.topColor])
        sphere(side*.03f,-.48f,.07f,.12f,.13f,.12f,Palette.skins[l.skin])
    }.build()

    fun legs(look:Look)=BergMeshBuilder().apply {
        val l=look.safe()
        sphere(0f,.22f,0f,.125f,.23f,.12f,Palette.cloth[l.bottomColor],10,6)
        sphere(0f,.035f,.073f,.151f,.042f,.224f,0x565456,14,8)
        sphere(0f,.084f,.07f,.15f,.09f,.22f,Palette.cloth[l.shoes],14,9)
        for(i in 0..1) beam(V3(-.063f,.146f,.10f+i*.045f),V3(.063f,.153f,.115f+i*.045f),.009f,0xE7DFCB,7)
    }.build()
    fun items(art:BergAssets,stations:BergBerryStations):Map<BergItem,BergMesh> {
        fun model(draw:BergMeshBuilder.()->Unit)=BergMeshBuilder().apply(draw).build().also { it.upload() }
        return mapOf(
            BergItem.DIAMOND to model { gem(0f,0f,0f,.65f) },
            BergItem.GOLD to model { sphere(0f,.2f,0f,.4f,.25f,.32f,0xEBC05C,8,5) },
            BergItem.CAKE to model { cake(0f,0f,0f,.7f) },
            BergItem.PEPSI to art.mesh("rumle-pepsi").also { it.upload() },
            BergItem.URGE to art.mesh("rumle-urge").also { it.upload() },
            BergItem.BERRIES to stations.basket,
            BergItem.BERRY_CUP to stations.cup,
        )
    }
    fun water()=BergMeshBuilder().apply {
        quad(V3(-14f,Highlands.WATER,-8f),V3(-14f,Highlands.WATER,10f),V3(8f,Highlands.WATER,10f),V3(8f,Highlands.WATER,-8f),0x51979D)
        quad(V3(-170f,-4f,-170f),V3(-170f,-4f,170f),V3(170f,-4f,170f),V3(170f,-4f,-170f),0x789EAF)
    }.build()
    /** Fixed celestial geometry: no particles, transparency sorting or per-frame allocations. */
    fun nightSky()=BergMeshBuilder().apply {
        val random=Random(4186)
        repeat(85) { i ->
            val angle=random.nextFloat()*2f*PI.toFloat()
            val elevation=.15f+random.nextFloat()*1.2f
            val p=V3(cos(angle)*cos(elevation)*155f,sin(elevation)*155f,sin(angle)*cos(elevation)*155f)
            val across=V3(-sin(angle),0f,cos(angle))
            val up=p.unit().cross(across)
            val r=if(i%7==0) .34f else .14f+random.nextFloat()*.12f
            fun point(x:Float,y:Float)=V3(p.x+across.x*x+up.x*y,p.y+up.y*y,p.z+across.z*x+up.z*y)
            val color=if(i%3==0) 0xD6E8F7 else 0xF4EDCE
            quad(point(-r,0f),point(0f,r),point(r,0f),point(0f,-r),color)
        }
        sphere(-87f,44f,-115f,5.5f,5.5f,5.5f,0xF1E6C8,24,14)
        // Small flat patches sit on the near face of the moon, in the same warm palette.
        sphere(-85.8f,44.4f,-110.1f,1.1f,1.3f,.10f,0xD8D4BF,12,8)
        sphere(-87.7f,42.9f,-109.9f,.65f,.75f,.10f,0xDFDCC6,12,8)
    }.build(lighting=2f)
    fun marker()=BergMeshBuilder().apply {
        for(i in 0..31) { val a=i*PI.toFloat()/16;val b=(i+1)*PI.toFloat()/16
            quad(V3(cos(a)*.55f,0f,sin(a)*.55f),V3(cos(a)*.68f,0f,sin(a)*.68f),V3(cos(b)*.68f,0f,sin(b)*.68f),V3(cos(b)*.55f,0f,sin(b)*.55f),0xF4D77D)
        }
    }.build()
    fun puff()=BergMeshBuilder().apply { sphere(0f,0f,0f,1f,1f,1f,0xA7C282,10,6) }.build()
    fun skippingStone()=BergMeshBuilder().apply {sphere(0f,0f,0f,.22f,.055f,.15f,0xC9CDC9,12,6)}.build()
    fun ripple()=BergMeshBuilder().apply {
        for(i in 0..39) {
            val a=i*PI.toFloat()/20;val b=(i+1)*PI.toFloat()/20
            quad(V3(cos(a)*.85f,0f,sin(a)*.85f),V3(cos(a),0f,sin(a)),V3(cos(b),0f,sin(b)),V3(cos(b)*.85f,0f,sin(b)*.85f),0xDCF5EC)
        }
    }.build(lighting=2f)
}
