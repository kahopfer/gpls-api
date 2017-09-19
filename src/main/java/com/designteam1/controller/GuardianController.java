package com.designteam1.controller;

import com.designteam1.model.Guardian;
import com.designteam1.model.Guardians;
import com.designteam1.repository.GuardianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("guardians")
public class GuardianController {
    private static final Logger logger = LoggerFactory.getLogger(GuardianController.class);

    public GuardianController() {

    }

    public GuardianController(final GuardianRepository guardianRepository) {
        this.guardianRepository = guardianRepository;
    }

    @Autowired
    private GuardianRepository guardianRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Guardians> getGuardians(@RequestParam(value = "familyUnitID", defaultValue = "", required = false) final String familyUnitID) {
        try {
            final Guardians guardians = new Guardians();
            final List<Guardian> guardianList = guardianRepository.getGuardians(familyUnitID);
            if (guardianList == null) {
                return ResponseEntity.ok(guardians);
            }
            guardians.setGuardians(guardianList);
            return ResponseEntity.ok(guardians);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getGuardians', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Guardian> getGuardian(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Guardian> guardian = guardianRepository.getGuardian(id);
            if (!guardian.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(guardian.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getGuardian', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
