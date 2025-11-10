package com.mycompany.app;

import java.util.Objects;

//La classe Coord est une structure de données générique qui stocke une simple paire de coordonnées (r, c) (ligne, colonne) et est principalement utilisée pour définir les emplacements statiques des cibles (TARGETS) sur le plateau.
class Coord implements Comparable<Coord> {
    public final int r;
    public final int c;

    public Coord(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coord coord = (Coord) o;
        return r == coord.r && c == coord.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, c);
    }

    @Override
    public int compareTo(Coord other) {
        if (this.r != other.r) {
            return Integer.compare(this.r, other.r);
        }
        return Integer.compare(this.c, other.c);
    }
}
//La classe BoxCoord est une structure de données spécialisée qui étend Coord en y ajoutant le nom unique de la caisse ('a', 'b', 'c') et est essentielle pour permettre au solveur de suivre individuellement chaque caisse dans l'état du jeu
class BoxCoord implements Comparable<BoxCoord> {
    public final int r;
    public final int c;
    public final char name; // 'a', 'b', 'c', 'd'

    public BoxCoord(int r, int c, char name) {
        this.r = r;
        this.c = c;
        // On s'assure que le nom stocké est toujours en minuscule pour l'uniformité
        this.name = (name >= 'A' && name <= 'D') ? SokobanSolver.BOX_TO_TARGET_MAP.get(name) : name;
    }

    // Le Set de caisses (boxCoords) est basé sur la position (r, c),
    // car deux caisses ne peuvent pas occuper la même position.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxCoord coord = (BoxCoord) o;
        return r == coord.r && c == coord.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, c);
    }

    // Le tri est fait d'abord par nom pour garantir que la clé unique soit stable
    @Override
    public int compareTo(BoxCoord other) {
        if (this.name != other.name) {
            return Character.compare(this.name, other.name);
        }
        if (this.r != other.r) {
            return Integer.compare(this.r, other.r);
        }
        return Integer.compare(this.c, other.c);
    }
}