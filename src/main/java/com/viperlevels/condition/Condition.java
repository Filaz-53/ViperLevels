package com.viperlevels.condition;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Condition {
    private List<SkillRequirement> requirements;
    private ConditionOperator operator;

    public enum ConditionOperator {
        AND,
        OR
    }

    public Condition() {
        this.requirements = new ArrayList<>();
        this.operator = ConditionOperator.AND;
    }

    public void addRequirement(SkillRequirement requirement) {
        this.requirements.add(requirement);
    }

    public boolean isEmpty() {
        return requirements.isEmpty();
    }

    public String toString() {
        if (requirements.isEmpty()) {
            return "NONE";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < requirements.size(); i++) {
            sb.append(requirements.get(i).toString());
            if (i < requirements.size() - 1) {
                sb.append(" ").append(operator.name()).append(" ");
            }
        }
        return sb.toString();
    }
}