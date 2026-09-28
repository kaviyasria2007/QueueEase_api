package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.Doctor;
import com.example.repo.DoctorRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // Add Doctor
    public Doctor addDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    // Get All Doctors
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Get Doctor By ID
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));
    }

    // Update Doctor
    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {

        Doctor doctor = getDoctorById(id);

        doctor.setName(updatedDoctor.getName());
        doctor.setSpecialization(updatedDoctor.getSpecialization());
        doctor.setAverageConsultationTime(
                updatedDoctor.getAverageConsultationTime());

        return doctorRepository.save(doctor);
    }

    // Delete Doctor
   // Delete Doctor
public String deleteDoctor(Long id) {

    Doctor doctor = getDoctorById(id);

    doctorRepository.delete(doctor);

    return "Doctor deleted successfully";
}
}