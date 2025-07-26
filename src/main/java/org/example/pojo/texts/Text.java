package org.example.pojo.texts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Text {
    //    public long duration;
    private String id;
    private String content;
    private String type;
    private int line_spacing;
}
