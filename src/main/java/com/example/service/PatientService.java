package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.Patient;
import com.example.repo.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // Add Patient
    public Patient addPatient(Patient patient) {
        patient.setActive(true);
        return patientRepository.save(patient);
    }

    // Get All Patients
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // Get Patient By ID
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found"));
    }

    // Update Patient
    public Patient updatePatient(Long id, Patient updatedPatient) {

        Patient patient = getPatientById(id);

        patient.setName(updatedPatient.getName());
        patient.setPhone(updatedPatient.getPhone());

        return patientRepository.save(patient);
    }

    // Deactivate Patient
    public String deactivatePatient(Long id) {

        Patient patient = getPatientById(id);

        patient.setActive(false);

        patientRepository.save(patient);

        return "Patient deactivated successfully";
    }

    // Delete Patient - Safe Deactivation
    public String deletePatient(Long id) {
        return deactivatePatient(id);
    }
}