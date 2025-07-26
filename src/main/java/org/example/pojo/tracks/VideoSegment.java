package org.example.pojo.tracks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoSegment {
    private String id;
    private double speed;
    private TimeRange target_timerange;
    private TimeRange source_timerange;
    private String material_id;
    private long render_index;
    private double volume;
    private List<String> extra_material_refs;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeRange {
        private long duration;
        private long start;
    }
}
