package com.stage.backend.service;

import com.stage.backend.entity.Medecin;


public interface IMedecinService {

    Medecin registerMedecin(Medecin medecin);

    void approveUser(Long idmedecin);

    void blockUser(Long idmedecin);

}
