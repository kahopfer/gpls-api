package com.designteam1.repository;

import com.designteam1.model.Child;

import java.util.List;
import java.util.Optional;

public interface ChildRepository {
    List<Child> getChildren();

    Optional<Child> getChild(final String id);

    //TODO: Add create, update, and delete
}
