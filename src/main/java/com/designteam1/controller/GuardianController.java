package com.designteam1.controller;

import com.designteam1.model.Family;
import com.designteam1.model.Guardian;
import com.designteam1.model.Guardians;
import com.designteam1.repository.FamilyRepository;
import com.designteam1.repository.GuardianRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

    public GuardianController(final GuardianRepository guardianRepository, final FamilyRepository familyRepository) {
        this.guardianRepository = guardianRepository;
        this.familyRepository = familyRepository;
    }

    @Autowired
    private GuardianRepository guardianRepository;

    @Autowired
    private FamilyRepository familyRepository;

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

    // SecPhone and middle initial are not required
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Guardian> createGuardian(@RequestBody final Guardian guardian) {
        try {
            if (guardian == null || StringUtils.isBlank(guardian.getFname()) || StringUtils.isBlank(guardian.getLname())
                    || StringUtils.isBlank(guardian.getRelationship()) ||
                    StringUtils.isBlank(guardian.getPrimPhone()) || StringUtils.isBlank(guardian.getEmail()) ||
                    StringUtils.isBlank(guardian.getFamilyUnitID()) || StringUtils.isBlank(guardian.get_id())) {
                logger.error("Error in 'createGuardian': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Family> guardianFamily = familyRepository.getFamily(guardian.getFamilyUnitID());
                if (guardianFamily.isPresent()) {
                    guardianFamily.get().getGuardians().add(guardian.get_id());
                    Family familyResult = familyRepository.updateFamily(guardianFamily.get().get_id(), guardianFamily.get());
                    if (familyResult == null) {
                        logger.error("Error in 'createGuardian': error adding ID to family record");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        Guardian guardian1 = guardianRepository.createGuardian(guardian);
                        if (guardian1 == null || guardian1.get_id() == null) {
                            logger.error("Error in 'createGuardian': error creating guardian");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                        } else {
                            HttpHeaders header = new HttpHeaders();
                            header.add("location", guardian1.get_id());
                            return new ResponseEntity<Guardian>(null, header, HttpStatus.CREATED);
                        }
                    }
                } else {
                    logger.error("Error in 'createGuardian': could not find family associated to guardian");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createGuardian', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(value = "enrollGuardian", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Guardian> enrollGuardian(@RequestBody final Guardian guardian) {
        try {
            if (guardian == null || StringUtils.isBlank(guardian.getFname()) || StringUtils.isBlank(guardian.getLname())
                    || StringUtils.isBlank(guardian.getRelationship()) ||
                    StringUtils.isBlank(guardian.getPrimPhone()) || StringUtils.isBlank(guardian.getEmail()) ||
                    StringUtils.isBlank(guardian.getFamilyUnitID()) || StringUtils.isBlank(guardian.get_id())) {
                logger.error("Error in 'createGuardian': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Guardian guardian1 = guardianRepository.createGuardian(guardian);
                if (guardian1 == null || guardian1.get_id() == null) {
                    logger.error("Error in 'createGuardian': error enrolling guardian");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", guardian1.get_id());
                    return new ResponseEntity<Guardian>(null, header, HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createGuardian', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // SecPhone and middle initial are not required
    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Guardian> updateGuardian(@PathVariable(name = "id") final String id, @RequestBody final Guardian guardian) {
        try {
            if (guardian == null || id == null || StringUtils.isBlank(guardian.getFname()) || StringUtils.isBlank(guardian.getLname())
                    || StringUtils.isBlank(guardian.getRelationship()) ||
                    StringUtils.isBlank(guardian.getPrimPhone()) || StringUtils.isBlank(guardian.getEmail()) ||
                    StringUtils.isBlank(guardian.getFamilyUnitID()) || StringUtils.isBlank(guardian.get_id())) {
                logger.error("Error in 'updateGuardian': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(guardian.get_id())) {
                logger.error("Error in 'updateGuardian': id parameter does not match id in guardian");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Guardian> guardianOptional = guardianRepository.getGuardian(id);
                if (!guardianOptional.isPresent()) {
                    return this.createGuardian(guardian);
                } else {
                    Guardian result = guardianRepository.updateGuardian(id, guardian);
                    if (result == null) {
                        logger.error("Error in 'updateGuardian': error building guardian");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateGuardian', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteGuardian(@PathVariable(name = "id") final String id) {
        try {
            Optional<Guardian> guardian = guardianRepository.getGuardian(id);
            if (guardian.isPresent()) {
                Optional<Family> guardianFamily = familyRepository.getFamily(guardian.get().getFamilyUnitID());
                if (guardianFamily.isPresent()) {
                    if (guardianFamily.get().getGuardians().size() == 1) {
                        logger.error("Error in 'deleteGuardian': a family must have at least 1 guardian");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    // Remove guardianID from family record
                    guardianFamily.get().getGuardians().removeIf(s -> s.equals(guardian.get().get_id()));
                    Family familyResult = familyRepository.updateFamily(guardianFamily.get().get_id(), guardianFamily.get());
                    if (familyResult == null) {
                        logger.error("Error in 'deleteGuardian': error updating family record");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        Guardian result = guardianRepository.deleteGuardian(guardian.get());
                        if (result == null) {
                            logger.error("Error in 'deleteGuardian': error deleting guardian");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                        } else {
                            return ResponseEntity.status(HttpStatus.OK).body(null);
                        }
                    }
                } else {
                    logger.error("Error in 'deleteGuardian': cannot find family associated to guardian");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
            } else {
                logger.error("Error in 'deleteGuardian': guardian is null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteGuardian', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
