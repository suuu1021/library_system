package dto;

import lombok.*;

@Data // Getter, Setter, ToString 포함
@NoArgsConstructor
@AllArgsConstructor

public class Student {

    private int id;
    private String name;
    private String studentId;

}
