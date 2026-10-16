package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class SalleRepositoryImpl implements SalleRepository {

    @PersistenceContext(unitName = "SallePU")
    private EntityManager entityManager;

    /**
     * Recherche "classique" en JPQL : on prend toutes les salles et on retire
     * celles qui ont une reservation qui mord sur le creneau.
     */
    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        String jpql = "SELECT s FROM Salle s "
                + "WHERE s.id NOT IN ("
                + "  SELECT r.salle.id FROM Reservation r "
                + "  WHERE (:debut < r.fin AND :fin > r.debut)"
                + ") "
                + "ORDER BY s.nom";

        TypedQuery<Salle> query = entityManager.createQuery(jpql, Salle.class);
        query.setParameter("debut", debut);
        query.setParameter("fin", fin);
        return query.getResultList();
    }

    /**
     * Recherche dynamique : on empile les conditions seulement pour les
     * criteres reellement fournis par l'utilisateur.
     */
    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin,
                                   LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);

        List<Predicate> conditions = construireConditions(cb, cq, salle,
                nom, capaciteMin, debut, fin);

        cq.select(salle)
          .where(cb.and(conditions.toArray(new Predicate[0])))
          .orderBy(cb.asc(salle.get("nom")));

        TypedQuery<Salle> query = entityManager.createQuery(cq);
        query.setFirstResult(convertirEnOffset(page, size));
        query.setMaxResults(size);
        return query.getResultList();
    }

    /**
     * Meme filtres que searchRooms, mais uniquement le COUNT.
     * Sans ca on ne peut pas calculer le nombre de pages.
     */
    @Override
    public long countRooms(String nom, Integer capaciteMin,
                           LocalDateTime debut, LocalDateTime fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Salle> salle = cq.from(Salle.class);

        List<Predicate> conditions = construireConditions(cb, cq, salle,
                nom, capaciteMin, debut, fin);

        cq.select(cb.count(salle));
        cq.where(cb.and(conditions.toArray(new Predicate[0])));

        Long total = entityManager.createQuery(cq).getSingleResult();
        return total == null ? 0L : total;
    }

    /**
     * Une page "1" doit commencer a l'index 0.
     */
    private int convertirEnOffset(int page, int size) {
        int pageValide = page < 1 ? 1 : page;
        return (pageValide - 1) * size;
    }

    /**
     * Construit la liste des predicats. Les criteres non renseignes (null ou vide)
     * sont tout simplement ignores.
     */
    private List<Predicate> construireConditions(CriteriaBuilder cb, CriteriaQuery<?> cq,
                                                 Root<Salle> salle, String nom,
                                                 Integer capaciteMin,
                                                 LocalDateTime debut, LocalDateTime fin) {
        List<Predicate> conditions = new ArrayList<Predicate>();

        // filtre sur le nom : recherche partielle, insensible a la casse
        if (nom != null && !nom.trim().isEmpty()) {
            String motif = "%" + nom.trim().toLowerCase() + "%";
            conditions.add(cb.like(cb.lower(salle.<String>get("nom")), motif));
        }

        // filtre sur la capacite minimale
        if (capaciteMin != null) {
            conditions.add(cb.ge(salle.<Integer>get("capacite"), capaciteMin));
        }

        // filtre de disponibilite : on exclut les salles deja prises sur le creneau
        if (debut != null && fin != null) {
            conditions.add(cb.not(salle.<Long>get("id").in(sousRequeteSallesOccupees(cb, cq, debut, fin))));
        }

        return conditions;
    }

    /**
     * Sous-requete : les ids des salles qui ont une reservation qui chevauche
     * le creneau demande.
     */
    private Subquery<Long> sousRequeteSallesOccupees(CriteriaBuilder cb, CriteriaQuery<?> cq,
                                                     LocalDateTime debut, LocalDateTime fin) {
        Subquery<Long> sousRequete = cq.subquery(Long.class);
        Root<Reservation> reservation = sousRequete.from(Reservation.class);

        Predicate chevauchement = cb.and(
                cb.lessThan(reservation.<LocalDateTime>get("debut"), fin),
                cb.greaterThan(reservation.<LocalDateTime>get("fin"), debut));

        sousRequete.select(reservation.<Salle>get("salle").<Long>get("id"));
        sousRequete.where(chevauchement);
        return sousRequete;
    }

    /**
     * Utilise par la classe principale (Java SE) car il n'y a pas de conteneur EJB.
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
