package org.example.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoSegments {
    private float speed;
    private long duration;
    private long speed_duration;
    private long start;
    private long speed_start;
    private String material_id;
    private long render_index;
    private double volume;
}
