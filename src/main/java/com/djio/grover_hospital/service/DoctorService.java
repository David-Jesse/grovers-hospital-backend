package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.DoctorRequest;
import com.djio.grover_hospital.model.dto.response.AdminDoctorResponse;
import com.djio.grover_hospital.model.dto.response.DoctorResponse;
import com.djio.grover_hospital.model.entity.Department;
import com.djio.grover_hospital.model.entity.Doctor;
import com.djio.grover_hospital.repository.DepartmentRepository;
import com.djio.grover_hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    // ==== Public read ====

    public List<DoctorResponse> getAllActive() {
        return doctorRepository.findByIsActiveTrueOrderByFullNameAsc()
                .stream()
                .map(DoctorResponse::from)
                .toList();
    }

    public List<DoctorResponse> getByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndIsActiveTrueOrderByFullNameAsc(departmentId)
                .stream()
                .map(DoctorResponse::from)
                .toList();
    }

    public DoctorResponse getById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return DoctorResponse.from(doctor);
    }

    // ====== Admin ======

    public List<AdminDoctorResponse> getAllForAdmin() {
        return doctorRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"))
                .stream()
                .map(AdminDoctorResponse::from)
                .toList();
    }

    public AdminDoctorResponse getByIdForAdmin(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return AdminDoctorResponse.from(doctor);
    }

    @Transactional
    public AdminDoctorResponse create(DoctorRequest request) {
        Doctor doctor = Doctor.builder()
                .fullName(request.getFullName())
                .title(request.getTitle())
                .specialty(request.getSpecialty())
                .department(resolveDepartment(request.getDepartmentId()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .photoUrl(request.getPhotoUrl())
                .bio(request.getBio())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return AdminDoctorResponse.from(doctorRepository.save(doctor));
    }

    @Transactional
    public AdminDoctorResponse update(Long id, DoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));

        doctor.setFullName(request.getFullName());
        doctor.setTitle(request.getTitle());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setDepartment(resolveDepartment(request.getDepartmentId()));
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setPhotoUrl(request.getPhotoUrl());
        doctor.setBio(request.getBio());
        if (request.getIsActive() != null) doctor.setIsActive(request.getIsActive());

        return AdminDoctorResponse.from(doctorRepository.save(doctor));
    }

    @Transactional
    public void delete(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor", "id", id);
        }
        doctorRepository.deleteById(id);
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
    }
}