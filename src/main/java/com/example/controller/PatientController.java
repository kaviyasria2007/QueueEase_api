package com.example.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.model.Patient;
import com.example.service.PatientService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // POST - Add Patient
    @PostMapping
    public Patient addPatient(@RequestBody Patient patient) {
        return patientService.addPatient(patient);
    }

    // GET - All Patients
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    // GET - Patient By ID
    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }

    // PUT - Update Patient
    @PutMapping("/{id}")
    public Patient updatePatient(
            @PathVariable Long id,
            @RequestBody Patient patient) {

        return patientService.updatePatient(id, patient);
    }

    // PUT - Deactivate Patient
    @PutMapping("/deactivate/{id}")
    public String deactivatePatient(@PathVariable Long id) {
        return patientService.deactivatePatient(id);
    }

    // DELETE - Safe Patient Deactivation
    @DeleteMapping("/{id}")
    public String deletePatient(@PathVariable Long id) {
        return patientService.deletePatient(id);
    }
}