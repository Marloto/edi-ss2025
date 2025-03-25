package de.thi.informatik.edi.example01;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lombok generiert
// alle Getter / Setter
// @Data
// -> start schlägt fehl?
// -> ggf. maven?
public class Message {
    private String msg;

    public String getMsg() {
        return msg;
    }
}
