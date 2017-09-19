package com.designteam1.controller;

import com.designteam1.model.Families;
import com.designteam1.model.Family;
import com.designteam1.repository.FamilyRepository;
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
@RequestMapping("families")
public class FamilyController {
    private static final Logger logger = LoggerFactory.getLogger(FamilyController.class);

    public FamilyController() {

    }

    public FamilyController(final FamilyRepository familyRepository) {
        this.familyRepository = familyRepository;
    }

    @Autowired
    private FamilyRepository familyRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Families> getFamilies() {
        try {
            final Families families = new Families();
            final List<Family> familyList = familyRepository.getFamilies();
            if (familyList == null) {
                return ResponseEntity.ok(families);
            }
            families.setFamilies(familyList);
            return ResponseEntity.ok(families);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getFamilies', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Family> getFamily(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Family> family = familyRepository.getFamily(id);
            if (!family.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(family.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getFamily', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
