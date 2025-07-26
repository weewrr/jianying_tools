package org.example.pojo.texts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subtitle {
    private int number;
    private String startTime;
    private String endTime;
    private String text;
}
