package org.example.pojo.video;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Speeds {
    private String id;
    private float speed;
    private String type;
}
