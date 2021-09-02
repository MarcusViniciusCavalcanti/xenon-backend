package br.edu.utfpr.tsi.xenon.structure.repository;

import org.springframework.data.jpa.domain.Specification;

public interface BasicSpecification<T, U> {

    Specification<T> filterBy(U object);

}

