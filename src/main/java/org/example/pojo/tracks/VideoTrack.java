package org.example.pojo.tracks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoTrack {
    private String id;
    private List<VideoSegment> segments;
    private String type;
}
