package com.viperlevels.rule;

import com.viperlevels.condition.SkillRequirement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ValidationResult {
    private boolean passed;
    private List<SkillRequirement> missingRequirements;
    private Rule appliedRule;

    public static ValidationResult pass() {
        return new ValidationResult(true, new ArrayList<>(), null);
    }

    public static ValidationResult fail(List<SkillRequirement> missing, Rule rule) {
        return new ValidationResult(false, missing, rule);
    }

    public boolean hasMissingRequirements() {
        return !missingRequirements.isEmpty();
    }

    public String formatMissingRequirements() {
        if (missingRequirements.isEmpty()) {
            return "NONE";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < missingRequirements.size(); i++) {
            sb.append(missingRequirements.get(i).toString());
            if (i < missingRequirements.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}