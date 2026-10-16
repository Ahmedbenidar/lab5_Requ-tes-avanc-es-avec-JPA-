package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.util.PaginationResult;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class SalleServiceImpl implements SalleService {

    @Inject
    private SalleRepository salleRepository;

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        // garde-fou metier : un creneau doit avoir un debut strictement avant la fin
        if (debut == null || fin == null || !debut.isBefore(fin)) {
            throw new IllegalArgumentException("Le creneau demande est invalide");
        }
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin,
                                   LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        return salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin,
                           LocalDateTime debut, LocalDateTime fin) {
        return salleRepository.countRooms(nom, capaciteMin, debut, fin);
    }

    @Override
    public PaginationResult<Salle> rechercherPage(String nom, Integer capaciteMin,
                                                  LocalDateTime debut, LocalDateTime fin,
                                                  int page, int size) {
        List<Salle> resultats = salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
        long total = salleRepository.countRooms(nom, capaciteMin, debut, fin);
        return new PaginationResult<Salle>(resultats, page, size, total);
    }

    /**
     * Hors conteneur EJB : la classe App injecte elle-meme les dependances.
     */
    public void setEntityManager(EntityManager entityManager) {
        SalleRepositoryImpl repository = new SalleRepositoryImpl();
        repository.setEntityManager(entityManager);
        this.salleRepository = repository;
    }
}
