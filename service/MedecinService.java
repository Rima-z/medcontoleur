package com.stage.backend.service;

import com.stage.backend.entity.Medecin;
import com.stage.backend.repository.MedecinRepository;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MedecinService implements IMedecinService {
    @Autowired
    private MedecinRepository medecinRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Medecin registerMedecin(Medecin medecin) {
        medecin.setPassword(passwordEncoder.encode(medecin.getPassword()));
        return medecinRepository.save(medecin);
    }

    @Override
    public void approveUser(Long idmedecin) {
        Medecin medecin = medecinRepository.findById(idmedecin).orElse(null);
        if (medecin != null) {
            medecin.setIsActivated(true);
            medecinRepository.save(medecin);
        }
    }

    @Override
    public void blockUser(Long idmedecin) {
        Medecin medecin = medecinRepository.findById(idmedecin).orElse(null);
        if (medecin != null) {
            medecin.setIsActivated(false);
            medecinRepository.save(medecin);
        }
    }
}
