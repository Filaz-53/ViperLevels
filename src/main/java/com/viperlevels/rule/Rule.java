package com.viperlevels.rule;

import com.viperlevels.condition.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Rule {
    private String identifier;
    private RuleType type;
    private List<ActionType> actions;
    private Condition condition;

    public boolean appliesToAction(ActionType action) {
        return actions.contains(action);
    }

    public boolean hasRequirements() {
        return condition != null && !condition.isEmpty();
    }
}