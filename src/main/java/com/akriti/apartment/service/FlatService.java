package com.akriti.apartment.service;

import com.akriti.apartment.dto.FlatRequest;
import com.akriti.apartment.entity.Flat;
import com.akriti.apartment.repository.FlatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FlatService {

    @Autowired
    private FlatRepository flatRepository;

    public List<Flat> getAll() {
        return flatRepository.findByIsActiveTrue();
    }

    public Flat getFlat(String flatNo) {
        return flatRepository.findById(flatNo)
            .orElseThrow(() -> new RuntimeException("Flat not found: " + flatNo));
    }

    public Flat update(String flatNo, FlatRequest req) {
        Flat flat = getFlat(flatNo);
        if (req.getOwnerName()     != null) flat.setOwnerName(req.getOwnerName());
        if (req.getOwnerPhone()    != null) flat.setOwnerPhone(req.getOwnerPhone());
        if (req.getOwnerEmail()    != null) flat.setOwnerEmail(req.getOwnerEmail());
        if (req.getResidentName()  != null) flat.setResidentName(req.getResidentName());
        if (req.getResidentPhone() != null) flat.setResidentPhone(req.getResidentPhone());
        if (req.getResidentEmail() != null) flat.setResidentEmail(req.getResidentEmail());
        if (req.getOwnerType()     != null) flat.setOwnerType(Flat.OwnerType.valueOf(req.getOwnerType().toUpperCase().replace("-","_").replace(" ","_")));
        return flatRepository.save(flat);
    }
}
