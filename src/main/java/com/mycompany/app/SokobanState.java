package com.mycompany.app;

import java.util.*;

class SokobanState {

    private static char[][] STATIC_BOARD;
    private static Set<Coord> TARGETS = new HashSet<>();
    private int rows, cols;

    public char[][] board;
    public int playerR, playerC;
    public Set<BoxCoord> boxCoords; // Set de BoxCoord pour suivre les positions et noms

    public int g_cost;
    public int h_cost;
    public int f_cost;

    public SokobanState parent;
    public String action;

    /**
     * @brief Constructeur initial.
     */
    public SokobanState(String[] level) {
        this.rows = level.length;
        this.cols = level[0].length();
        this.board = new char[rows][cols];
        this.boxCoords = new HashSet<>();

        if (STATIC_BOARD == null) {
            STATIC_BOARD = new char[rows][cols];
        }

        // Cette boucle analyse la carte de niveau initiale pour séparer les éléments statiques (murs et emplacements des cibles)
        // //des éléments mobiles (joueur et caisses nommées), initialisant ainsi l'état de départ du puzzle Sokoban pour la recherche A*
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char cell = level[r].charAt(c);

                // Définition de la grille statique et des cibles
                if (cell == SokobanSolver.WALL) {
                    STATIC_BOARD[r][c] = SokobanSolver.WALL;
                } else if (cell == SokobanSolver.TARGET || cell == SokobanSolver.PLAYER_ON_TARGET || SokobanSolver.isBoxSymbol(cell)) {
                    if (cell == SokobanSolver.TARGET || (cell >= 'A' && cell <= 'D') || cell == SokobanSolver.PLAYER_ON_TARGET) {
                        // Seules les cibles réelles (T) ou les cases qui les contiennent sont traitées
                        STATIC_BOARD[r][c] = SokobanSolver.TARGET;
                        TARGETS.add(new Coord(r, c));
                    } else {
                        STATIC_BOARD[r][c] = SokobanSolver.FLOOR;
                    }
                } else {
                    STATIC_BOARD[r][c] = SokobanSolver.FLOOR;
                }

                // Définition des éléments mobiles
                if (cell == SokobanSolver.PLAYER || cell == SokobanSolver.PLAYER_ON_TARGET) {
                    this.playerR = r;
                    this.playerC = c;
                    this.board[r][c] = cell;
                } else if (SokobanSolver.isBoxSymbol(cell)) {
                    // Utilisez getBoxName pour stocker le nom en minuscule
                    this.boxCoords.add(new BoxCoord(r, c, SokobanSolver.getBoxName(cell)));
                    this.board[r][c] = cell;
                } else {
                    this.board[r][c] = cell;
                }
            }
        }

        this.g_cost = 0;
        this.h_cost = calculateHeuristic();
        this.f_cost = this.g_cost + this.h_cost;
    }

    /**
     * @brief Constructeur de copie.
     */
    public SokobanState(SokobanState other) {
        // ... (Copie des attributs simples) ...
        this.rows = other.rows;
        this.cols = other.cols;
        this.g_cost = other.g_cost;
        this.parent = other;
        this.action = other.action;

        // Copie profonde du plateau
        this.board = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, cols);
        }

        // Copie profonde de BoxCoord
        this.boxCoords = new HashSet<>();
        for (BoxCoord box : other.boxCoords) {
            this.boxCoords.add(new BoxCoord(box.r, box.c, box.name));
        }
        this.playerR = other.playerR;
        this.playerC = other.playerC;
    }

    // Fonction pour créer une clé unique pour le Set
    public String getUniqueKey() {
        StringBuilder sb = new StringBuilder();
        sb.append("P").append(playerR).append(",").append(playerC);

        List<BoxCoord> sortedBoxes = new ArrayList<>(boxCoords);
        Collections.sort(sortedBoxes);

        for (BoxCoord c : sortedBoxes) {
            sb.append("_B").append(c.name).append("(").append(c.r).append(",").append(c.c).append(")");
        }
        return sb.toString();
    }

    // Vérifie si toutes les caisses sont sur une cible.
    public boolean isGoal() {
        // Le but est atteint si toutes les coordonnées des caisses correspondent
        // aux coordonnées des cibles.
        for (BoxCoord box : boxCoords) {
            if (!TARGETS.contains(new Coord(box.r, box.c))) {
                return false;
            }
        }
        return true;
    }

    //Calcule l'heuristique h(n) (Somme des distances de Manhattan minimales)
    private int calculateHeuristic() {
        int h = 0;

        // On utilise la liste des caisses (BoxCoord) et la liste des cibles (Coord)
        for (BoxCoord box : boxCoords) {
            int minManhattan = Integer.MAX_VALUE;

            for (Coord target : TARGETS) {
                // Seulement si la caisse n'est pas DEJA sur cette cible, ou on ajoute 0
                if (!(box.r == target.r && box.c == target.c)) {
                    int manhattan = Math.abs(box.r - target.r) + Math.abs(box.c - target.c);
                    if (manhattan < minManhattan) {
                        minManhattan = manhattan;
                    }
                } else {
                    minManhattan = 0; // Si la caisse est déjà sur une cible
                    break;
                }
            }
            h += minManhattan;
        }
        return h;
    }

    //Génère les successeurs valides
    public List<SokobanState> generateSuccessors() {
        List<SokobanState> successors = new ArrayList<>();

        for (int i = 0; i < 4; i++) { // Itération sur les 4 directions
            int dr = SokobanSolver.DIRS[i][0];
            int dc = SokobanSolver.DIRS[i][1];
            String actionDirection = SokobanSolver.DIR_NAMES[i];

            int nextPR = playerR + dr; // Position adjacente (là où le joueur veut aller)
            int nextPC = playerC + dc;

            if (!SokobanSolver.isValid(nextPR, nextPC, rows, cols) || STATIC_BOARD[nextPR][nextPC] == SokobanSolver.WALL) {
                continue;
            }

            char cellAtNextPos = this.board[nextPR][nextPC];

            // --- Cas 1 : Poussée de Caisse ---
            if (SokobanSolver.isBoxSymbol(cellAtNextPos)) {

                int targetR = nextPR + dr; // Position derrière la caisse (cible de la poussée)
                int targetC = nextPC + dc;

                // Vérification de la cible de la poussée
                if (SokobanSolver.isValid(targetR, targetC, rows, cols) &&
                        STATIC_BOARD[targetR][targetC] != SokobanSolver.WALL &&
                        !SokobanSolver.isBoxSymbol(this.board[targetR][targetC]))
                {
                    // Poussée valide
                    SokobanState newState = new SokobanState(this);
                    newState.g_cost += 1;

                    // Récupération de l'objet BoxCoord actuel
                    Coord oldBoxPos = new Coord(nextPR, nextPC);
                    BoxCoord boxToPush = newState.boxCoords.stream()
                            .filter(b -> b.r == oldBoxPos.r && b.c == oldBoxPos.c)
                            .findFirst().orElseThrow(() -> new IllegalStateException("Caisse non trouvée"));

                    // L'action utilise le nom de la caisse
                    newState.action = "PUSH '" + boxToPush.name + "' " + actionDirection;

                    Coord oldPlayerPos = new Coord(playerR, playerC);
                    Coord newBoxPos = new Coord(targetR, targetC);

                    // 1. Mise à jour des coordonnées des caisses (Set)
                    newState.boxCoords.remove(boxToPush);
                    newState.boxCoords.add(new BoxCoord(newBoxPos.r, newBoxPos.c, boxToPush.name));

                    // 2. Mise à jour de la position du joueur
                    newState.playerR = nextPR;
                    newState.playerC = nextPC;

                    // 3. Mise à jour des symboles sur le plateau (GRILLE)
                    // A. Ancienne position du Joueur
                    newState.board[oldPlayerPos.r][oldPlayerPos.c] = (STATIC_BOARD[oldPlayerPos.r][oldPlayerPos.c] == SokobanSolver.TARGET) ? SokobanSolver.TARGET : SokobanSolver.FLOOR;

                    // B. Ancienne position de la Caisse (maintenant Joueur)
                    newState.board[oldBoxPos.r][oldBoxPos.c] = (STATIC_BOARD[oldBoxPos.r][oldBoxPos.c] == SokobanSolver.TARGET) ? SokobanSolver.PLAYER_ON_TARGET : SokobanSolver.PLAYER;

                    // C. Nouvelle position de la Caisse (nom en Majuscule si sur cible)
                    char newBoxChar = (STATIC_BOARD[newBoxPos.r][newBoxPos.c] == SokobanSolver.TARGET)
                            ? SokobanSolver.BOX_TO_TARGET_MAP.get(boxToPush.name)
                            : boxToPush.name;

                    newState.board[newBoxPos.r][newBoxPos.c] = newBoxChar;

                    // 4. Calcul des coûts
                    newState.h_cost = newState.calculateHeuristic();
                    newState.f_cost = newState.g_cost + newState.h_cost;

                    successors.add(newState);
                }
            }

            // --- Cas 2 : Mouvement Simple du Joueur ---
            else {
                // ... (Logique inchangée pour le MOVE) ...
                SokobanState newState = new SokobanState(this);
                newState.action = "MOVE " + actionDirection;

                Coord oldPlayerPos = new Coord(playerR, playerC);

                newState.playerR = nextPR;
                newState.playerC = nextPC;

                newState.board[oldPlayerPos.r][oldPlayerPos.c] = (STATIC_BOARD[oldPlayerPos.r][oldPlayerPos.c] == SokobanSolver.TARGET) ? SokobanSolver.TARGET : SokobanSolver.FLOOR;
                newState.board[nextPR][nextPC] = (STATIC_BOARD[nextPR][nextPC] == SokobanSolver.TARGET) ? SokobanSolver.PLAYER_ON_TARGET : SokobanSolver.PLAYER;

                newState.h_cost = newState.calculateHeuristic();
                newState.f_cost = newState.g_cost + newState.h_cost;

                successors.add(newState);
            }
        }
        return successors;
    }
}