package com.designteam1.repository;

import com.designteam1.model.Family;

import java.util.List;
import java.util.Optional;

public interface FamilyRepository {
    List<Family> getFamilies(final String active);

    Optional<Family> getFamily(final String id);

    Family createFamily(Family family);

    Family deleteFamily(Family family);

    Family updateFamily(String id, Family family);

    Family updateActive(String id, Family family);
}
