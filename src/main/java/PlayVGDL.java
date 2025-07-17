import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.awt.Dimension;

import core.game.StateObservation;
import core.game.Observation;
import core.game.Event;
import ontology.Types;
import tools.Vector2d;
import tracks.ArcadeMachine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PlayVGDL {

    public static class VGDLResult {
        // Game state information
        public int gameState; // Types.GAMESTATES converted to int
        public double gameScore;
        public int gameTick;
        public int gameWinner; // Types.WINNER converted to int
        public boolean isGameOver;
        
        // World dimensions
        public int worldWidth;
        public int worldHeight;
        public int blockSize;
        
        // Avatar information
        public double avatarPositionX;
        public double avatarPositionY;
        public double avatarSpeed;
        public double avatarOrientationX;
        public double avatarOrientationY;
        public int avatarLastAction; // Types.ACTIONS converted to int
        public int avatarType;
        public int avatarHealthPoints;
        public int avatarMaxHealthPoints;
        public int avatarLimitHealthPoints;
        public boolean isAvatarAlive;
        
        // Avatar resources (flattened HashMap)
        public int[] resourceTypes;
        public int[] resourceAmounts;
        
        // Available actions (ArrayList<Types.ACTIONS> converted to int[])
        public int[] availableActions;
        
        // Events history (TreeSet<Event> flattened)
        public int[] eventGameSteps;
        public int[] eventActiveIds;
        public int[] eventPassiveIds;
        public double[] eventFromAvatarX;
        public double[] eventFromAvatarY;
        public double[] eventToAvatarX;
        public double[] eventToAvatarY;
        
        // Observation grid dimensions
        public int gridWidth;
        public int gridHeight;
        
        // NPC positions (flattened ArrayList<Observation>[])
        public int[] npcSpriteTypes;
        public int[] npcObsIds;
        public double[] npcPositionsX;
        public double[] npcPositionsY;
        public int[] npcItypes;
        public int[] npcCategories;
        
        // Immovable positions
        public int[] immovableSpriteTypes;
        public int[] immovableObsIds;
        public double[] immovablePositionsX;
        public double[] immovablePositionsY;
        public int[] immovableItypes;
        public int[] immovableCategories;
        
        // Movable positions
        public int[] movableSpriteTypes;
        public int[] movableObsIds;
        public double[] movablePositionsX;
        public double[] movablePositionsY;
        public int[] movableItypes;
        public int[] movableCategories;
        
        // Resource positions
        public int[] resourceSpriteTypes;
        public int[] resourceObsIds;
        public double[] resourcePositionsX;
        public double[] resourcePositionsY;
        public int[] resourceItypes;
        public int[] resourceCategories;
        
        // Portal positions
        public int[] portalSpriteTypes;
        public int[] portalObsIds;
        public double[] portalPositionsX;
        public double[] portalPositionsY;
        public int[] portalItypes;
        public int[] portalCategories;
        
        // From avatar sprite positions
        public int[] fromAvatarSpriteTypes;
        public int[] fromAvatarObsIds;
        public double[] fromAvatarPositionsX;
        public double[] fromAvatarPositionsY;
        public int[] fromAvatarItypes;
        public int[] fromAvatarCategories;

        public VGDLResult(StateObservation obs) {
            // Game state information
            this.gameState = obs.getGameState() != null ? obs.getGameState().ordinal() : -1;
            this.gameScore = obs.getGameScore();
            this.gameTick = obs.getGameTick();
            this.gameWinner = obs.getGameWinner() != null ? obs.getGameWinner().ordinal() : -1;
            this.isGameOver = obs.isGameOver();
            
            // World dimensions
            Dimension worldDim = obs.getWorldDimension();
            this.worldWidth = worldDim != null ? worldDim.width : 0;
            this.worldHeight = worldDim != null ? worldDim.height : 0;
            this.blockSize = obs.getBlockSize();
            
            // Avatar information
            Vector2d avatarPos = obs.getAvatarPosition();
            this.avatarPositionX = avatarPos != null ? avatarPos.x : 0.0;
            this.avatarPositionY = avatarPos != null ? avatarPos.y : 0.0;
            this.avatarSpeed = obs.getAvatarSpeed();
            
            Vector2d avatarOrientation = obs.getAvatarOrientation();
            this.avatarOrientationX = avatarOrientation != null ? avatarOrientation.x : 0.0;
            this.avatarOrientationY = avatarOrientation != null ? avatarOrientation.y : 0.0;
            
            this.avatarLastAction = obs.getAvatarLastAction() != null ? obs.getAvatarLastAction().ordinal() : -1;
            this.avatarType = obs.getAvatarType();
            this.avatarHealthPoints = obs.getAvatarHealthPoints();
            this.avatarMaxHealthPoints = obs.getAvatarMaxHealthPoints();
            this.avatarLimitHealthPoints = obs.getAvatarLimitHealthPoints();
            this.isAvatarAlive = obs.isAvatarAlive();
            
            // Avatar resources
            HashMap<Integer, Integer> resources = obs.getAvatarResources();
            if (resources != null && !resources.isEmpty()) {
                this.resourceTypes = new int[resources.size()];
                this.resourceAmounts = new int[resources.size()];
                int i = 0;
                for (HashMap.Entry<Integer, Integer> entry : resources.entrySet()) {
                    this.resourceTypes[i] = entry.getKey();
                    this.resourceAmounts[i] = entry.getValue();
                    i++;
                }
            } else {
                this.resourceTypes = new int[0];
                this.resourceAmounts = new int[0];
            }
            
            // Available actions
            ArrayList<Types.ACTIONS> actions = obs.getAvailableActions();
            if (actions != null) {
                this.availableActions = new int[actions.size()];
                for (int i = 0; i < actions.size(); i++) {
                    this.availableActions[i] = actions.get(i).ordinal();
                }
            } else {
                this.availableActions = new int[0];
            }
            
            // Events history
            TreeSet<Event> events = obs.getEventsHistory();
            if (events != null && !events.isEmpty()) {
                this.eventGameSteps = new int[events.size()];
                this.eventActiveIds = new int[events.size()];
                this.eventPassiveIds = new int[events.size()];
                this.eventFromAvatarX = new double[events.size()];
                this.eventFromAvatarY = new double[events.size()];
                this.eventToAvatarX = new double[events.size()];
                this.eventToAvatarY = new double[events.size()];
                
                int i = 0;
                for (Event event : events) {
                    this.eventGameSteps[i] = event.gameStep;
                    this.eventActiveIds[i] = event.activeTypeId;
                    this.eventPassiveIds[i] = event.passiveTypeId;
                    // this.eventFromAvatarX[i] = event.fromAvatar != false ? event.fromAvatar.x : 0.0;
                    // this.eventFromAvatarY[i] = event.fromAvatar != false ? event.fromAvatar.y : 0.0;
                    // this.eventToAvatarX[i] = event.toAvatar != false ? event.toAvatar.x : 0.0;
                    // this.eventToAvatarY[i] = event.toAvatar != false ? event.toAvatar.y : 0.0;
                    i++;
                }
            } else {
                this.eventGameSteps = new int[0];
                this.eventActiveIds = new int[0];
                this.eventPassiveIds = new int[0];
                this.eventFromAvatarX = new double[0];
                this.eventFromAvatarY = new double[0];
                this.eventToAvatarX = new double[0];
                this.eventToAvatarY = new double[0];
            }
            
            // Observation grid dimensions
            ArrayList<Observation>[][] grid = obs.getObservationGrid();
            this.gridWidth = grid != null ? grid.length : 0;
            this.gridHeight = (grid != null && grid.length > 0) ? grid[0].length : 0;
            
            // Helper method to flatten observation arrays
            this.flattenObservations(obs.getNPCPositions(), "npc");
            this.flattenObservations(obs.getImmovablePositions(), "immovable");
            this.flattenObservations(obs.getMovablePositions(), "movable");
            this.flattenObservations(obs.getResourcesPositions(), "resource");
            this.flattenObservations(obs.getPortalsPositions(), "portal");
            this.flattenObservations(obs.getFromAvatarSpritesPositions(), "fromAvatar");
        }
        
        private void flattenObservations(ArrayList<Observation>[] observations, String type) {
            if (observations == null) {
                setEmptyArrays(type);
                return;
            }
            
            // Count total observations
            int totalCount = 0;
            for (ArrayList<Observation> obsArray : observations) {
                if (obsArray != null) {
                    totalCount += obsArray.size();
                }
            }
            
            if (totalCount == 0) {
                setEmptyArrays(type);
                return;
            }
            
            // Create arrays
            int[] spriteTypes = new int[totalCount];
            int[] obsIds = new int[totalCount];
            double[] positionsX = new double[totalCount];
            double[] positionsY = new double[totalCount];
            int[] itypes = new int[totalCount];
            int[] categories = new int[totalCount];
            
            int index = 0;
            for (int spriteType = 0; spriteType < observations.length; spriteType++) {
                ArrayList<Observation> obsArray = observations[spriteType];
                if (obsArray != null) {
                    for (Observation obs : obsArray) {
                        spriteTypes[index] = spriteType;
                        obsIds[index] = obs.obsID;
                        positionsX[index] = obs.position.x;
                        positionsY[index] = obs.position.y;
                        itypes[index] = obs.itype;
                        categories[index] = obs.category;
                        index++;
                    }
                }
            }
            
            // Assign to appropriate fields
            switch (type) {
                case "npc":
                    this.npcSpriteTypes = spriteTypes;
                    this.npcObsIds = obsIds;
                    this.npcPositionsX = positionsX;
                    this.npcPositionsY = positionsY;
                    this.npcItypes = itypes;
                    this.npcCategories = categories;
                    break;
                case "immovable":
                    this.immovableSpriteTypes = spriteTypes;
                    this.immovableObsIds = obsIds;
                    this.immovablePositionsX = positionsX;
                    this.immovablePositionsY = positionsY;
                    this.immovableItypes = itypes;
                    this.immovableCategories = categories;
                    break;
                case "movable":
                    this.movableSpriteTypes = spriteTypes;
                    this.movableObsIds = obsIds;
                    this.movablePositionsX = positionsX;
                    this.movablePositionsY = positionsY;
                    this.movableItypes = itypes;
                    this.movableCategories = categories;
                    break;
                case "resource":
                    this.resourceSpriteTypes = spriteTypes;
                    this.resourceObsIds = obsIds;
                    this.resourcePositionsX = positionsX;
                    this.resourcePositionsY = positionsY;
                    this.resourceItypes = itypes;
                    this.resourceCategories = categories;
                    break;
                case "portal":
                    this.portalSpriteTypes = spriteTypes;
                    this.portalObsIds = obsIds;
                    this.portalPositionsX = positionsX;
                    this.portalPositionsY = positionsY;
                    this.portalItypes = itypes;
                    this.portalCategories = categories;
                    break;
                case "fromAvatar":
                    this.fromAvatarSpriteTypes = spriteTypes;
                    this.fromAvatarObsIds = obsIds;
                    this.fromAvatarPositionsX = positionsX;
                    this.fromAvatarPositionsY = positionsY;
                    this.fromAvatarItypes = itypes;
                    this.fromAvatarCategories = categories;
                    break;
            }
        }
        
        private void setEmptyArrays(String type) {
            int[] emptySpriteTypes = new int[0];
            int[] emptyObsIds = new int[0];
            double[] emptyPositionsX = new double[0];
            double[] emptyPositionsY = new double[0];
            int[] emptyItypes = new int[0];
            int[] emptyCategories = new int[0];
            
            switch (type) {
                case "npc":
                    this.npcSpriteTypes = emptySpriteTypes;
                    this.npcObsIds = emptyObsIds;
                    this.npcPositionsX = emptyPositionsX;
                    this.npcPositionsY = emptyPositionsY;
                    this.npcItypes = emptyItypes;
                    this.npcCategories = emptyCategories;
                    break;
                case "immovable":
                    this.immovableSpriteTypes = emptySpriteTypes;
                    this.immovableObsIds = emptyObsIds;
                    this.immovablePositionsX = emptyPositionsX;
                    this.immovablePositionsY = emptyPositionsY;
                    this.immovableItypes = emptyItypes;
                    this.immovableCategories = emptyCategories;
                    break;
                case "movable":
                    this.movableSpriteTypes = emptySpriteTypes;
                    this.movableObsIds = emptyObsIds;
                    this.movablePositionsX = emptyPositionsX;
                    this.movablePositionsY = emptyPositionsY;
                    this.movableItypes = emptyItypes;
                    this.movableCategories = emptyCategories;
                    break;
                case "resource":
                    this.resourceSpriteTypes = emptySpriteTypes;
                    this.resourceObsIds = emptyObsIds;
                    this.resourcePositionsX = emptyPositionsX;
                    this.resourcePositionsY = emptyPositionsY;
                    this.resourceItypes = emptyItypes;
                    this.resourceCategories = emptyCategories;
                    break;
                case "portal":
                    this.portalSpriteTypes = emptySpriteTypes;
                    this.portalObsIds = emptyObsIds;
                    this.portalPositionsX = emptyPositionsX;
                    this.portalPositionsY = emptyPositionsY;
                    this.portalItypes = emptyItypes;
                    this.portalCategories = emptyCategories;
                    break;
                case "fromAvatar":
                    this.fromAvatarSpriteTypes = emptySpriteTypes;
                    this.fromAvatarObsIds = emptyObsIds;
                    this.fromAvatarPositionsX = emptyPositionsX;
                    this.fromAvatarPositionsY = emptyPositionsY;
                    this.fromAvatarItypes = emptyItypes;
                    this.fromAvatarCategories = emptyCategories;
                    break;
            }
        }
    
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("VGDLResult{\n");
            
            // Game state information
            sb.append("  Game State: ").append(gameState).append("\n");
            sb.append("  Game Score: ").append(gameScore).append("\n");
            sb.append("  Game Tick: ").append(gameTick).append("\n");
            sb.append("  Game Over: ").append(isGameOver).append("\n");
            sb.append("  Game Winner: ").append(gameWinner).append("\n");
            
            // World dimensions
            sb.append("  World Size: ").append(worldWidth).append("x").append(worldHeight).append("\n");
            sb.append("  Block Size: ").append(blockSize).append("\n");
            sb.append("  Grid Size: ").append(gridWidth).append("x").append(gridHeight).append("\n");
            
            // Avatar information
            sb.append("  Avatar Position: (").append(String.format("%.2f", avatarPositionX))
              .append(", ").append(String.format("%.2f", avatarPositionY)).append(")\n");
            sb.append("  Avatar Health: ").append(avatarHealthPoints).append("/").append(avatarMaxHealthPoints).append("\n");
            sb.append("  Avatar Alive: ").append(isAvatarAlive).append("\n");
            sb.append("  Avatar Speed: ").append(String.format("%.2f", avatarSpeed)).append("\n");
            sb.append("  Avatar Last Action: ").append(avatarLastAction).append("\n");
            
            // Resources
            sb.append("  Resources: ");
            if (resourceTypes.length > 0) {
                for (int i = 0; i < resourceTypes.length; i++) {
                    sb.append("Type").append(resourceTypes[i]).append(":").append(resourceAmounts[i]);
                    if (i < resourceTypes.length - 1) sb.append(", ");
                }
            } else {
                sb.append("None");
            }
            sb.append("\n");
            
            // Available actions
            sb.append("  Available Actions: ").append(availableActions.length).append(" actions\n");
            
            // Object counts
            sb.append("  NPCs: ").append(npcSpriteTypes.length).append("\n");
            sb.append("  Immovables: ").append(immovableSpriteTypes.length).append("\n");
            sb.append("  Movables: ").append(movableSpriteTypes.length).append("\n");
            sb.append("  Resources: ").append(resourceSpriteTypes.length).append("\n");
            sb.append("  Portals: ").append(portalSpriteTypes.length).append("\n");
            sb.append("  FromAvatar: ").append(fromAvatarSpriteTypes.length).append("\n");
            
            // Events
            sb.append("  Recent Events: ").append(eventGameSteps.length).append(" events\n");
            
            sb.append("}");
            return sb.toString();
        }
    
    }

    public static void PlayVGDL(String vgdl, String level, boolean visuals){
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
		var genGame = ArcadeMachine.runOneGameStr(vgdl, level, visuals, sampleMCTSController, recordActionsFile, seed, 0);
        var obs = genGame.getObservation();

        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .create();

                
        var result = new VGDLResult(obs);
        String jsonResult = gson.toJson(result);
        
        System.out.println("Game finished with JSON result: " + jsonResult);
        System.out.println(result.toString());
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

        PlayVGDL(vgdl, level, false);

    }
}
