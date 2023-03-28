package com.ib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
@RequestMapping(value = "api/person")
public class PersonController {
    @Autowired
    private PersonRepo personRepo;
    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        Person person=new Person();
        person.setName("Danilo");
        personRepo.save(person);
        Person person1=personRepo.getReferenceById(1L);
        System.out.println(person1.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
