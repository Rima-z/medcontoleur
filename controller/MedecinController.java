package com.stage.backend.controller;

import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mysql.fabric.Response;
import com.stage.backend.Dto.AuthRequest;
import com.stage.backend.entity.EmailDetails;
import com.stage.backend.entity.Medecin;
import com.stage.backend.repository.MedecinRepository;
import com.stage.backend.service.EmailService;
import com.stage.backend.service.IMedecinService;
import com.stage.backend.service.JwtService;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import org.hibernate.mapping.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Objects;

@RestController
@RequestMapping("/Medecin")
@CrossOrigin("*")
public class MedecinController {

    @Autowired
    private IMedecinService iMedecinService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome medecin";
    }

    @GetMapping("/welcomeADMIN")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String welcomeAdmin() {
        return "Welcome ADMIN";
    }


    @PostMapping(value = "/register-medecin",consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> addClient( @RequestBody Medecin medecin){
        EmailDetails  details = new EmailDetails();
        details.setRecipient(medecin.getEmail());
        details.setSubject("Compte créé");
        details.setAttachment("");
        System.out.println(medecin.getEmail());
        if(Objects.equals(medecin.getRole(), "ADMIN")){
            iMedecinService.registerMedecin(medecin);
            details.setMsgBody("admin account created");
            String status=emailService.sendSimpleMail(details);
            System.out.println(status);
           return ResponseEntity.status(200).body("admin created");
        }else{
            if (medecinRepository.existsByEmail(medecin.getEmail())) {

                return ResponseEntity
                        .badRequest()
                        .body("email exists");
            }
            iMedecinService.registerMedecin(medecin);
            details.setMsgBody("Compte Médecin créé, attendez que l'administrateur approuve votre compte.");

            String status=emailService.sendSimpleMail(details);
            System.out.println(status);
            HashMap<String, Object > map = new HashMap<>();
            map.put("success",true);
          
            map.put("message", "medecin created");
            return ResponseEntity.status(200).body(map);
        }
    }
    @PostMapping(value="/authenticate",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequest authRequest) throws Exception {
        Medecin medecin = medecinRepository.findByEmail(authRequest.getEmail()).orElse(null);
        if(medecin==null){
           return  ResponseEntity.status(404).body("user non found !");
        }else {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //*******ADMIN******************
            if(Objects.equals(medecin.getRole(), "ADMIN")){
                String jwt = jwtService.generateToken(authRequest.getEmail());
                //jwtService.generateToken(String.valueOf(authentication));
                HashMap<String, Object > map = new HashMap<>();
                map.put("success",true);
                map.put("token",jwt);
                map.put("user", medecin);
                return ResponseEntity.status(200).body(map);

            }else {
            //********MEDECIN****************
                if (authentication.isAuthenticated()) {

                    if(medecin.getIsActivated()){
                        String jwt= jwtService.generateToken(authRequest.getEmail());
                        HashMap<String, Object > map = new HashMap<>();
                        map.put("success",true);
                        map.put("token",jwt);
                        map.put("user", medecin);
                        return ResponseEntity.status(200).body(map);
                    }else{
                        HashMap<String, Object > map = new HashMap<>();
                        map.put("success",false);
                        map.put("message","user not verified!");
                        return ResponseEntity.status(405).body(map);
                    }

                } else {
                    return ResponseEntity.badRequest().body("check your credentials");
                }

            }
        }
    }

    @PostMapping("/approuveUser")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void  approuveUser(@RequestHeader("id") Long id){

        iMedecinService.approveUser(id);
        System.out.println("approved");

    }
    @PostMapping("/block-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void  blockUser(@RequestHeader("id") Long id){

        iMedecinService.blockUser(id);
        System.out.println("blocked");

    }


}
