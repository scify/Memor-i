package org.scify.memori;

import javafx.util.Pair;
import org.scify.memori.interfaces.MoveFactory;
import org.scify.memori.interfaces.Tile;
import org.scify.memori.interfaces.UserAction;
import org.scify.memori.rules.RuleObserverObject;

import java.awt.geom.Point2D;
import java.util.*;

public class CPUMoveFactory implements Observer, MoveFactory {

    private static ArrayList<ArrayList<Point2D>> visitedTilesCombinations = new ArrayList<>();
    private static ArrayList<Pair<Point2D, Tile>> visitedTilesPositionsForCurrTurn = new ArrayList<>();
    private static Map<Point2D, Tile> visitedTilesWithPositions = new HashMap<>();

    @Override
    public ArrayList<UserAction> getNextUserMovements(MemoriGameState memoriGameState) {
        boolean tileFound = false;
        MemoriTerrain memoriTerrain = (MemoriTerrain) memoriGameState.getTerrain();
        Point2D currentPlayerPosition = new Point2D.Double(memoriGameState.getRowIndex(), memoriGameState.getColumnIndex());
        ArrayList<UserAction> movements = new ArrayList<>();
        while (!tileFound) {
            movements = new ArrayList<>();

            Pair<Point2D,Tile> candidateTile = getCandidateTile(memoriTerrain);

            Point2D tilePosition = candidateTile.getKey();
            Tile currTile = candidateTile.getValue();
            if(!currTile.getFlipped() && !currTile.getWon() && !candidateTileCombinationVisited(tilePosition)) {
                System.err.println("candidate tile found at position: " + tilePosition.getX() + " " + tilePosition.getY());
                System.err.println("flipped: " + currTile.getFlipped() + " won: " + currTile.getWon());
                System.err.println("tile info: " + currTile.getTileType());
                UserAction nextMovementInXAxis = navigateToTileInXAxis((int)tilePosition.getY(), currentPlayerPosition);
                while(nextMovementInXAxis != null) {
                    movements.add(nextMovementInXAxis);
                    nextMovementInXAxis = navigateToTileInXAxis((int)tilePosition.getY(), currentPlayerPosition);
                }
                UserAction nextMovementInYAxis = navigateToTileInYAxis((int)tilePosition.getX(), currentPlayerPosition);
                while(nextMovementInYAxis != null) {
                    movements.add(nextMovementInYAxis);
                    nextMovementInYAxis = navigateToTileInYAxis((int)tilePosition.getX(), currentPlayerPosition);
                }
                Pair<Point2D, Tile> tilePos = new Pair<>(tilePosition, currTile);
                visitedTilesPositionsForCurrTurn.add(tilePos);
                visitedTilesWithPositions.put(tilePosition, currTile);
                tileFound = true;
            }
        }
        return movements;
    }

    private Pair<Point2D,Tile> getCandidateTile(MemoriTerrain terrain) {
        Random rand = new Random();
        double randomMovementProbabilty = 0.20;
        if(visitedTilesWithPositions.size() < 4)
            randomMovementProbabilty = 0.80;
        if (rand.nextDouble() < randomMovementProbabilty) { // <-- 20% of the time.
            return getRandomTile(terrain);
        } else {
            Pair<Point2D, Tile> candidateTile = searchForAlreadyVisitedPair();
            if(candidateTile != null)
                return candidateTile;
            return getRandomTile(terrain);
        }
    }

    private Pair<Point2D,Tile> getRandomTile(MemoriTerrain terrain) {
        Random random = new Random();
        ArrayList<Point2D> keys = new ArrayList<>(terrain.getTiles().keySet());
        Point2D randomKey = keys.get( random.nextInt(keys.size()) );
        Tile value = terrain.getTiles().get(randomKey);
        return new Pair<>(randomKey, value);
    }

    private Pair<Point2D,Tile> searchForAlreadyVisitedPair() {
        if(visitedTilesWithPositions.isEmpty() && visitedTilesPositionsForCurrTurn.isEmpty())
            return null;
        else if(!visitedTilesWithPositions.isEmpty() && visitedTilesPositionsForCurrTurn.isEmpty())
            return getRandomTileFromAlreadyVisitedTiles();
        Pair<Point2D, Tile> openTilePosition = visitedTilesPositionsForCurrTurn.get(0);
        for (Map.Entry<Point2D, Tile> entry : visitedTilesWithPositions.entrySet()) {
            Point2D curTilePos = entry.getKey();
            Tile curTile = entry.getValue();
            if(openTilePosition.getValue().getTileType().equals(curTile.getTileType()) && !curTilePos.equals(openTilePosition.getKey())) {
                return new Pair<>(curTilePos, curTile);
            }
        }
        return null;
    }

    private Pair<Point2D,Tile> getRandomTileFromAlreadyVisitedTiles() {
        Random random = new Random();
        ArrayList<Point2D> keys = new ArrayList<>(visitedTilesWithPositions.keySet());
        Point2D randomKey = keys.get( random.nextInt(keys.size()) );
        Tile value = visitedTilesWithPositions.get(randomKey);
        return new Pair<>(randomKey, value);
    }

    @Override
    public void updateFactoryComponents() {
        if(!visitedTilesPositionsForCurrTurn.isEmpty()) {
            Pair<Point2D, Tile> firstTilePos = visitedTilesPositionsForCurrTurn.get(0);
            Pair<Point2D, Tile> secondTilePos = visitedTilesPositionsForCurrTurn.get(1);
            ArrayList<Point2D> positionsToAdd = new ArrayList<>();
            positionsToAdd.add(firstTilePos.getKey());
            positionsToAdd.add(secondTilePos.getKey());
            visitedTilesCombinations.add(positionsToAdd);

            visitedTilesPositionsForCurrTurn = new ArrayList<>();
        }
    }

    @Override
    public int getMovementDelay() {
        return 1000;
    }

    private boolean candidateTileCombinationVisited(Point2D candidateTilePosition) {
        if(visitedTilesPositionsForCurrTurn.isEmpty())
            return false;
        Pair<Point2D, Tile> previousTileVisited = visitedTilesPositionsForCurrTurn.get(0);
        Point2D tilePos = previousTileVisited.getKey();
        for(ArrayList<Point2D> positionCombination : visitedTilesCombinations) {
            Point2D firstTilePosition = positionCombination.get(0);
            Point2D secondTilePosition = positionCombination.get(1);
            if((tilePos.equals(firstTilePosition) && candidateTilePosition.equals(secondTilePosition)) || (tilePos.equals(secondTilePosition) && candidateTilePosition.equals(firstTilePosition))) {
                return true;
            }
        }
        return false;
    }

    private UserAction navigateToTileInXAxis(int tileXPosition, Point2D playerPosition) {

        if(tileXPosition == (int)playerPosition.getX())
            return null;
        if(tileXPosition < (int)playerPosition.getX()) {
            playerPosition.setLocation(playerPosition.getX() - 1, playerPosition.getY());
            return new UserAction("opponent_movement", "UP");
        }
        else {
            playerPosition.setLocation(playerPosition.getX() + 1, playerPosition.getY());
            return new UserAction("opponent_movement", "DOWN");
        }
    }

    private UserAction navigateToTileInYAxis(int tileYPosition, Point2D playerPosition) {
        if(tileYPosition == (int)playerPosition.getY())
            return null;
        if(tileYPosition < (int)playerPosition.getY()) {
            playerPosition.setLocation(playerPosition.getX(), playerPosition.getY() - 1);
            return new UserAction("opponent_movement", "LEFT");
        }
        else {
            playerPosition.setLocation(playerPosition.getX(), playerPosition.getY() + 1);
            return new UserAction("opponent_movement", "RIGHT");
        }
    }

    @Override
    public  UserAction getUserFlip() {
        return new UserAction("flip", "SPACE");
    }

    @Override
    public void update(Observable o, Object arg) {
        RuleObserverObject ruleObserverObject = (RuleObserverObject) arg;
        String ruleObserverCode = ruleObserverObject.code;
        if(ruleObserverCode.equals("TILE_REVEALED")) {
            Pair<UserAction, Tile> newTilePos = (Pair<UserAction, Tile>) ruleObserverObject.parameters;
            visitedTilesWithPositions.put(new Point2D.Double(newTilePos.getKey().getCoords().getY(), newTilePos.getKey().getCoords().getX()), newTilePos.getValue());
        }
    }
}
