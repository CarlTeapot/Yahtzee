
import acm.io.*;
import acm.program.*;
import acm.util.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		dices = new int[N_DICE];
		points = new int[nPlayers][N_CATEGORIES + 1];
		usedCategories = new boolean[nPlayers][N_CATEGORIES + 1];
		playGame();
	}
	/*
		Method for playing the game, it plays a single round 13 (ROUND) times
		at the end of the loop, it shows all the points (including the lower and upper scores)
		then it checks the winner of the game or if it is a draw
	 */
	private void playGame() {
		for (int i = 0; i < ROUNDS; i++) {
			playRound();
		}
		showAllPoints();
		if (checkDraw())
			display.printMessage(drawMessage());
		else
			display.printMessage(winMessage());
	}
	// Method for playing a single round
	// a single round contains the turns of all players
	private void playRound() {
		for (int i = 1; i <= nPlayers; i++) {
			playerTurn(i);
		}
	}
	// Displays the lower, upper and bonus scores at the end of the game
	private void showAllPoints() {
		for (int i = 1; i <= nPlayers; i++) {
			display.updateScorecard(UPPER_SCORE, i, points[i - 1][UPPER_SCORE]);
			display.updateScorecard(LOWER_SCORE, i, points[i - 1][LOWER_SCORE]);
			display.updateScorecard(UPPER_BONUS, i, points[i - 1][UPPER_BONUS]);
		}
	}
	/*
		Method for playing a single turn
		player rolls the dice 3 times on the turn and at the end chooses the category
		after that the method checks weather or not the category was already used
		at the end of the turn it updates the points matrix
		and displays the points on the scoreboard using the updatePoints() method
	 */
	private void playerTurn(int player) {
		startingRoll(player);
		for (int j = 0; j < MAX_TRIES; j++) {
			roll(player);
		}
		int category = display.waitForPlayerToSelectCategory();
		while (checkIfCategoryIsChosen(player, category)) {
			display.printMessage(printUsedCategoryMessage());
			category = display.waitForPlayerToSelectCategory();
		}
		updatePoints(player, category, checkCategory(category));

	}
	/*
		Method for updating the points[][] matrix and displaying new points on the scoreboard
	 */
	private void updatePoints(int player, int category, int point) {
		points[player - 1][category] = point;
		display.updateScorecard(category, player, points[player - 1][category]);
		usedCategories[player - 1][category] = true;
		if (category < UPPER_SCORE) {
			updateUpper(player, category);
			updateBonus(player);
		}
		if (category > UPPER_BONUS)
			updateLower(player, category);

		display.updateScorecard(TOTAL, player, updateTotal(player));
	}
	// Updates the bonus category
	private int updateBonus(int player) {
		if (points[player-1][UPPER_SCORE] >= 63)
			points[player - 1][UPPER_BONUS] = 35;
		return points[player - 1][UPPER_BONUS];
	}
	// Updates the upper score category
	private int updateUpper(int player, int category) {
		points[player - 1][UPPER_SCORE] += points[player - 1][category];
		return points[player - 1][UPPER_SCORE];
	}
	// Updates the lower score category
	private int updateLower(int player, int category) {
		points[player - 1][LOWER_SCORE] += points[player - 1][category];
		return points[player - 1][LOWER_SCORE];
	}
	// updates the total score category using the upper, lower and bonus scores

	private int updateTotal(int player) {
		points[player - 1][TOTAL] = points[player - 1][UPPER_SCORE] + points[player - 1][LOWER_SCORE]
				+ points[player - 1][UPPER_BONUS];
		return points[player - 1][TOTAL];
	}
	// Method for rolling the dice
	private void roll(int i) {
		display.printMessage(printRollMessage());
		display.waitForPlayerToSelectDice();
		rollDices(checkSelectedDices());
		display.displayDice(dices);
	}
	// Method for rolling the dice at the beginning
	private void startingRoll(int i) {
		display.printMessage(printStartMessage(i));
		display.waitForPlayerToClickRoll(i);
		rollDice();
		display.displayDice(dices);

	}
	// Finds the winner of the game and returns its index
	private int findPlayerWithHighestPoint() {
		int max = 0;
		int player = 0;
		for (int i = 0; i < nPlayers; i++) {
			if (points[i][TOTAL] > max) {
				max = Math.max(max, points[i][TOTAL]);
				player = i;
			}
		}
		return player;
	}
	/* checks if the game is a draw
	 to do this, the method creates a new array of the total points of players,
	 it sorts the array and if it's size is larger than 1 and
	  if the first and the last values of the array are equal, then the game is a draw
	*/
	private boolean checkDraw() {
		int[] totalPoints = new int[nPlayers];
		for (int i = 0; i < nPlayers; i++) {
			totalPoints[i] = points[i][TOTAL];
		}
		Arrays.sort(totalPoints);
		if (nPlayers == 1)
			return false;
		else
			return totalPoints[0] == totalPoints[nPlayers - 1];
	}

	// Code for checking the categories
	private int checkCategory(int category) {
		if (category < 7) {
			int sum = 0;
			for (int dice : dices) {
				if (dice == category)
					sum += category;
			}
			return sum;

		} else if (category == CHANCE) {
			return sum(dices);

		} else if (category == LARGE_STRAIGHT) {
			Arrays.sort(dices);
			int num = dices[0];
			for (int i = 1; i < dices.length; i++) {
				if (dices[i] - num != 1)
					return 0;
				num = dices[i];
			}
			return 40;
		} else if (category == SMALL_STRAIGHT) {
			Arrays.sort(dices);
			int num = dices[0];
			int counter = 0;
			for (int dice : dices) {
				if (dice - num == 1)
					counter++;
				num = dice;
			}
			if (counter >= 3)
				return 30;
			else return 0;

		} else if (category == THREE_OF_A_KIND) {
			Arrays.sort(dices);
			for (int i : dices) {
				int counter = 0;
				for (int j : dices) {
					if (i == j)
						counter++;
				}
				if (counter >= 3)
					return sum(dices);
			}
		} else if (category == FOUR_OF_A_KIND) {
			Arrays.sort(dices);
			for (int i : dices) {
				int counter = 0;
				for (int j : dices) {
					if (i == j)
						counter++;
				}
				if (counter >= 4)
					return sum(dices);
			}
		} else if (category == FULL_HOUSE) {
			Arrays.sort(dices);
			if (dices[2] == dices[0] && dices[3] == dices[4] && dices[0] != dices[4]) {
				return 25;
			} else if (dices[1] == dices[0] && dices[2] == dices[4] && dices[0] != dices[4])
				return 25;
			else return 0;

		} else if (category == YAHTZEE) {
			int counter = 0;
			int num = dices[0];
			for (int i : dices) {
				if (i == num)
					counter++;
			}
			if (counter == 5)
				return 50;
			else return 0;
		}
		return 0;
	}

	// Sums up the dice values
	// this method is used by the Chance, Four-of-a-kind and three-of-a-kind categories
	// It is not necessary to have the array of dices as a parameter because the dice array is an instance variable
	// But I think this is more practical because you can use the method to sum up other arrays
	private static int sum(int[] dices) {
		int sum = 0;
		for (int i : dices)
			sum += i;
		return sum;
	}


	// checks which dices have been chosen to re-roll
	// it stores the chosen dice indexes in an arraylist
	// this is not the simplest approach, but it is an intuitive one
	private ArrayList<Integer> checkSelectedDices() {
		ArrayList<Integer> selectedDices = new ArrayList<>();
		for (int i = 0; i < dices.length; i++) {
			if (display.isDieSelected(i))
				selectedDices.add(i);
		}
		return selectedDices;
	}

	// roles every dice using RandomGenerator
	private void rollDice() {
		for (int i = 0; i < dices.length; i++) {
			dices[i] = rgen.nextInt(1, 6);
		}
	}

	// rolls the chosen dices
	private void rollDices(ArrayList<Integer> diceIndexes) {
		for (int i : diceIndexes) {
			dices[i] = rgen.nextInt(1, 6);
		}
	}

	//
	private boolean checkIfCategoryIsChosen(int player, int category) {
		return usedCategories[player - 1][category];
	}

	// Message that is displayed at the start of the players turn
	private String printStartMessage(int i) {
		return playerNames[i - 1] + "'s turn, Click \"Roll Dice\" button to roll the dice";
	}

	//Message that is displayed after the first roll
	private String printRollMessage() {
		return "Select the dice you wish to re-roll and click \"Roll again\" ";
	}

	// Message that is displayed after rolling the dice three times
	private String printCategoryMessage() {
		return "Select a category for this roll";
	}

	// Message that is displayed when the player chooses already chosen category
	private String printUsedCategoryMessage() {
		return "This category has already been chosen, try something else";
	}

	// Message that displays who won the game
	private String winMessage() {
		int player = findPlayerWithHighestPoint();
		return "Congratulations, " + playerNames[player] + ", you're the winner with a total score of " + points[player][TOTAL] + "!";
	}

	// Message that is displayed when the game is a draw
	private String drawMessage() {
		return "Game is a draw";
	}

	/* Private instance variables */
	private final int ROUNDS = 13;
	private final int MAX_TRIES = 2;
	// Amount of players in the game
	private int nPlayers;
	// Array of dices
	private int[] dices;
	// Matrix of player points (rows (the first array) are the players, columns (arrays within arrays) are the category points of each player)
	private int[][] points;
	// boolean matrix that is used to check wether or not the player has already used this category
	private boolean[][] usedCategories;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

}
