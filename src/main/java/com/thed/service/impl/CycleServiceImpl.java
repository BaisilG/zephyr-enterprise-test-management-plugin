package com.thed.service.impl;

import com.thed.model.Cycle;
import com.thed.model.CyclePhase;
import com.thed.model.TCRCatalogTreeTestcase;
import com.thed.model.Testcase;
import com.thed.service.CycleService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by prashant on 26/6/19.
 */
public class CycleServiceImpl extends BaseServiceImpl implements CycleService {

    public CycleServiceImpl() {
        super();
    }

    @Override
    public List<Cycle> getAllCyclesForReleaseId(Long releaseId) throws URISyntaxException, IOException {
        return zephyrRestService.getAllCyclesForReleaseId(releaseId);
    }

    @Override
    public Cycle createCycle(Cycle cycle) throws URISyntaxException, IOException {
        return zephyrRestService.createCycle(cycle);
    }

    @Override
    public Cycle getCycleById(Long cycleId) throws URISyntaxException, IOException {
        return zephyrRestService.getCycleById(cycleId);
    }

    @Override
    public CyclePhase createCyclePhase(CyclePhase cyclePhase) throws URISyntaxException, IOException {
        return zephyrRestService.createCyclePhase(cyclePhase);
    }

    @Override
    public Integer assignCyclePhaseToCreator(Long cyclePhaseId) throws URISyntaxException, IOException {
        return zephyrRestService.assignCyclePhaseToCreator(cyclePhaseId);
    }

    @Override
    public String addTestcasesToFreeFormCyclePhase(CyclePhase cyclePhase, List<TCRCatalogTreeTestcase> testcases, Boolean includeHierarchy) throws URISyntaxException, IOException {
        //todo: this data parsing loop runs two times, once here and once in ZephyrRestService, need to fix this
        Map<Long, Set<Long>> treeTestcaseMap = new HashMap<>();

        for (TCRCatalogTreeTestcase testcase : testcases) {
            if(treeTestcaseMap.containsKey(testcase.getTcrCatalogTreeId())) {
                treeTestcaseMap.get(testcase.getTcrCatalogTreeId()).add(testcase.getTestcase().getId());
            }
            else {
                Set<Long> tctIds = new HashSet<>();
                tctIds.add(testcase.getTestcase().getId());
                treeTestcaseMap.put(testcase.getTcrCatalogTreeId(), tctIds);
            }
        }

        return zephyrRestService.addTestcasesToFreeFormCyclePhase(cyclePhase, treeTestcaseMap, includeHierarchy);
    }
}
