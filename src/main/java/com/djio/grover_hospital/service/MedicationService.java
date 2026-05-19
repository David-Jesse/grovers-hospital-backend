package com.djio.grover_hospital.service;


import com.djio.grover_hospital.repository.DoctorRepository;
import com.djio.grover_hospital.repository.MedicationRepository;
import com.djio.grover_hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationService {
    
    private static final String RESOURCE_TYPE = "MEDICATION";

    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;


}
