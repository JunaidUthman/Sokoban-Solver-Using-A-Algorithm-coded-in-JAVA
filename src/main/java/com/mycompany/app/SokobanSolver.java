package com.mycompany.app;

import java.util.*;

public class SokobanSolver {


    // --- SYMBOLES DU JEU ---
    public static final char WALL = '■';
    public static final char TARGET = 'T';
    public static final char PLAYER = '@';
    public static final char FLOOR = '□';
    public static final char PLAYER_ON_TARGET = '+';

    // les caisses dans un emplacement autre que le target
    public static final char[] BOX_NAMES = {'a', 'b', 'c', 'd'};
    //les caisses dans leurs target
    public static final char[] BOX_ON_TARGET_NAMES = {'A', 'B', 'C', 'D'};


    // utilisé pour convertire du a -> A si la caiise est poussé dans le target et vice versa
    public static final Map<Character, Character> BOX_TO_TARGET_MAP = Map.of(
            'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D',
            'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd' // Correction ici pour D -> d
    );

    public static final Set<Character> ALL_BOX_SYMBOLS = new HashSet<>(Arrays.asList('a', 'b', 'c', 'd', 'A', 'B', 'C', 'D'));

    public static final int[][] DIRS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };
    //les noms des directions
    public static final String[] DIR_NAMES = {
            "UP", "DOWN", "LEFT", "RIGHT"
    };

    // --------- METHODES PRINCIPALES ------------------

    public static void main(String[] args) {
        // les tests donnée dans le devoire
        System.out.println("--- Résolution Sokoban: 1er test  ---");
        String[] test1 = {
                "■■■■■■■■■■",
                "■□□□□□□□□■",
                "■□■■□■■□□■",
                "■□a□T□b□□■",
                "■□■□@□■□□■",
                "■□c□T□d□□■",
                "■□■■□■■□□■",
                "■□□T□□T□□■",
                "■□□□□□□□□■",
                "■■■■■■■■■■"
        };
        solve(test1, "Test 1");

        System.out.println("\n--- Résolution Sokoban 2emme (Caisses Nommées) ---");
        String[] test2 = {
                "■■■■■■■■■■",
                "■T□■□□■□T■",
                "■□■a□□b■□■",
                "■□■□□□□■□■",
                "■□□□@□□□□■",
                "■□■□□□□■□■",
                "■□■c□□d■□■",
                "■T□■□□■□T■",
                "■□□□□□□□□■",
                "■■■■■■■■■■"
        };
        solve(test2, "Test 2");
    }

    //cette fo,ction implémente l'algorithme de recherche A* pour trouver la séquence de mouvements (poussées) la plus courte et la plus efficace pour résoudre le niveau.
    public static void solve(String[] level, String name) {
        // ... logic de A*
        long startTime = System.currentTimeMillis();
        SokobanState initialState = new SokobanState(level);
        if (initialState.isGoal()) {
            System.out.println(name + " : Déjà résolu!");
            return;
        }

        PriorityQueue<SokobanState> openList = new PriorityQueue<>(Comparator.comparingInt(s -> s.f_cost));
        Set<String> closedList = new HashSet<>();

        openList.add(initialState);

        int exploredNodes = 0;


        //Tant qu'il y a des états à explorer, prend le meilleur état : Si cet état est l'objectif (current.isGoal()), c'est gagné !
        //Sinon, ajoute cet état à la closedList et génèrer ses successeurs
        while (!openList.isEmpty()) {
            SokobanState current = openList.poll();
            exploredNodes++;

            if (closedList.contains(current.getUniqueKey())) {
                continue;
            }

            if (current.isGoal()) {
                long endTime = System.currentTimeMillis();
                System.out.println("\n" + name + " - Solution trouvée!");
                System.out.println("Temps de résolution: " + (endTime - startTime) + " ms");
                System.out.println("Nombre de nœuds explorés par A*: " + exploredNodes);
                printSolution(current, initialState); // Passer initialState pour l'affichage initial
                return;
            }

            closedList.add(current.getUniqueKey());

            for (SokobanState nextState : current.generateSuccessors()) {
                if (!closedList.contains(nextState.getUniqueKey())) {
                    openList.add(nextState);
                }
            }
        }
        System.out.println("\n" + name + " - Solution non trouvée!");
    }


    //fonction pour afficher les resultats
    private static void printSolution(SokobanState goalState, SokobanState initialState) {
        LinkedList<SokobanState> solutionStates = new LinkedList<>();
        SokobanState current = goalState;

        while (current != null) {
            solutionStates.addFirst(current);
            current = current.parent;
        }

        System.out.println("Longueur de la solution optimale (Poussées): " + goalState.g_cost);
        System.out.println("Chemin des poussées avec visualisation:");

        // Affichage de l'état initial
        System.out.println("\n--- ÉTAT INITIAL ---");
        displayBoard(initialState.board);

        int pushCount = 0;
        for (SokobanState state : solutionStates) {
            if (state.action != null && state.action.startsWith("PUSH")) {
                pushCount++;
                System.out.println("\n" + pushCount + ". " + state.action);
                displayBoard(state.board);
            }
        }
        System.out.println("\n--- FIN DE LA SOLUTION ---");
    }

    private static void displayBoard(char[][] board) {
        for (char[] row : board) {
            for (char cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }

    //vérifie si une coordonnée (r, c) se trouve bien à l'intérieur des limites de la grille
    public static boolean isValid(int r, int c, int rows, int cols) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // verifie si le symbole est une caisse(a,b,c,d)
    public static boolean isBoxSymbol(char c) {
        return ALL_BOX_SYMBOLS.contains(c);
    }

    public static char getBoxName(char c) {
        if (c >= 'a' && c <= 'd') return c;
        if (c >= 'A' && c <= 'D') return BOX_TO_TARGET_MAP.get(c);
        return ' ';
    }

}