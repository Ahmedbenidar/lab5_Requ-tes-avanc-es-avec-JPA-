package com.example.repository;

import com.example.model.Salle;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acces aux salles : tout ce qui touche au fetch des donnees est ici.
 */
public interface SalleRepository {

    /**
     * Salles qui n'ont aucune reservation sur le creneau donne.
     */
    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    /**
     * Recherche multi-criteres avec pagination. Un critere null est ignore.
     */
    List<Salle> searchRooms(String nom, Integer capaciteMin,
                            LocalDateTime debut, LocalDateTime fin,
                            int page, int size);

    /**
     * Meme filtres que searchRooms, mais on ne renvoie que le total.
     */
    long countRooms(String nom, Integer capaciteMin,
                    LocalDateTime debut, LocalDateTime fin);
}
