package com.craftora.subscription;

import com.craftora.subscription.PlanDtos.PlanResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Plans")
@RestController
public class PlanController {

    private final PlanService plans;

    public PlanController(PlanService plans) {
        this.plans = plans;
    }

    @GetMapping("/api/public/plans")
    public List<PlanResponse> plans() {
        return plans.listActive();
    }
}
