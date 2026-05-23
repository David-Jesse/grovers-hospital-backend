package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.TestComponentBulkRequest;
import com.djio.grover_hospital.model.dto.request.TestComponentRequest;
import com.djio.grover_hospital.model.dto.response.TestComponentResponse;
import com.djio.grover_hospital.model.entity.Result;
import com.djio.grover_hospital.model.entity.TestComponent;
import com.djio.grover_hospital.repository.ResultRepository;
import com.djio.grover_hospital.repository.TestComponentRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestComponentService {

    private static final String RESOURCE_TYPE = "TEST_COMPONENT";

    private final TestComponentRepository testComponentRepository;
    private final ResultRepository resultRepository;
    private final AuditService auditService;

    // ====== Patient facing ==========
    @Transactional(readOnly = true)
    public List<TestComponentResponse> getMyResultComponents(Long resultId) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Result result = resultRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id " + resultId));

        // Ownership guard - a patient can only read components of thier own results
        if (result.getPatient() == null || !result.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("You can only view components of your own results");
        }

        return testComponentRepository.findByResultIdOrderByDisplayOrderAscIdDesc(resultId)
                .stream().map(TestComponentResponse::from).toList();
    }

    // ======= Admin-facing ========

    @Transactional(readOnly = true)
    public List<TestComponentResponse> getComponentsForResult(Long resultId) {
        verifyResultExists(resultId);
        return testComponentRepository.findByResultIdOrderByDisplayOrderAscIdDesc(resultId)
                .stream().map(TestComponentResponse::from).toList();
    }

    @Transactional
    public TestComponentResponse addToResult(Long resultId, TestComponentRequest request,
                                             HttpServletRequest httpRequest
    ) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id " + resultId));

        TestComponent component = TestComponent.builder()
                .result(result)
                .name(request.getName())
                .value(request.getValue())
                .unit(request.getUnit())
                .referenceRange(request.getReferenceRange())
                .flag(request.getFlag())
                .notes(request.getNotes())
                .displayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder())
                .build();

        TestComponent saved = testComponentRepository.save(component);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "TEST_COMPONENT_CREATED",
                RESOURCE_TYPE, saved.getId(), httpRequest
        );
        log.info("Admin {} added test component {} to result {}", adminId, saved.getId(), resultId);

        return TestComponentResponse.from(saved);
    }

    @Transactional
    public TestComponentResponse update(Long componentId, TestComponentRequest request,
                                        HttpServletRequest httpRequest
    ) {
        TestComponent component = testComponentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Test component not found with id " + componentId));

        component.setName(request.getName());
        component.setValue(request.getValue());
        component.setUnit(request.getUnit());
        component.setReferenceRange(request.getReferenceRange());
        component.setFlag(request.getFlag());
        component.setNotes(request.getNotes());
        if (request.getDisplayOrder() != null) {
            component.setDisplayOrder(request.getDisplayOrder());
        }

        TestComponent saved = testComponentRepository.save(component);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "TEST_COMPONENT_UPDATED",
                RESOURCE_TYPE, saved.getId(), httpRequest);

        return TestComponentResponse.from(saved);
    }

    @Transactional
    public void delete(Long componentId, HttpServletRequest httpRequest) {
        TestComponent component = testComponentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Test component not found with id " + componentId));
        Long resultId = component.getResult() != null ? component.getResult().getId() : null;
        testComponentRepository.delete(component);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "TEST_COMPONENT_DELETED",
                RESOURCE_TYPE, componentId, httpRequest
        );
    }

    /**
     * Bulk replace - clears all existing components for a result and re-creates
     * from the supplied list. Handy for entering a full panel at once
     */
    @Transactional
    public List<TestComponentResponse> replaceAllForResult(Long resultId, TestComponentBulkRequest request, HttpServletRequest httpRequest) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id " + resultId));

        // Remove existing
        testComponentRepository.deleteByResultId(resultId);

        List<TestComponent> toSave = new ArrayList<>();
        int autoOrder = 0;
        for (TestComponentRequest req : request.getComponents()) {
            TestComponent component = TestComponent.builder()
                    .result(result)
                    .name(req.getName())
                    .value(req.getValue())
                    .referenceRange(req.getReferenceRange())
                    .flag(req.getFlag())
                    .notes(req.getNotes())
                    .displayOrder(req.getDisplayOrder() == null ? autoOrder : req.getDisplayOrder())
                    .build();
            toSave.add(component);
            autoOrder++;
        }

        List<TestComponent> saved = testComponentRepository.saveAll(toSave);

        Long adminId = SecurityUtils.getCurrentUserId();
        auditService.log(adminId, "ADMIN", "TEST_COMPONENTS_BULK_REPLACED", RESOURCE_TYPE, resultId, httpRequest);
        log.info("Admin {} bulk-replaced {} test components on result {}", adminId, saved.size(), resultId);

        return saved.stream().map(TestComponentResponse::from).toList();
    }

    // ======= Helpers =======
    private void verifyResultExists(Long resultId) {
        if (!resultRepository.existsById(resultId)) {
            throw new ResourceNotFoundException("Result not found with id " + resultId);
        }
    }
}
