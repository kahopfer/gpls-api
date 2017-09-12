package com.designteam1.controller;

import com.designteam1.model.Parent;
import com.designteam1.model.Parents;
import com.designteam1.repository.ParentRepository;
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
@RequestMapping("parents")
public class ParentController {
    private static final Logger logger = LoggerFactory.getLogger(ParentController.class);

    public ParentController() {

    }

    public ParentController(final ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
    }

    @Autowired
    private ParentRepository parentRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Parents> getParents() {
        try {
            final Parents parents = new Parents();
            final List<Parent> parentList = parentRepository.getParents();
            if(parentList == null) {
                return ResponseEntity.ok(parents);
            }
            parents.setParents(parentList);
            return ResponseEntity.ok(parents);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getParents', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Parent> getParent(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Parent> parent = parentRepository.getParent(id);
            if(!parent.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(parent.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getParent', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
