package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test

class BergEchoTest {
    private fun visit(cave:Boolean=false):HighlandsWalk {
        val p=BergEcho.site(cave)
        return HighlandsWalk(HighlandsState().apply {this.cave=cave;x=p.x;z=p.z;leader=19})
    }
    @Test fun aChildsRhythmAndNotesAreAnsweredInOrderAcrossRoomsAndSaving() {
        val world=WorldFactory.create()
        val s=world.highlands
        s.x=BergEcho.outside.x;s.z=BergEcho.outside.z;s.bag.add(BergItem.GOLD);s.held=BergItem.GOLD
        val walk=HighlandsWalk(s);assertEquals(BergEvent.LISTEN,walk.use())
        for((i,key) in listOf(2,0,0,1).withIndex()) {
            repeat(listOf(0,9,18,12)[i]){walk.step(.05f)}
            assertEquals(BergEvent.STONE_NOTE,walk.playEcho(key))
        }
        assertEquals(listOf(3,3,6,4),s.song.map {it.beats})
        val restored=WorldStore.decode(WorldStore.encode(world,Settings())).world.highlands
        restored.cave=true;restored.x=BergEcho.inside.x;restored.z=BergEcho.inside.z
        val next=HighlandsWalk(restored);next.use()
        assertEquals(BergEvent.REPLY,next.echo.answer());assertEquals(BergEvent.NONE,next.echo.answer())
        val heard=mutableListOf<Pair<Int,Int>>()
        repeat(130){tick->if(next.step(.04f)==BergEvent.FLUTE_NOTE) heard+=tick to next.echo.tone}
        assertEquals(listOf(2,0,0,1),heard.map {it.second})
        assertEquals(.45,(heard[1].first-heard[0].first)*.04,.045)
        assertEquals(.90,(heard[2].first-heard[1].first)*.04,.045)
        assertEquals(0f,next.echo.replyLength,.001f)
        assertEquals(BergItem.GOLD,restored.held);assertEquals(setOf(BergItem.GOLD),restored.bag)
    }

    @Test fun instrumentRespondsToRepeatsAndStopsForWalkingPauseAndANewLeader() {
        val walk=visit(true)
        walk.use();assertEquals(BergEvent.NONE,walk.echo.answer())
        repeat(12){walk.playEcho(it%3)}
        assertEquals(BergEcho.LIMIT,walk.state.song.size)
        walk.echo.answer();repeat(25){walk.step(.04f)}
        walk.playEcho(1);assertEquals(0f,walk.echo.replyLength,.001f)
        walk.leadEcho(EchoLight(999f,991f,993f))
        val p=walk.echo.guide!!
        assertTrue(p.x<=BergEcho.inside.x+7f && p.y<12f && p.z<=BergEcho.inside.z+4f)
        walk.state.leader=20;walk.step(.04f)
        assertFalse(walk.echo.active);assertNull(walk.echo.guide)
        assertEquals(BergEvent.NONE,walk.playEcho(0))
        walk.use();walk.echo.answer();walk.state.z=10f;walk.step(.04f)
        assertFalse(walk.echo.active);assertEquals(BergEcho.LIMIT,walk.state.song.size)
        walk.state.z=BergEcho.inside.z;walk.use();walk.stop()
        assertFalse(walk.echo.active);assertEquals(0f,walk.echo.replyLength,.001f)
    }

    @Test fun aFinishedSongBringsTheMothToTheSatchelAndRumleRemembersOnTheNextVisit() {
        val world=WorldFactory.create()
        val s=world.highlands
        s.x=BergEcho.outside.x;s.z=BergEcho.outside.z
        val walk=HighlandsWalk(s)
        walk.use();walk.playEcho(2);walk.echo.answer()
        assertFalse(s.mothFriend)
        val events=List(85){walk.step(.04f)}
        assertEquals(1,events.count {it==BergEvent.FRIEND})
        assertTrue(s.mothFriend)
        walk.echo.close();walk.callFriend();repeat(35){walk.step(.04f)}
        assertTrue(walk.friendReach>.95f)
        walk.forward=1f;walk.step(.05f);assertFalse(walk.friendHand)
        val again=WorldStore.decode(WorldStore.encode(world,Settings())).world.highlands
        again.cave=true;again.x=-3f;again.z=-1f
        val returned=HighlandsWalk(again)
        assertEquals(BergEvent.REPLY,returned.use())
        assertEquals(1,List(80){returned.step(.04f)}.count {it==BergEvent.FLUTE_NOTE})
        assertEquals(listOf(2),again.song.map {it.tone})
        assertTrue(again.mothFriend)
    }

    @Test fun bothSitesAreReachedOnFootAndMalformedSavedNotesAreBounded() {
        val world=WorldFactory.create()
        for(cave in listOf(false,true)) {
            val walk=HighlandsWalk(HighlandsState().apply {this.cave=cave;x=0f;z=if(cave)10f else 18f})
            val spot=BergEcho.site(cave)
            walk.walkTo(BergPoint(spot.x,spot.z));repeat(850){walk.step(.04f)}
            assertEquals(spot,walk.nearby())
            for(i in 0..2) {val stone=BergEcho.stone(cave,i);assertFalse(Highlands.walkable(stone.x,stone.z,cave))}
        }
        val json=WorldStore.encode(world,Settings())
        json.getJSONObject("highlands").put("echoSong",JSONArray().apply {
            repeat(11){ put(JSONArray(listOf(1,99))) };put(JSONArray(listOf(-1,3)));put("old")
        })
        val saved=WorldStore.decode(json).world.highlands
        assertEquals(4,saved.song.size);assertTrue(saved.song.all {it==EchoNote(1,7)})
        val walk=visit();walk.use();walk.playEcho(0);walk.echo.clearSong()
        assertTrue(walk.state.song.isEmpty());assertEquals(BergEvent.NONE,walk.echo.answer())
    }
}
