package com.example.util;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jeu de donnees de depart, separe de App pour garder le main court.
 */
public final class DonneesDemo {

    private DonneesDemo() {
        // classe d'outils : pas d'instance
    }

    /**
     * Cree 6 salles puis pose 3 reservations le matin du 1er juin 2024.
     * Retourne les salles indexees par leur nom.
     */
    public static Map<String, Salle> initialiser(EntityManager em) {
        Map<String, Salle> salles = new LinkedHashMap<String, Salle>();

        em.getTransaction().begin();
        for (int i = 0; i < 6; i++) {
            String nom = "Salle " + (char) ('A' + i);
            // les salles grossissent de 5 places a chaque fois
            Salle salle = new Salle(nom, 15 + i * 5);
            em.persist(salle);
            salles.put(nom, salle);
        }
        em.getTransaction().commit();

        reserver(em, salles.get("Salle A"), 9, 11);
        reserver(em, salles.get("Salle C"), 10, 12);
        reserver(em, salles.get("Salle E"), 8, 9);

        return salles;
    }

    private static void reserver(EntityManager em, Salle salle, int heureDebut, int heureFin) {
        if (salle == null) {
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setDebut(LocalDateTime.of(2024, 6, 1, heureDebut, 0));
        reservation.setFin(LocalDateTime.of(2024, 6, 1, heureFin, 0));
        salle.ajouterReservation(reservation);

        em.getTransaction().begin();
        em.persist(reservation);
        em.getTransaction().commit();
    }
}
