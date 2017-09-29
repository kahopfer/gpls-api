package com.designteam1.repository;

import com.designteam1.model.Guardian;

import java.util.List;
import java.util.Optional;

public interface GuardianRepository {
    List<Guardian> getGuardians(final String FamilyUnitID);

    Optional<Guardian> getGuardian(final String id);

    Guardian createGuardian(Guardian guardian);

    //TODO: Add Update and Delete
}
