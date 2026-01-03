package org.assignment.model;

import lombok.Data;

@Data
public class KeyValueObjectDTO {
    private String name;
    private Object value;

    public KeyValueObjectDTO() {
    }

    public KeyValueObjectDTO(String name, Object value) {
        this.name = name;
        this.value = value;
    }

}
