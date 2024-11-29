package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;

public class MinesweeperJavaFX extends Application {
    private final int rows = 10;
    private final int cols = 10;
    private final int mines = 20;
    private Button[][] buttons = new Button[rows][cols];
    private boolean[][] mineField = new boolean[rows][cols];
    private int[][] mineCounts = new int[rows][cols];  // Store the number of adjacent mines
    private boolean[][] revealed = new boolean[rows][cols];  // Track revealed cells

    private boolean gameOver = false;

    private Stage primaryStage;  // Store reference to the primary stage
    private final String secretPassword = "letmein";  // The password to reveal the mines

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;  // Save the primary stage reference

        GridPane grid = new GridPane();

        // Initialize buttons
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = new Button();
                buttons[i][j].setPrefSize(50, 50);
                grid.add(buttons[i][j], j, i);

                final int x = i, y = j;
                buttons[i][j].setOnAction(e -> handleClick(x, y));
            }
        }

        // Place mines and calculate adjacent mine counts
        placeMines();
        calculateMineCounts();

        Scene scene = new Scene(grid, 500, 500);
        
        // Listen for the "h" key press to trigger the reveal action
        scene.setOnKeyPressed(this::handleKeyPress);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Minesweeper");
        primaryStage.show();
    }

    private void placeMines() {
        int placedMines = 0;
        while (placedMines < mines) {
            int x = (int) (Math.random() * rows);
            int y = (int) (Math.random() * cols);
            if (!mineField[x][y]) {
                mineField[x][y] = true;
                placedMines++;
            }
        }
    }

    private void calculateMineCounts() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mineField[i][j]) {
                    // If it's a mine, increase the count for neighboring cells
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = i + dx;
                            int ny = j + dy;
                            if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && !mineField[nx][ny]) {
                                mineCounts[nx][ny]++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleClick(int x, int y) {
        if (gameOver || revealed[x][y]) return;  // Don't allow further clicks if the game is over or cell is revealed

        revealed[x][y] = true;  // Mark the cell as revealed

        if (mineField[x][y]) {
            buttons[x][y].setText("M");
            gameOver = true;
            showGameOverDialog();
        } else {
            // Reveal the cell and its adjacent cells if it's a '0'
            reveal(x, y);

            // Check for a win after revealing cells
            if (checkWin()) {
                showWinDialog();
            }
        }
    }

    private void reveal(int x, int y) {
        // Prevent revealing already clicked buttons
        if (!buttons[x][y].getText().isEmpty()) return;

        // Display the mine count
        int count = mineCounts[x][y];
        buttons[x][y].setText(count > 0 ? String.valueOf(count) : "0");

        if (count == 0) {
            // If there are no mines around, reveal adjacent cells
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && !revealed[nx][ny]) {
                        revealed[nx][ny] = true;
                        reveal(nx, ny);  // Recursively reveal adjacent cells
                    }
                }
            }
        }
    }

    private boolean checkWin() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // If a cell is not a mine and not revealed, the player hasn't won yet
                if (!mineField[i][j] && !revealed[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getText().equals("h")) {  // Detect "h" key press
            showPasswordDialog();
        }
    }

    private void showPasswordDialog() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password to reveal mines");

        // Create the dialog box for password input
        Alert passwordDialog = new Alert(AlertType.CONFIRMATION);
        passwordDialog.setTitle("Password Required");
        passwordDialog.setHeaderText("Please enter the password to reveal mines.");
        passwordDialog.getDialogPane().setContent(passwordField);

        ButtonType buttonTypeSubmit = new ButtonType("Submit");
        ButtonType buttonTypeCancel = new ButtonType("Cancel");

        passwordDialog.getButtonTypes().setAll(buttonTypeSubmit, buttonTypeCancel);

        passwordDialog.showAndWait().ifPresent(response -> {
            if (response == buttonTypeSubmit) {
                if (passwordField.getText().equals(secretPassword)) {
                    revealAllMines();
                } else {
                    Alert wrongPasswordAlert = new Alert(AlertType.ERROR);
                    wrongPasswordAlert.setTitle("Incorrect Password");
                    wrongPasswordAlert.setHeaderText("The password you entered is incorrect.");
                    wrongPasswordAlert.showAndWait();
                }
            }
        });
    }

    private void revealAllMines() {
        // Reveal all mines immediately
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mineField[i][j]) {
                    buttons[i][j].setText("M");
                }
            }
        }
    }

    private void showGameOverDialog() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("You hit a mine!");
        alert.setContentText("Would you like to try again?");

        ButtonType buttonTypeRetry = new ButtonType("Try Again");
        ButtonType buttonTypeExit = new ButtonType("Exit");

        alert.getButtonTypes().setAll(buttonTypeRetry, buttonTypeExit);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeRetry) {
                restartGame();
            } else {
                System.exit(0);  // Exit the game
            }
        });
    }

    private void showWinDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText("You win!");
        alert.setContentText("You successfully revealed all non-mine cells.");

        ButtonType buttonTypeRetry = new ButtonType("Play Again");
        ButtonType buttonTypeExit = new ButtonType("Exit");

        alert.getButtonTypes().setAll(buttonTypeRetry, buttonTypeExit);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeRetry) {
                restartGame();
            } else {
                System.exit(0);  // Exit the game
            }
        });
    }

    private void restartGame() {
         gameOver = false;

        // Reset the game state
        mineField = new boolean[rows][cols];
        mineCounts = new int[rows][cols];
        revealed = new boolean[rows][cols];
        buttons = new Button[rows][cols];

        // Create a new grid
        GridPane newGrid = new GridPane();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = new Button();
                buttons[i][j].setPrefSize(50, 50);
                newGrid.add(buttons[i][j], j, i);

                final int x = i, y = j;
                buttons[i][j].setOnAction(e -> handleClick(x, y));
            }
        }

        // Place mines and calculate adjacent mine counts
        placeMines();
        calculateMineCounts();

        // Create a new scene with the updated grid and reattach the key event listener
        Scene newScene = new Scene(newGrid, 500, 500);
        newScene.setOnKeyPressed(this::handleKeyPress); // Reattach the key listener for the "h" key

        // Set the new scene to the stage
        primaryStage.setScene(newScene);
    }

//    public static void main(String[] args) {
//        launch(args);
//    }
}


