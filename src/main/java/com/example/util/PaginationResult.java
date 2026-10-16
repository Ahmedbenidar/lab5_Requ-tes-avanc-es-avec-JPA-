package com.example.util;

import java.util.Collections;
import java.util.List;

/**
 * Petit conteneur generique pour transporter une page de resultats
 * avec ses infos de navigation.
 */
public class PaginationResult<T> {

    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;

    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = calculerNombreDePages(totalItems, pageSize);
    }

    /**
     * Arrondi au superieur : 5 elements avec des pages de 2 donnent 3 pages.
     */
    private static int calculerNombreDePages(long totalItems, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() {
        return items == null ? Collections.<T>emptyList() : items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public boolean estVide() {
        return getItems().isEmpty();
    }

    public boolean aUnePageSuivante() {
        return currentPage < totalPages;
    }
}
