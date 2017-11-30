package com.designteam1.controller;

import com.designteam1.model.ApiResponse;
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
    public ResponseEntity<ApiResponse> getGuardians(@RequestParam(value = "familyUnitID", defaultValue = "", required = false) final String familyUnitID) {
        try {
            final Guardians guardians = new Guardians();
            final List<Guardian> guardianList = guardianRepository.getGuardians(familyUnitID, "true");
            if (guardianList == null) {
                return new ApiResponse(guardians).send(HttpStatus.OK);
            }
            guardians.setGuardians(guardianList);
            return new ApiResponse(guardians).send(HttpStatus.OK);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getGuardians', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the guardians");
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getGuardian(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Guardian> guardian = guardianRepository.getGuardian(id);
            if (!guardian.isPresent()) {
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the guardian you were looking for");
            } else {
                return new ApiResponse(guardian.get()).send(HttpStatus.OK);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getGuardian', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the guardian");
        }
    }

    @GetMapping(value = "/inactive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getInactiveGuardians(@RequestParam(value = "familyUnitID", defaultValue = "", required = false) final String familyUnitID) {
        try {
            final Guardians guardians = new Guardians();
            final List<Guardian> guardianList = guardianRepository.getGuardians(familyUnitID, "false");
            if (guardianList == null) {
                return new ApiResponse(guardians).send(HttpStatus.OK);
            }
            guardians.setGuardians(guardianList);
            return new ApiResponse(guardians).send(HttpStatus.OK);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getGuardians', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the guardians");
        }
    }

    // SecPhone and middle initial are not required
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createGuardian(@RequestBody final Guardian guardian) {
        try {
            if (guardian == null || StringUtils.isBlank(guardian.getFname()) || StringUtils.isBlank(guardian.getLname())
                    || StringUtils.isBlank(guardian.getRelationship()) ||
                    StringUtils.isBlank(guardian.getPrimPhone()) || StringUtils.isBlank(guardian.getEmail()) ||
                    StringUtils.isBlank(guardian.getFamilyUnitID()) || StringUtils.isBlank(guardian.get_id()) ||
                    StringUtils.isBlank(String.valueOf(guardian.isActive()))) {
                logger.error("Error in 'createGuardian': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else {
                Optional<Family> guardianFamily = familyRepository.getFamily(guardian.getFamilyUnitID());
                if (guardianFamily.isPresent()) {
                    if (!guardianFamily.get().isActive()) {
                        logger.error("Error in 'createGuardian': cannot add a guardian to an inactive family");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot add a guardian to an inactive family");
                    }
                    guardianFamily.get().getGuardians().add(guardian.get_id());
                    Family familyResult = familyRepository.updateFamily(guardianFamily.get().get_id(), guardianFamily.get());
                    if (familyResult == null) {
                        logger.error("Error in 'createGuardian': error adding ID to family record");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding ID to family record");
                    } else {
                        guardian.setActive(true);
                        Guardian guardian1 = guardianRepository.createGuardian(guardian);
                        if (guardian1 == null || guardian1.get_id() == null) {
                            logger.error("Error in 'createGuardian': error creating guardian");
                            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the guardian");
                        } else {
                            HttpHeaders header = new HttpHeaders();
                            header.add("location", guardian1.get_id());
                            return new ApiResponse().send(HttpStatus.CREATED);
                        }
                    }
                } else {
                    logger.error("Error in 'createGuardian': could not find family associated to guardian");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Cannot find family associated to guardian");
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createGuardian', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the guardian");
        }
    }

    @PostMapping(value = "enrollGuardian", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> enrollGuardian(@RequestBody final Guardian guardian) {
        try {
            if (guardian == null || StringUtils.isBlank(guardian.getFname()) || StringUtils.isBlank(guardian.getLname())
                    || StringUtils.isBlank(guardian.getRelationship()) ||
                    StringUtils.isBlank(guardian.getPrimPhone()) || StringUtils.isBlank(guardian.getEmail()) ||
                    StringUtils.isBlank(guardian.getFamilyUnitID()) || StringUtils.isBlank(guardian.get_id()) ||
                    StringUtils.isBlank(String.valueOf(guardian.isActive()))) {
                logger.error("Error in 'createGuardian': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else {
                guardian.setActive(true);
                Guardian guardian1 = guardianRepository.createGuardian(guardian);
                if (guardian1 == null || guardian1.get_id() == null) {
                    logger.error("Error in 'createGuardian': error enrolling guardian");
                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while enrolling the guardian");
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", guardian1.get_id());
                    return new ApiResponse().send(HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createGuardian', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the guardian");
        }
    }

    // SecPhone and middle initial are not required
    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateGuardian(@PathVariable(name = "id") final String id, @RequestBody final Guardian guardian) {
        try {
            if (guardian == null || id == null || StringUtils.isBlank(guardian.getFname()) || StringUtils.isBlank(guardian.getLname())
                    || StringUtils.isBlank(guardian.getRelationship()) ||
                    StringUtils.isBlank(guardian.getPrimPhone()) || StringUtils.isBlank(guardian.getEmail()) ||
                    StringUtils.isBlank(guardian.getFamilyUnitID()) || StringUtils.isBlank(guardian.get_id()) ||
                    StringUtils.isBlank(String.valueOf(guardian.isActive()))) {
                logger.error("Error in 'updateGuardian': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (!id.equals(guardian.get_id())) {
                logger.error("Error in 'updateGuardian': id parameter does not match id in guardian");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in guardian");
            } else {
                Optional<Guardian> guardianOptional = guardianRepository.getGuardian(id);
                if (!guardianOptional.isPresent()) {
                    logger.error("Error in 'updateGuardian': could not find guardian to update");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Cannot find the guardian you were looking for");
                } else {
                    if (!guardianOptional.get().isActive()) {
                        logger.error("Error in 'updateGuardian': cannot update an inactive guardian");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot update an inactive guardian");
                    }
                    Guardian result = guardianRepository.updateGuardian(id, guardian);
                    if (result == null) {
                        logger.error("Error in 'updateGuardian': error building guardian");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the guardian");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateGuardian', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the guardian");
        }
    }

//    @DeleteMapping(value = "{id}")
//    public ResponseEntity<Void> deleteGuardian(@PathVariable(name = "id") final String id) {
//        try {
//            Optional<Guardian> guardian = guardianRepository.getGuardian(id);
//            if (guardian.isPresent()) {
//                Optional<Family> guardianFamily = familyRepository.getFamily(guardian.get().getFamilyUnitID());
//                if (guardianFamily.isPresent()) {
//                    if (guardianFamily.get().getGuardians().size() == 1) {
//                        logger.error("Error in 'deleteGuardian': a family must have at least 1 guardian");
//                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//                    }
//                    // Remove guardianID from family record
//                    guardianFamily.get().getGuardians().removeIf(s -> s.equals(guardian.get().get_id()));
//                    Family familyResult = familyRepository.updateFamily(guardianFamily.get().get_id(), guardianFamily.get());
//                    if (familyResult == null) {
//                        logger.error("Error in 'deleteGuardian': error updating family record");
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//                    } else {
//                        Guardian result = guardianRepository.deleteGuardian(guardian.get());
//                        if (result == null) {
//                            logger.error("Error in 'deleteGuardian': error deleting guardian");
//                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//                        } else {
//                            return ResponseEntity.status(HttpStatus.OK).body(null);
//                        }
//                    }
//                } else {
//                    logger.error("Error in 'deleteGuardian': cannot find family associated to guardian");
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//                }
//            } else {
//                logger.error("Error in 'deleteGuardian': guardian is null");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//        } catch (final Exception e) {
//            logger.error("Caught " + e + " in 'deleteGuardian', " + e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

    @PutMapping(value = "/updateActive/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateActiveGuardian(@PathVariable(name = "id") final String id, @RequestBody final Guardian guardian) {
        try {
            if (guardian == null || id == null || StringUtils.isBlank(guardian.getFname()) || StringUtils.isBlank(guardian.getLname())
                    || StringUtils.isBlank(guardian.getRelationship()) ||
                    StringUtils.isBlank(guardian.getPrimPhone()) || StringUtils.isBlank(guardian.getEmail()) ||
                    StringUtils.isBlank(guardian.getFamilyUnitID()) || StringUtils.isBlank(guardian.get_id()) ||
                    StringUtils.isBlank(String.valueOf(guardian.isActive()))) {
                logger.error("Error in 'updateActiveGuardian': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (!id.equals(guardian.get_id())) {
                logger.error("Error in 'updateActiveGuardian': id parameter does not match id in guardian");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in guardian");
            } else {
                Optional<Guardian> guardianOptional = guardianRepository.getGuardian(id);
                if (!guardianOptional.isPresent()) {
                    logger.error("Error in 'updateActiveGuardian': tried to update a guardian that does not exist");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the guardian you were trying to update");
                } else {
                    List<Guardian> activeGuardians = guardianRepository.getGuardians(guardianOptional.get().getFamilyUnitID(),  "true");
                    if (guardianOptional.get().isActive() && !guardian.isActive() && activeGuardians.size() == 1) {
                        logger.error("Error in 'updateActiveGuardian': a family must have at least 1 active guardian");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "A family must have at least 1 active guardian");
                    }
                    Guardian result = guardianRepository.updateActive(id, guardian);
                    if (result == null) {
                        logger.error("Error in 'updateActiveGuardian': error building guardian");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the guardian");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateActiveGuardian', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the guardian");
        }
    }
}
