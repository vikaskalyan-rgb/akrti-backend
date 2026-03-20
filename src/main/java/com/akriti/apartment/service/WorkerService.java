package com.akriti.apartment.service;

import com.akriti.apartment.dto.WorkerRequest;
import com.akriti.apartment.entity.Worker;
import com.akriti.apartment.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkerService {

    @Autowired private WorkerRepository workerRepository;

    public List<Worker> getAll() {
        return workerRepository.findByIsActiveTrueOrderByRoleAscNameAsc();
    }

    public List<Worker> getAllIncludingInactive() {
        return workerRepository.findAll();
    }

    public Worker getById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
    }

    @Transactional
    public Worker create(WorkerRequest req) {
        Worker worker = Worker.builder()
                .name(req.getName().trim())
                .role(Worker.WorkerRole.valueOf(req.getRole().toUpperCase()))
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .shift(req.getShift() != null && !req.getShift().isBlank()
                        ? Worker.Shift.valueOf(req.getShift().toUpperCase()) : null)
                .idProofType(req.getIdProofType())
                .idProofNumber(req.getIdProofNumber())
                .joiningDate(req.getJoiningDate() != null && !req.getJoiningDate().isBlank()
                        ? LocalDate.parse(req.getJoiningDate()) : null)
                .monthlySalary(req.getMonthlySalary())
                .notes(req.getNotes())
                .isActive(true)
                .build();
        return workerRepository.save(worker);
    }

    @Transactional
    public Worker update(Long id, WorkerRequest req) {
        Worker worker = getById(id);
        if (req.getName()     != null) worker.setName(req.getName().trim());
        if (req.getRole()     != null) worker.setRole(
                Worker.WorkerRole.valueOf(req.getRole().toUpperCase()));
        if (req.getPhone()    != null) worker.setPhone(req.getPhone());
        if (req.getEmail()    != null) worker.setEmail(req.getEmail());
        if (req.getAddress()  != null) worker.setAddress(req.getAddress());
        if (req.getShift()    != null) worker.setShift(
                req.getShift().isBlank() ? null
                        : Worker.Shift.valueOf(req.getShift().toUpperCase()));
        if (req.getIdProofType()   != null) worker.setIdProofType(req.getIdProofType());
        if (req.getIdProofNumber() != null) worker.setIdProofNumber(req.getIdProofNumber());
        if (req.getJoiningDate()   != null && !req.getJoiningDate().isBlank())
            worker.setJoiningDate(LocalDate.parse(req.getJoiningDate()));
        if (req.getMonthlySalary() != null) worker.setMonthlySalary(req.getMonthlySalary());
        if (req.getNotes()    != null) worker.setNotes(req.getNotes());
        if (req.getIsActive() != null) worker.setIsActive(req.getIsActive());
        worker.setUpdatedAt(LocalDateTime.now());
        return workerRepository.save(worker);
    }

    @Transactional
    public void deactivate(Long id) {
        Worker worker = getById(id);
        worker.setIsActive(false);
        worker.setUpdatedAt(LocalDateTime.now());
        workerRepository.save(worker);
    }
}