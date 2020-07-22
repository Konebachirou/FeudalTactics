package com.sesu8642.feudaltactics.engine;

import java.util.ArrayList;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sesu8642.feudaltactics.gamestate.GameState;
import com.sesu8642.feudaltactics.gamestate.HexTile;
import com.sesu8642.feudaltactics.gamestate.Kingdom;
import com.sesu8642.feudaltactics.gamestate.Player;
import com.sesu8642.feudaltactics.gamestate.Player.Type;
import com.sesu8642.feudaltactics.screens.IngameScreen;
import com.sesu8642.feudaltactics.screens.IngameScreen.IngameStages;

public class GameController {

	private MapRenderer mapRenderer;
	private GameState gameState;
	private IngameScreen ingameScreen;
	private LinkedList<GameState> undoStates;
	BotAI botAI = new BotAI();
	
	public GameController() {
		this.gameState = new GameState();
		this.undoStates = new LinkedList<GameState>();
	}

	public void generateDummyMap() {
		ArrayList<Player> players = new ArrayList<Player>();
		gameState.setBotIntelligence(BotAI.Intelligence.MEDIUM);
		Player p1 = new Player(new Color(0F, 1F, 1F, 1), Type.LOCAL_PLAYER);
		Player p2 = new Player(new Color(0.75F, 0.5F, 0F, 1), Type.LOCAL_BOT);
		Player p3 = new Player(new Color(1F, 0.67F, 0.67F, 1), Type.LOCAL_BOT);
		Player p4 = new Player(new Color(1F, 1F, 0F, 1), Type.LOCAL_BOT);
		Player p5 = new Player(new Color(1F, 1F, 1F, 1), Type.LOCAL_BOT);
		Player p6 = new Player(new Color(0F, 1F, 0F, 1), Type.LOCAL_BOT);
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		players.add(p5);
		players.add(p6);
		Long seed = GameStateController.initializeMap(gameState, players, 100, -10, 0.1F, null);
		updateSeedText(seed.toString());
		mapRenderer.updateMap(gameState);
	}

	public void printTileInfo(Vector2 hexCoords) {
		System.out.println("clicked tile position " + hexCoords);
		System.out.println(gameState.getMap().getTiles().get(hexCoords));
	}
	
	public void updateSeedText(String seedText) {
		ingameScreen.getMenuStage().setBottomLabelText("Seed: " + seedText);
	}
	
	public void updateInfoText() {
		if (ingameScreen == null) {
			return;
		}
		Kingdom kingdom = gameState.getActiveKingdom();
		if (kingdom == null) {
			ingameScreen.getHudStage().setInfoText("");
			return;
		}
		int income = kingdom.getIncome();
		int salaries = kingdom.getSalaries();
		int result = income - salaries;
		int savings = kingdom.getSavings();
		String resultText = result < 0 ? String.valueOf(result) : "+" + result;
		String infoText = "Savings: " + savings + " (" + resultText + ")";
		ingameScreen.getHudStage().setInfoText(infoText);
	}

	public void activateKingdom(Kingdom kingdom) {
		GameStateController.activateKingdom(gameState, kingdom);
		updateInfoText();
		mapRenderer.updateMap(gameState);
	}

	public void pickupObject(HexTile tile) {
		undoStates.add(new GameState(this.gameState));
		GameStateController.pickupObject(gameState, tile);
		mapRenderer.updateMap(gameState);
		ingameScreen.getHudStage().updateHandContent(gameState.getHeldObject().getSpriteName());
	}

	public void placeOwn(HexTile tile) {
		undoStates.add(new GameState(this.gameState));
		GameStateController.placeOwn(gameState, tile);
		mapRenderer.updateMap(gameState);
		ingameScreen.getHudStage().updateHandContent(null);
	}

	public void combineUnits(HexTile tile) {
		undoStates.add(new GameState(this.gameState));
		GameStateController.combineUnits(gameState, tile);
		mapRenderer.updateMap(gameState);
		updateInfoText();
		ingameScreen.getHudStage().updateHandContent(null);
	}

	public void conquer(HexTile tile) {
		undoStates.add(new GameState(this.gameState));
		GameStateController.conquer(gameState, tile);
		mapRenderer.updateMap(gameState);
		updateInfoText();
		ingameScreen.getHudStage().updateHandContent(null);
	}

	public void endTurn() {
		gameState = GameStateController.endTurn(gameState);
		// clear undo states
		undoStates.clear();
		// reset info text
		updateInfoText();
		mapRenderer.updateMap(gameState);
		// make bots act
		if (gameState.getActivePlayer().getType() == Type.LOCAL_BOT) {
			gameState = botAI.doTurn(gameState,
					gameState.getBotIntelligence());
			endTurn();
		}
	}

	public void buyPeasant() {
		undoStates.add(new GameState(this.gameState));
		GameStateController.buyPeasant(gameState);
		updateInfoText();
		mapRenderer.updateMap(gameState);
		ingameScreen.getHudStage().updateHandContent(gameState.getHeldObject().getSpriteName());
	}

	public void buyCastle() {
		undoStates.add(new GameState(this.gameState));
		GameStateController.buyCastle(gameState);
		mapRenderer.updateMap(gameState);
		updateInfoText();
		ingameScreen.getHudStage().updateHandContent(gameState.getHeldObject().getSpriteName());
	}

	public void undoLastAction() {
		this.gameState = undoStates.removeLast();
		mapRenderer.updateMap(gameState);
		updateInfoText();
		if (gameState.getHeldObject() != null) {
			ingameScreen.getHudStage().updateHandContent(gameState.getHeldObject().getSpriteName());
		} else {
			ingameScreen.getHudStage().updateHandContent(null);
		}
	}

	public void toggleMenu() {
		ingameScreen.activateStage(IngameStages.MENU);
	}
	
	public void setHud(IngameScreen gameUIOverlay) {
		this.ingameScreen = gameUIOverlay;
	}

	public MapRenderer getMapRenderer() {
		return mapRenderer;
	}

	public void setMapRenderer(MapRenderer mapRenderer) {
		this.mapRenderer = mapRenderer;
	}

	public GameState getGameState() {
		return gameState;
	}

	public LinkedList<GameState> getUndoStates() {
		return undoStates;
	}

}