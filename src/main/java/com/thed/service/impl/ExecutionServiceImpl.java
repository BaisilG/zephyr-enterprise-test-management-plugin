package com.thed.service.impl;

import com.thed.model.ReleaseTestSchedule;
import com.thed.model.TestStepResult;
import com.thed.service.ExecutionService;
import com.thed.utils.ZephyrConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by prashant on 29/6/19.
 */
public class ExecutionServiceImpl extends BaseServiceImpl implements ExecutionService {

    public ExecutionServiceImpl() {
        super();
    }

    @Override
    public List<ReleaseTestSchedule> getReleaseTestSchedules(Long cyclePhaseId) throws URISyntaxException, IOException {
        return zephyrRestService.getReleaseTestSchedules(cyclePhaseId);
    }

    @Override
    public List<ReleaseTestSchedule> executeReleaseTestSchedules(Set<Long> rtsIds, boolean pass) throws URISyntaxException, IOException {
        if(rtsIds.isEmpty()) {
            return new ArrayList<>();
        }
        return zephyrRestService.executeReleaseTestSchedules(rtsIds, pass ? ZephyrConstants.EXECUTION_STATUS_PASS : ZephyrConstants.EXECUTION_STATUS_FAIL);
    }

    @Override
    public List<TestStepResult> addTestStepResults(List<TestStepResult> testStepResults) throws URISyntaxException, IOException {
        return zephyrRestService.addTestStepsResults(testStepResults);
    }

}
