package org.assignment.model;

import lombok.Data;

@Data
public class KeyValueStringDTO {
    private String name;
    private String value;

    public KeyValueStringDTO() {
    }

    public KeyValueStringDTO(String name, String value) {
        this.name = name;
        this.value = value;
    }

}
