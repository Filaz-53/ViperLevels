package com.viperlevels.rule;

import com.viperlevels.condition.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MaterialGroup {
    private String name;
    private List<String> materials;
    private Condition condition;

    public boolean containsMaterial(String material) {
        return materials.contains(material);
    }
}