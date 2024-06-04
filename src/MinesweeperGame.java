import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.CircleImage;
import javalib.worldimages.EquilateralTriangleImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;
import tester.Tester;

// Utils interface
interface IUtils {

  // Color that represents a revealed cell
  Color CELL_COLOR_REVEALED = new Color(245, 212, 188);

  // Color that represents a covered cell
  Color CELL_COLOR_COVERED = new Color(181, 217, 119);

  // size of the cell
  int CELL_SIZE = 50;

  // WorldImage that represents a revealed cell
  WorldImage CELL_REVEALED = new OverlayImage(
      new RectangleImage(IUtils.CELL_SIZE - 2, IUtils.CELL_SIZE - 2, OutlineMode.SOLID,
          IUtils.CELL_COLOR_REVEALED),
      new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, OutlineMode.SOLID,
          new Color(224, 193, 171)));

  // WorldImage that represents a covered cell
  WorldImage CELL_COVERED = new OverlayImage(
      new RectangleImage(IUtils.CELL_SIZE - 2, IUtils.CELL_SIZE - 2, OutlineMode.SOLID,
          IUtils.CELL_COLOR_COVERED),
      new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, OutlineMode.SOLID,
          new Color(144, 173, 94)));

  // WorldImage that represents a flag
  WorldImage FLAG = new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.ORANGE);

  // WorldImage that represents a mine
  WorldImage MINE = new CircleImage(15, OutlineMode.SOLID, Color.RED);

}

// Utils class that is used to check for constructor exceptions
class Utils {

  // checks if a given value is in a given range
  int checkRange(int val, int min, int max, String msg) {
    if (val > min && val <= max) {
      return val;
    }
    else {
      throw new IllegalArgumentException(msg);
    }
  }
}

//Represents a game World of Minesweeper
class Minesweeper extends World implements IUtils {
  int rows;
  int columns;
  int mines;
  Random randMines;
  ArrayList<ArrayList<Cell>> grid;
  int score;

  Minesweeper(int rows, int columns, int mines) {
    this.rows = new Utils().checkRange(rows, 0, 100, "Invalid Number of Rows");
    this.columns = new Utils().checkRange(columns, 0, 100, "Invalid Number of Columns");
    this.mines = new Utils().checkRange(mines, 0, this.rows * this.columns,
        "Invalid Number of Mines");
    this.randMines = new Random();
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.score = 0;

    makeGrid();
    addMines();
    findNeighbors();
    countAllMines();

  }

  // constructor for testing
  Minesweeper(int rows, int columns, int mines, Random randMines) {
    this.rows = new Utils().checkRange(rows, 0, 100, "Invalid Number of Rows");
    this.columns = new Utils().checkRange(columns, 0, 100, "Invalid Number of Columns");
    this.mines = new Utils().checkRange(mines, 0, this.rows * this.columns,
        "Invalid Number of Mines");
    this.randMines = randMines;
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.score = 0;
  }

  // draws the grid/mines on the background
  @Override
  public WorldScene makeScene() {


    WorldScene w = new WorldScene(columns * IUtils.CELL_SIZE, rows * IUtils.CELL_SIZE);

    w.placeImageXY(new TextImage("Score: " + Integer.toString(this.score), Color.BLACK),
        IUtils.CELL_SIZE, IUtils.CELL_SIZE);


    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.columns; c++) {
        this.grid.get(r).get(c).drawCell(w, r * IUtils.CELL_SIZE + IUtils.CELL_SIZE / 2,
            c * IUtils.CELL_SIZE + IUtils.CELL_SIZE / 2);
      }
    }

    return w;

  }



  // counts the number of mines present in the neighbouring cell of each cell
  public void countAllMines() {
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.columns; c++) {
        this.grid.get(r).get(c).countMines();
      }
    }
  }

  // Initializes and constructs the game's grid using cells
  public void makeGrid() {
    for (int r = 0; r < this.rows; r++) {
      this.grid.add(new ArrayList<Cell>());

      for (int c = 0; c < this.columns; c++) {
        this.grid.get(r).add(new Cell());
      }
    }
  }

  // Randomly places mines within the grid
  public void addMines() {

    for (int i = 0; i < this.mines; i++) {
      int row = this.randMines.nextInt(this.rows - 1);
      int column = this.randMines.nextInt(this.columns - 1);

      Cell currCell = this.grid.get(row).get(column);

      if (!currCell.addAMine()) {
        i--;
      }
    }
  }

  // populates each cell's list of neighbours with adjacent cells
  public void findNeighbors() {
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.columns; c++) {
        // top
        if (r - 1 >= 0) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r - 1).get(c));
        }
        // bottom
        if (r + 1 < this.rows) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r + 1).get(c));
        }
        // left
        if (c - 1 >= 0) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r).get(c - 1));
        }
        // right
        if (c + 1 < this.columns) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r).get(c + 1));
        }
        // top left
        if (r - 1 >= 0 && c - 1 >= 0) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r - 1).get(c - 1));
        }
        // bottom left
        if (r + 1 < this.rows && c - 1 >= 0) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r + 1).get(c - 1));
        }
        // bottom right
        if (r + 1 < this.rows && c + 1 < this.columns) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r + 1).get(c + 1));
        }
        // top right
        if (r - 1 >= 0 && c + 1 < this.columns) {
          this.grid.get(r).get(c).addToNeighbors(this.grid.get(r - 1).get(c + 1));
        }
      }
    }
  }

  // reveals a cell if left-clicked, flags a cell if right clicked, and checks the
  // win/lost state of the game
  @Override
  public void onMouseClicked(Posn posn, String key) {

    if (key.equals("LeftButton")) {

      this.grid.get((int) Math.floor(posn.x / IUtils.CELL_SIZE))
      .get((int) Math.floor(posn.y / IUtils.CELL_SIZE)).reveal();

      if((this.grid.get(posn.y / IUtils.CELL_SIZE).get(posn.x / IUtils.CELL_SIZE)).isRevealed == false
          && !(this.grid.get(posn.y / IUtils.CELL_SIZE).get(posn.x / IUtils.CELL_SIZE)).containsMine == false) {
        this.score += 50;

      }

    }
    else if (key.equals("RightButton")) {
      this.grid.get((int) Math.floor(posn.x / IUtils.CELL_SIZE))
      .get((int) Math.floor(posn.y / IUtils.CELL_SIZE)).flagCell();
    }

    boolean gameOverLose = false;
    boolean gameOverWin = true;

    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.columns; c++) {
        if (this.grid.get(r).get(c).loseCondition()) {
          gameOverLose = true;
        }

        if (!this.grid.get(r).get(c).winCondition()) {
          gameOverWin = false;
        }
      }
    }
    if (gameOverLose) {
      this.endOfWorld("you lost!");
    }

    if (gameOverWin) {
      this.endOfWorld("you won!");
    }
  }

  // creates the last scene of the game, for the winning and losing cases
  @Override
  public WorldScene lastScene(String msg) {
    WorldScene w = new WorldScene(columns * IUtils.CELL_SIZE, rows * IUtils.CELL_SIZE);
    WorldImage text = new OverlayImage(new TextImage(msg, IUtils.CELL_SIZE, Color.BLACK),
        new RectangleImage(IUtils.CELL_SIZE * 5, IUtils.CELL_SIZE, OutlineMode.SOLID, Color.WHITE));

    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.columns; c++) {
        if (msg.equals("you lost!")) {
          this.grid.get(r).get(c).revealMines();
        }

        this.grid.get(r).get(c).drawCell(w, r * IUtils.CELL_SIZE + IUtils.CELL_SIZE / 2,
            c * IUtils.CELL_SIZE + IUtils.CELL_SIZE / 2);
      }
    }

    w.placeImageXY(text, w.height / 2, w.width / 2);

    return w;
  }

}

// a class to represent a cell on the board
class Cell {
  ArrayList<Cell> neighbors;
  boolean containsMine;
  boolean isFlagged;
  boolean isRevealed;
  int numMines;

  // empty constructor for testing
  Cell() {
    this.neighbors = new ArrayList<Cell>();
    this.containsMine = false;
    this.isFlagged = false;
    this.isRevealed = false;
    this.numMines = 0;

  }

  // constructor that contains fields
  Cell(ArrayList<Cell> neighbors, boolean containsMine, boolean isFlagged, boolean isRevealed,
      int numMines) {
    this.neighbors = neighbors;
    this.containsMine = containsMine;
    this.isFlagged = isFlagged;
    this.isRevealed = isRevealed;
    this.numMines = numMines;

  }

  // Adds a given cell to the current cell's list of neighbours
  public void addToNeighbors(Cell c) {
    this.neighbors.add(c);
  }

  // updates numMines field with the mine count in neighbouring cells
  void countMines() {
    for (Cell i : this.neighbors) {
      if (new FindMines().test(i)) {
        this.numMines++;
      }
    }
  }

  // Returns a color corresponding to i-index within the array list
  public Color colorNum(int i) {
    ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.GREEN, Color.RED,
        Color.ORANGE, Color.YELLOW, Color.CYAN, Color.PINK, Color.WHITE));

    return colors.get(i - 1);
  }

  // Creates a WorldImage with the number of mines adjacent to a cell
  public WorldImage displayNum() {
    return new TextImage(("" + this.numMines), colorNum(this.numMines));
  }

  // Draws a state of the Cell in Minesweeper's WorldScene
  public WorldScene drawCell(WorldScene w, int row, int column) {

    if (!this.isFlagged && !this.isRevealed) {
      w.placeImageXY(IUtils.CELL_COVERED, row, column);
    }
    else if (this.isFlagged && !this.isRevealed) {
      w.placeImageXY(new OverlayImage(IUtils.FLAG, IUtils.CELL_COVERED), row, column);
    }
    else if (!this.containsMine && this.isRevealed && this.numMines == 0) {
      w.placeImageXY(IUtils.CELL_REVEALED, row, column);
    }
    else if (!this.containsMine && this.isRevealed && this.numMines > 0) {
      w.placeImageXY(new OverlayImage(this.displayNum(), IUtils.CELL_REVEALED), row, column);
    }
    else {
      w.placeImageXY(new OverlayImage(IUtils.MINE, IUtils.CELL_REVEALED), row, column);
    }
    return w;
  }

  // adds a mine to a cell if it doesn't already contain a mine
  public boolean addAMine() {
    if (this.containsMine) {
      return false;
    }
    else {
      this.containsMine = true;
      return true;
    }

  }

  // reveals a cell if there are surrounding mines, flood-fills if there are not
  public void reveal() {
    if (!this.isFlagged && !this.isRevealed) {
      this.isRevealed = true;

      if (this.numMines == 0) {
        for (int i = 0; i < this.neighbors.size(); i++) {
          this.neighbors.get(i).reveal();
        }
      }
    }
  }

  // flags or unflags a cell based on the current flagged state
  public void flagCell() {
    if (this.isFlagged) {
      this.isFlagged = false;
    }
    else if (!this.isFlagged) {
      this.isFlagged = true;
    }
  }

  // checks if a cell satisfies the losing conditions
  public boolean loseCondition() {
    return (this.containsMine && this.isRevealed);
  }

  // checks if a cell satisfies the winning conditions
  public boolean winCondition() {
    return (!this.containsMine && this.isRevealed) || (this.containsMine && !this.isRevealed);
  }

  // reveals a cell with mines if it isn't revealed
  public void revealMines() {
    if (this.containsMine && !this.isRevealed) {
      this.isRevealed = true;

    }
  }
}

// Class that defines the test method to return true if a given Cell contains a
// Mine
class FindMines implements Predicate<Cell> {
  @Override
  public boolean test(Cell t) {
    return t.containsMine;
  }
}

// class that represents examples and tests
class ExamplesMinesweeper {

  Cell c1 = new Cell();
  Cell c2 = new Cell();
  Cell c3 = new Cell();
  Cell c4 = new Cell();
  Cell c5 = new Cell();
  Cell c6 = new Cell();
  Cell c7 = new Cell();
  Cell c8 = new Cell();
  Cell c9 = new Cell();

  void reset() {
    this.c1 = new Cell();
    this.c2 = new Cell();
    this.c3 = new Cell();
    this.c4 = new Cell();
    this.c5 = new Cell();
    this.c6 = new Cell();
    this.c7 = new Cell();
    this.c8 = new Cell();
    this.c9 = new Cell();
  }

  Minesweeper m1 = new Minesweeper(3, 3, 3, new Random(5));

  // tests the constructor exceptions for minesweeper
  boolean testConstructorExceptions(Tester t) {
    // test 1 : invalid number of rows
    return t.checkConstructorException(
        // expected exception
        new IllegalArgumentException("Invalid Number of Rows"), "Minesweeper", 0, 85, 99)

        // test 2: invalid number of columns
        && t.checkConstructorException(new IllegalArgumentException("Invalid Number of Columns"),
            "Minesweeper", 30, 101, 99)
        // test 3: invalid number of rows and columns
        && t.checkConstructorException(new IllegalArgumentException("Invalid Number of Rows"),
            "Minesweeper", 0, 101, 99)
        // test 3: invalid number of mines
        && t.checkConstructorException(new IllegalArgumentException("Invalid Number of Mines"),
            "Minesweeper", 10, 10, 101);
  }

  // tests all cell methods, building up to a completely constructed cell
  void testCell(Tester t) {
    this.reset();

    // test addToNeighbors Method
    this.c1.addToNeighbors(c2);
    this.c1.addToNeighbors(c4);
    this.c1.addToNeighbors(c5);

    this.c2.addToNeighbors(c1);
    this.c2.addToNeighbors(c3);
    this.c2.addToNeighbors(c4);
    this.c2.addToNeighbors(c5);
    this.c2.addToNeighbors(c6);

    // test addAMine method
    this.c3.addAMine();
    this.c4.addAMine();

    // test countMines Method
    this.c1.countMines();
    this.c2.countMines();

    WorldScene mtw = new WorldScene(IUtils.CELL_SIZE, IUtils.CELL_SIZE);
    mtw.placeImageXY(IUtils.CELL_COVERED, 25, 25);

    WorldScene nmtw = new WorldScene(IUtils.CELL_SIZE, IUtils.CELL_SIZE);
    nmtw.placeImageXY(new OverlayImage(IUtils.MINE, IUtils.CELL_REVEALED), 25, 25);

    t.checkExpect(this.c1.neighbors, new ArrayList<Cell>(Arrays.asList(this.c2, this.c4, this.c5)));
    t.checkExpect(this.c2.neighbors,
        new ArrayList<Cell>(Arrays.asList(this.c1, this.c3, this.c4, this.c5, this.c6)));
    t.checkExpect(this.c4.containsMine, true);
    t.checkExpect(this.c3.containsMine, true);
    t.checkExpect(this.c2.containsMine, false);
    t.checkExpect(this.c1.numMines, 1);
    t.checkExpect(this.c2.numMines, 2);
    t.checkExpect(this.c1.colorNum(this.c1.numMines), Color.BLUE); // test colorNum Method
    t.checkExpect(this.c2.colorNum(this.c2.numMines), Color.GREEN);
    t.checkExpect(this.c1.displayNum(), new TextImage("1", Color.BLUE)); // test displayNum Method
    t.checkExpect(this.c2.displayNum(), new TextImage("2", Color.GREEN));

    this.c4.isRevealed = true;

    // test drawCell Method
    t.checkExpect(c2.drawCell(new WorldScene(IUtils.CELL_SIZE, IUtils.CELL_SIZE), 25, 25), mtw);
    t.checkExpect(c4.drawCell(new WorldScene(IUtils.CELL_SIZE, IUtils.CELL_SIZE), 25, 25), nmtw);

  }

  // test Cell methods from Part 2
  void testMoreCell(Tester t) {
    this.reset();

    c7.addAMine();

    t.checkExpect(c7.containsMine, true);

    this.c3.addToNeighbors(c2);
    this.c3.addToNeighbors(c5);
    this.c3.addToNeighbors(c6);

    // test reveal method
    c3.reveal();

    // test to see if flood filling works
    t.checkExpect(c3.isRevealed, true);
    t.checkExpect(c2.isRevealed, true);
    t.checkExpect(c5.isRevealed, true);
    t.checkExpect(c6.isRevealed, true);

    // test flagCell method
    c4.flagCell();
    t.checkExpect(c4.isFlagged, true);

    // test winCondition for a covered cell without mines
    t.checkExpect(c1.winCondition(), false);

    c1.reveal();

    t.checkExpect(c1.winCondition(), true); // test winCondition for an unconvered cell without
    // mines
    t.checkExpect(c7.winCondition(), true); // test winCondition for a covered cell with mines

    t.checkExpect(c7.loseCondition(), false); // test loseCondition for a covered cell with mines
    c7.reveal();
    t.checkExpect(c7.loseCondition(), true); // test loseCondition for an uncovered cell with mines

    c4.addAMine();

    // test revealMines method
    c4.revealMines();
    c9.revealMines();

    t.checkExpect(c4.isRevealed, true);
    t.checkExpect(c9.isRevealed, false);
  }

  // tests all minesweeper methods, building up to a completely constructed
  // minesweeper WorldScene
  void testMinesweeper(Tester t) {
    this.reset();

    t.checkExpect(m1.grid, new ArrayList<ArrayList<Cell>>());

    // test makeGrid method
    m1.makeGrid();

    t.checkExpect(this.m1.grid,
        new ArrayList<ArrayList<Cell>>(
            Arrays.asList(new ArrayList<Cell>(Arrays.asList(this.c1, this.c2, this.c3)),
                new ArrayList<Cell>(Arrays.asList(this.c4, this.c5, this.c6)),
                new ArrayList<Cell>(Arrays.asList(this.c7, this.c8, this.c9)))));

    t.checkExpect(this.m1.grid.get(0).get(0).neighbors, new ArrayList<Cell>());

    // test findNeighbors method
    m1.findNeighbors();

    this.c1.neighbors = new ArrayList<Cell>(Arrays.asList(this.c4, this.c2, this.c5));
    this.c2.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.c5, this.c1, this.c3, this.c4, this.c6));
    this.c3.neighbors = new ArrayList<Cell>(Arrays.asList(this.c6, this.c2, this.c5));
    this.c4.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.c1, this.c7, this.c5, this.c8, this.c2));
    this.c5.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.c2, this.c8, this.c4, this.c6, this.c1, this.c7, this.c9, this.c3));
    this.c6.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.c3, this.c9, this.c5, this.c2, this.c8));
    this.c7.neighbors = new ArrayList<Cell>(Arrays.asList(this.c4, this.c8, this.c5));
    this.c8.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.c5, this.c7, this.c9, this.c4, this.c6));
    this.c9.neighbors = new ArrayList<Cell>(Arrays.asList(this.c6, this.c8, this.c5));

    t.checkExpect(this.m1.grid.get(0).get(0).neighbors,
        new ArrayList<Cell>(Arrays.asList(this.c4, this.c2, this.c5)));
    t.checkExpect(this.m1.grid.get(0).get(1).neighbors,
        new ArrayList<Cell>(Arrays.asList(this.c5, this.c1, this.c3, this.c4, this.c6)));
    t.checkExpect(this.m1.grid.get(0).get(2).neighbors,
        new ArrayList<Cell>(Arrays.asList(this.c6, this.c2, this.c5)));
    t.checkExpect(this.m1.grid.get(1).get(0).neighbors,
        new ArrayList<Cell>(Arrays.asList(this.c1, this.c7, this.c5, this.c8, this.c2)));
    t.checkExpect(this.m1.grid.get(1).get(1).neighbors, new ArrayList<Cell>(
        Arrays.asList(this.c2, this.c8, this.c4, this.c6, this.c1, this.c7, this.c9, this.c3)));

    // test addMines Method
    m1.addMines();
    t.checkExpect(this.m1.grid.get(0).get(0).containsMine, true);
    t.checkExpect(this.m1.grid.get(0).get(1).containsMine, true);
    t.checkExpect(this.m1.grid.get(1).get(0).containsMine, true);
    t.checkExpect(this.m1.grid.get(0).get(2).containsMine, false);
    t.checkExpect(this.m1.grid.get(1).get(1).containsMine, false);
    t.checkExpect(this.m1.grid.get(1).get(2).containsMine, false);
    t.checkExpect(this.m1.grid.get(2).get(0).containsMine, false);
    t.checkExpect(this.m1.grid.get(2).get(1).containsMine, false);
    t.checkExpect(this.m1.grid.get(2).get(2).containsMine, false);

    // test countAllMines method
    m1.countAllMines();
    t.checkExpect(this.m1.grid.get(0).get(0).numMines, 2);
    t.checkExpect(this.m1.grid.get(0).get(1).numMines, 2);
    t.checkExpect(this.m1.grid.get(1).get(0).numMines, 2);
    t.checkExpect(this.m1.grid.get(0).get(2).numMines, 1);
    t.checkExpect(this.m1.grid.get(1).get(1).numMines, 3);
    t.checkExpect(this.m1.grid.get(1).get(2).numMines, 1);
    t.checkExpect(this.m1.grid.get(2).get(0).numMines, 1);
    t.checkExpect(this.m1.grid.get(2).get(1).numMines, 1);
    t.checkExpect(this.m1.grid.get(2).get(2).numMines, 0);

    this.m1.grid.get(0).get(0).isRevealed = true;
    this.m1.grid.get(1).get(1).isRevealed = true;

    WorldScene scene = new WorldScene(IUtils.CELL_SIZE * 3, IUtils.CELL_SIZE * 3);
    scene.placeImageXY(new OverlayImage(IUtils.MINE, IUtils.CELL_REVEALED), 25, 25);
    scene.placeImageXY(IUtils.CELL_COVERED, 75, 25);
    scene.placeImageXY(IUtils.CELL_COVERED, 125, 25);
    scene.placeImageXY(IUtils.CELL_COVERED, 25, 75);
    scene.placeImageXY(new OverlayImage(new TextImage("3", Color.RED), IUtils.CELL_REVEALED), 75,
        75);
    scene.placeImageXY(IUtils.CELL_COVERED, 125, 75);
    scene.placeImageXY(IUtils.CELL_COVERED, 25, 125);
    scene.placeImageXY(IUtils.CELL_COVERED, 75, 125);
    scene.placeImageXY(IUtils.CELL_COVERED, 125, 125);

    // test makeScene Method
    t.checkExpect(this.m1.makeScene(), scene);

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        m1.grid.get(i).get(j).isRevealed = false;
        m1.grid.get(i).get(j).isFlagged = false;
      }
    }
    // test onMouseClicked Method
    this.m1.onMouseClicked(new Posn(50, 50), "LeftButton");
    t.checkExpect(this.m1.grid.get(1).get(1).isRevealed, true);

    this.m1.onMouseClicked(new Posn(100, 10), "LeftButton");
    t.checkExpect(this.m1.grid.get(2).get(0).isRevealed, true);

    this.m1.onMouseClicked(new Posn(50, 100), "RightButton");
    t.checkExpect(this.m1.grid.get(1).get(2).isFlagged, true);

    this.m1.onMouseClicked(new Posn(100, 50), "RightButton");
    t.checkExpect(this.m1.grid.get(2).get(1).isFlagged, true);

    this.m1.onMouseClicked(new Posn(10, 10), "LeftButton");
    t.checkExpect(this.m1.grid.get(0).get(0).isRevealed, true);

    t.checkExpect(this.m1.grid.get(0).get(0).loseCondition(), true);
    t.checkExpect(this.m1.grid.get(2).get(2).loseCondition(), false);
    t.checkExpect(this.m1.grid.get(2).get(0).winCondition(), true);
    t.checkExpect(this.m1.grid.get(2).get(1).winCondition(), false);

    // test lastScene when losing
    WorldScene endSceneLost = new WorldScene(150, 150);

    c1.addAMine();
    c2.addAMine();
    c4.addAMine();

    c1.countMines();
    c2.countMines();
    c3.countMines();
    c4.countMines();
    c5.countMines();
    c6.countMines();
    c7.countMines();
    c8.countMines();
    c9.countMines();

    c1.isRevealed = true;
    c2.isRevealed = true;
    c3.isRevealed = false;
    c4.isRevealed = true;
    c5.isRevealed = true;
    c6.isRevealed = false;
    c7.isRevealed = true;
    c8.isRevealed = false;
    c9.isRevealed = false;

    c1.isFlagged = false;
    c2.isFlagged = false;
    c3.isFlagged = false;
    c4.isFlagged = false;
    c5.isFlagged = false;
    c6.isFlagged = true;
    c7.isFlagged = false;
    c8.isFlagged = true;
    c9.isFlagged = false;

    endSceneLost = c1.drawCell(endSceneLost, 25, 25);
    endSceneLost = c2.drawCell(endSceneLost, 25, 75);
    endSceneLost = c3.drawCell(endSceneLost, 25, 125);
    endSceneLost = c4.drawCell(endSceneLost, 75, 25);
    endSceneLost = c5.drawCell(endSceneLost, 75, 75);
    endSceneLost = c6.drawCell(endSceneLost, 75, 125);
    endSceneLost = c7.drawCell(endSceneLost, 125, 25);
    endSceneLost = c8.drawCell(endSceneLost, 125, 75);
    endSceneLost = c9.drawCell(endSceneLost, 125, 125);

    WorldImage text = new OverlayImage(new TextImage("you lost!", IUtils.CELL_SIZE, Color.BLACK),
        new RectangleImage(IUtils.CELL_SIZE * 5, IUtils.CELL_SIZE, OutlineMode.SOLID, Color.WHITE));

    endSceneLost.placeImageXY(text, 75, 75);

    t.checkExpect(this.m1.lastScene("you lost!"), endSceneLost);

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        m1.grid.get(i).get(j).isRevealed = false;
        m1.grid.get(i).get(j).isFlagged = false;
      }
    }

    // test lastScene when winning

    this.m1.onMouseClicked(new Posn(10, 100), "LeftButton");
    t.checkExpect(this.m1.grid.get(0).get(2).isRevealed, true);

    this.m1.onMouseClicked(new Posn(50, 50), "LeftButton");
    t.checkExpect(this.m1.grid.get(1).get(1).isRevealed, true);

    this.m1.onMouseClicked(new Posn(50, 100), "LeftButton");
    t.checkExpect(this.m1.grid.get(1).get(2).isRevealed, true);

    this.m1.onMouseClicked(new Posn(100, 10), "LeftButton");
    t.checkExpect(this.m1.grid.get(2).get(0).isRevealed, true);

    this.m1.onMouseClicked(new Posn(100, 50), "LeftButton");
    t.checkExpect(this.m1.grid.get(2).get(1).isRevealed, true);

    this.m1.onMouseClicked(new Posn(100, 100), "LeftButton");
    t.checkExpect(this.m1.grid.get(2).get(2).isRevealed, true);

    WorldScene endSceneWin = new WorldScene(150, 150);

    c1.isRevealed = false;
    c2.isRevealed = false;
    c3.isRevealed = true;
    c4.isRevealed = false;
    c5.isRevealed = true;
    c6.isRevealed = true;
    c7.isRevealed = true;
    c8.isRevealed = true;
    c9.isRevealed = true;

    c1.isFlagged = false;
    c2.isFlagged = false;
    c3.isFlagged = false;
    c4.isFlagged = false;
    c5.isFlagged = false;
    c6.isFlagged = false;
    c7.isFlagged = false;
    c8.isFlagged = false;
    c9.isFlagged = false;

    endSceneLost = c1.drawCell(endSceneWin, 25, 25);
    endSceneLost = c2.drawCell(endSceneWin, 25, 75);
    endSceneLost = c3.drawCell(endSceneWin, 25, 125);
    endSceneLost = c4.drawCell(endSceneWin, 75, 25);
    endSceneLost = c5.drawCell(endSceneWin, 75, 75);
    endSceneLost = c6.drawCell(endSceneWin, 75, 125);
    endSceneLost = c7.drawCell(endSceneWin, 125, 25);
    endSceneLost = c8.drawCell(endSceneWin, 125, 75);
    endSceneLost = c9.drawCell(endSceneWin, 125, 125);

    WorldImage textWin = new OverlayImage(new TextImage("you won!", IUtils.CELL_SIZE, Color.BLACK),
        new RectangleImage(IUtils.CELL_SIZE * 5, IUtils.CELL_SIZE, OutlineMode.SOLID, Color.WHITE));

    endSceneLost.placeImageXY(textWin, 75, 75);

    t.checkExpect(this.m1.lastScene("you won!"), endSceneWin);

  }

  // tests the game
  void testGame(Tester t) {
    Minesweeper m = new Minesweeper(18, 14, 30);
    for (int i = 0; i < m.rows; i++) {
      for (int j = 0; j < m.columns; j++) {
        m.grid.get(i).get(j).isRevealed = false;
      }
    }
    m.bigBang(IUtils.CELL_SIZE * m.rows, IUtils.CELL_SIZE * m.columns);
  }
}
