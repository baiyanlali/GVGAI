import java.util.Random;
import java.util.TreeSet;
import java.util.ArrayList;

import core.game.Event;
import core.game.Game;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;
import tracks.ArcadeMachine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PlayVGDL {

    public static class VGDLResultCompatible {
        public int npcs;
        public double gameScore;
        public int gameTick;
        public Types.WINNER winner;
        public boolean isGameOver;
        public TreeSet<Event> historyEvents;
        public ArrayList<Vector2d>[] avatarSpritesPositions;

        public VGDLResultCompatible(StateObservation stateObs, Game game) {
            npcs = stateObs.getNoPlayers();
            gameScore = stateObs.getGameScore();
            gameTick = stateObs.getGameTick();
            winner = stateObs.getGameWinner();
            isGameOver = stateObs.isGameOver();
            historyEvents = stateObs.getEventsHistory();
            avatarSpritesPositions = game.avatarPositionHistory;
        }

    }

    public static String PlayVGDL(String vgdl, String level){
		String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
		String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
		String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
		String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";

		String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
        String sampleRSController = "tracks.singlePlayer.advanced.sampleRS.Agent";
        String sampleRHEAController = "tracks.singlePlayer.advanced.sampleRHEA.Agent";
		String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";

		int seed = new Random().nextInt();


		String recordActionsFile = null;
		var genGame = ArcadeMachine.runOneGameStr(vgdl, level, false, sampleMCTSController, recordActionsFile, seed, 0);
        var obs = genGame.getObservation();

        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .create();


        var result = new VGDLResultCompatible(obs, genGame);
        String jsonResult = gson.toJson(result);
        
        System.out.println("Game finished with JSON result: " + jsonResult);
//        System.out.println(result.toString());
        return jsonResult;
    }

    public static void main(String[] args) {

        String vgdl = """
BasicGame square_size=32
    SpriteSet
        background > Immovable img=oryx/space1 hidden=True
        base    > Immovable    color=WHITE img=oryx/planet
        avatar  > FlakAvatar   stype=sam img=oryx/spaceship1
        missile > Missile
            sam  > orientation=UP    color=BLUE singleton=True img=oryx/bullet1
            bomb > orientation=DOWN  color=RED  speed=0.5 img=oryx/bullet2
        alien   > Bomber       stype=bomb   prob=0.01  cooldown=3 speed=0.8
            alienGreen > img=oryx/alien3
            alienBlue > img=oryx/alien1
        portal  > invisible=True hidden=True
        	portalSlow  > SpawnPoint   stype=alienBlue  cooldown=16   total=20 img=portal
        	portalFast  > SpawnPoint   stype=alienGreen  cooldown=12   total=20 img=portal

    LevelMapping
        . > background
        0 > background base
        1 > background portalSlow
        2 > background portalFast
        A > background avatar

    TerminationSet
        SpriteCounter      stype=avatar               limit=0 win=False
        MultiSpriteCounter stype1=portal stype2=alien limit=0 win=True

    InteractionSet
        avatar  EOS  > stepBack
        alien   EOS  > turnAround
        missile EOS  > killSprite

        base bomb > killBoth
        base sam > killBoth scoreChange=1

        base   alien > killSprite
        avatar alien > killSprite scoreChange=-1
        avatar bomb  > killSprite scoreChange=-1
        alien  sam   > killSprite scoreChange=2
""";;
        String level = """
1.............................
000...........................
000...........................
..............................
..............................
..............................
..............................
....000......000000.....000...
...00000....00000000...00000..
...0...0....00....00...00000..
................A.............""";

        PlayVGDL(vgdl, level);

    }
}
