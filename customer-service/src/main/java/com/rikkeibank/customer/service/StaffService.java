package com.rikkeibank.customer.service;

import com.rikkeibank.common.exception.BusinessException;
import com.rikkeibank.common.exception.ResourceNotFoundException;
import com.rikkeibank.customer.dto.StaffRequest;
import com.rikkeibank.customer.dto.StaffResponse;
import com.rikkeibank.customer.entity.Staff;
import com.rikkeibank.customer.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffResponse createStaff(StaffRequest request) {
        if (staffRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BusinessException("Staff with this employee code already exists");
        }

        Staff staff = new Staff();
        staff.setFullName(request.getFullName());
        staff.setEmployeeCode(request.getEmployeeCode());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setEmail(request.getEmail());
        staff.setRole(request.getRole());
        staff.setCreatedAt(LocalDateTime.now());
        staff.setUpdatedAt(LocalDateTime.now());

        staff = staffRepository.save(staff);
        return mapToResponse(staff);
    }

    public StaffResponse getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id.toString()));
        return mapToResponse(staff);
    }

    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public StaffResponse updateStaff(Long id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id.toString()));

        if (!staff.getEmployeeCode().equals(request.getEmployeeCode()) &&
            staffRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BusinessException("Staff with this employee code already exists");
        }

        staff.setFullName(request.getFullName());
        staff.setEmployeeCode(request.getEmployeeCode());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setEmail(request.getEmail());
        staff.setRole(request.getRole());
        staff.setUpdatedAt(LocalDateTime.now());

        staff = staffRepository.save(staff);
        return mapToResponse(staff);
    }

    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staff", id.toString());
        }
        staffRepository.deleteById(id);
    }

    private StaffResponse mapToResponse(Staff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getFullName(),
                staff.getEmployeeCode(),
                staff.getPhoneNumber(),
                staff.getEmail(),
                staff.getRole(),
                staff.getCreatedAt(),
                staff.getUpdatedAt()
        );
    }
}
