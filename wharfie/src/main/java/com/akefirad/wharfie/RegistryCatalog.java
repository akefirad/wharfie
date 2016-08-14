package com.akefirad.wharfie;

import com.akefirad.wharfie.utils.Asserts;

import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class RegistryCatalog extends RegistryEntity implements Iterable<RegistryRepository> {
    private int number;
    private RegistryRepository last;
    private final List<RegistryRepository> repositories;

    public RegistryCatalog ( RegistryBase parent, int number, String last, List<String> repositories ) {
        super(parent);

        Asserts.notNull(repositories, "repositories");

        this.number = number;
        this.last = last != null ? new RegistryRepository(last) : null;
        this.repositories = repositories.stream().map(RegistryRepository::new).collect(toList());
    }

    public List<RegistryRepository> getRepositories () {
        return unmodifiableList(repositories);
    }

    public int getNumber () {
        return number;
    }

    public RegistryRepository getLast () {
        return last;
    }

    @Override
    RegistryBase getParent () {
        return (RegistryBase) super.getParent();
    }

    @Override
    public Iterator<RegistryRepository> iterator () {
        return new CatalogIterator();
    }

    //TODO: Review for thread-safety
    private class CatalogIterator implements Iterator<RegistryRepository> {
        private Iterator<RegistryRepository> iterator = repositories.iterator();

        @Override
        public boolean hasNext () {
            if (iterator.hasNext()) {
                return true;
            }
            else if (last != null && number > 0) {
                RegistryCatalog next = getParent().getCatalog();
                number = next.number;
                last = next.last;

                List<RegistryRepository> additional = next.getRepositories();
                repositories.addAll(additional);

                iterator = additional.iterator();
                return iterator.hasNext();
            }
            else {
                return false;
            }
        }

        @Override
        public RegistryRepository next () {
            return iterator.next();
        }
    }
}
