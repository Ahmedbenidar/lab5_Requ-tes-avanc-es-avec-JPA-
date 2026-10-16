package com.example;

import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.DonneesDemo;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Test "Java SE" du TP : on ouvre l'EntityManager a la main,
 * on charge un jeu de donnees puis on essaye les requetes.
 */
public class App {

    private static final LocalDateTime CRENEAU_DEBUT = LocalDateTime.of(2024, 6, 1, 9, 0);
    private static final LocalDateTime CRENEAU_FIN = LocalDateTime.of(2024, 6, 1, 11, 0);

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        try {
            Map<String, Salle> salles = DonneesDemo.initialiser(em);
            System.out.println("Jeu de donnees : " + salles.size() + " salles creees");

            SalleService service = new SalleServiceImpl();
            ((SalleServiceImpl) service).setEntityManager(em);

            testerSallesDisponibles(service);
            testerRechercheMultiCriteres(service);
            testerPagination(service);
        } finally {
            em.close();
            emf.close();
        }
    }

    /**
     * Requete JPQL : quelles salles sont libres sur le creneau du matin.
     */
    private static void testerSallesDisponibles(SalleService service) {
        System.out.println();
        System.out.println("=== 1. Salles libres de " + CRENEAU_DEBUT + " a " + CRENEAU_FIN + " ===");

        List<Salle> libres = service.getAvailableRooms(CRENEAU_DEBUT, CRENEAU_FIN);
        for (int i = 0; i < libres.size(); i++) {
            Salle salle = libres.get(i);
            System.out.println("  " + (i + 1) + ") " + salle.getNom()
                    + " - " + salle.getCapacite() + " places");
        }
        System.out.println("  Total : " + libres.size() + " salle(s)");
    }

    /**
     * API Criteria : on combine le nom, la capacite mini et la disponibilite.
     */
    private static void testerRechercheMultiCriteres(SalleService service) {
        System.out.println();
        System.out.println("=== 2. Recherche multi-criteres ===");

        System.out.println("[nom = \"salle\", capacite >= 25, libre le matin]");
        List<Salle> parNom = service.searchRooms("salle", 25, CRENEAU_DEBUT, CRENEAU_FIN, 1, 10);
        afficher(parNom);

        System.out.println("[sans nom, capacite >= 40, libre le matin]");
        List<Salle> parCapacite = service.searchRooms(null, 40, CRENEAU_DEBUT, CRENEAU_FIN, 1, 10);
        afficher(parCapacite);

        System.out.println("[nom = \"salle\", tous creneaux confondus]");
        List<Salle> sansCreneau = service.searchRooms("salle", null, null, null, 1, 10);
        afficher(sansCreneau);
    }

    /**
     * Pagination : on limite a 2 salles par page et on parcourt les pages.
     */
    private static void testerPagination(SalleService service) {
        System.out.println();
        System.out.println("=== 3. Pagination (2 salles par page) ===");

        int taillePage = 2;
        int page = 1;
        boolean continuer = true;

        while (continuer) {
            PaginationResult<Salle> resultat = service.rechercherPage(null, null, null, null, page, taillePage);
            System.out.println("Page " + resultat.getCurrentPage() + "/" + resultat.getTotalPages()
                    + " (" + resultat.getTotalItems() + " salles au total)");
            afficher(resultat.getItems());

            continuer = resultat.aUnePageSuivante();
            page++;
        }
    }

    private static void afficher(List<Salle> salles) {
        if (salles.isEmpty()) {
            System.out.println("  (aucun resultat)");
            return;
        }
        for (Salle salle : salles) {
            System.out.println("  - " + salle.getNom() + " (" + salle.getCapacite() + " places)");
        }
    }
}
