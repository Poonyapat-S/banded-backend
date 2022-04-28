package com.javainuse.controllers;

import com.javainuse.classes.BlockService;
import com.javainuse.classes.DummyUser;
import com.javainuse.classes.User;
import com.javainuse.classes.UserRepository;
import com.javainuse.registration.EmailValidator;
import com.javainuse.registration.StrengthCheck;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;


import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    private StrengthCheck strengthCheck;
    private PasswordEncoder passwordEncoder;
    private EmailValidator emailValidator;
    private BlockService blockService;

    @GetMapping
    public User getCurrentProfile(@AuthenticationPrincipal User user) {
        return user;
    }

    @GetMapping(path="/{username}")
    public User getProfile(@AuthenticationPrincipal User user, @PathVariable String username) {
        try {
            User viewedUser = userRepository.findByUserName(username).orElseThrow();
            viewedUser.setBlocked(blockService.blockExists(user.getUserID(), viewedUser.getUserID()));
            return viewedUser;
        } catch (Exception e) {
            System.out.println("ERROR retrieving User in ProfileController");
            return null;
        }
    }

    @PutMapping(path="/editbio/{username}")
    public ResponseEntity<?> editProfileBio(@AuthenticationPrincipal User user, @PathVariable String username, @RequestBody String bioText) {
        System.out.println("Called");
        System.out.println(user.getUsername());
        System.out.println(username);
        System.out.println(bioText);
        if(!Objects.equals(user.getUsername(), username)) {
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }
        user.setBio(bioText);
        userRepository.save(user);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @DeleteMapping(path="/delete/{userName}")
    public ResponseEntity<?> deleteProfile(@AuthenticationPrincipal User user, @PathVariable String userName) {
        System.out.println("Delete Called");
        System.out.println(user.getUsername());
        System.out.println(userName);
        if(!Objects.equals(user.getUsername(), userName)) {
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }
        userRepository.delete(user);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @PostMapping(path="/changeUsername")
    public ResponseEntity<String> changeUsername(@AuthenticationPrincipal User user,@PathVariable String newUsername){
        try {
            userRepository.findByUserName(newUsername).orElseThrow(() -> new UsernameNotFoundException(String.format("", "")));
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate Username");
        } catch(UsernameNotFoundException e){
            user.setUserName(newUsername);
            userRepository.save(user);
            return new ResponseEntity("Username successfully changed",HttpStatus.OK);
        }
    }

    @PostMapping(path="/changePassword")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal User user,@RequestParam String newPassword){
        try{
            String flags = strengthCheck.checkPassword(newPassword);
            if (flags.length() > 0) return ResponseEntity.status(HttpStatus.CONFLICT).body(flags);
            String encoded = passwordEncoder.encode(newPassword);
            User toComp = userRepository.findByUserName(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException(String.format("", "")));
            if(passwordEncoder.matches(newPassword,toComp.getPassword())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate Password");
            }
            user.setPassword(encoded);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("Password Successfully Changed!");
        } catch(UsernameNotFoundException e){
            return new ResponseEntity("User not found",HttpStatus.CONFLICT);
        }
    }

    @PostMapping(path="/changeEmail")
    public ResponseEntity<String> changeEmail(@AuthenticationPrincipal User user,@RequestParam String newEmail){
        if(!emailValidator.validateEmail(newEmail)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid Email");
        }
        try{
            userRepository.findByEmail(newEmail).orElseThrow(() -> new UsernameNotFoundException(String.format("", "")));
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate Email");
        } catch(UsernameNotFoundException e){
            user.setEmail(newEmail);
            userRepository.save(user);
            return new ResponseEntity("Email successfully changed",HttpStatus.OK);
        }
    }
}
