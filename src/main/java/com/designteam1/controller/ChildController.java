package com.designteam1.controller;

import com.designteam1.model.Child;
import com.designteam1.model.Children;
import com.designteam1.repository.ChildRepository;
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
@RequestMapping("children")
public class ChildController {
    private static final Logger logger = LoggerFactory.getLogger(ChildController.class);

    public ChildController() {

    }

    public ChildController(final ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    @Autowired
    private ChildRepository childRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Children> getChildren() {
        try {
            final Children children = new Children();
            final List<Child> childList = childRepository.getChildren();
            if(childList == null) {
                return ResponseEntity.ok(children);
            }
            children.setChildren(childList);
            return ResponseEntity.ok(children);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getChildren', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Child> getChild(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Child> child = childRepository.getChild(id);
            if(!child.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(child.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getChild', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
