package com.designteam1.repository;

import com.designteam1.model.Parent;

import java.util.List;
import java.util.Optional;

public interface ParentRepository {
    List<Parent> getParents();

    Optional<Parent> getParent(final String id);

    //TODO: Add Create, Update, and Delete
}
