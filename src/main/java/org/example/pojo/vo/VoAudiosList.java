package org.example.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.pojo.tracks.VideoSegment;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoAudiosList {
    private List<VideoSegment> segments ;
    private List<Map<String, Object>> audios;
}
