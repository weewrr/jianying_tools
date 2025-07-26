package org.example.service;

import org.example.pojo.texts.Text;
import org.example.pojo.tracks.VideoSegment;
import org.example.pojo.tracks.VideoTrack;
import org.example.pojo.video.Material;
import org.example.pojo.video.Speeds;
import org.example.pojo.vo.VoSegments;

import java.util.List;

public interface MajorFunction {
    /**
     * 设置轨道
     * @param type
     * @param segments
     * @return
     */
    VideoTrack setVideoTrack(String type, List<VideoSegment> segments);

    /**
     * 设置视频，音频素材
     * @param path
     * @param type
     * @return
     */
    Material setMaterial(String path, String type);

    /**
     * 设置文字素材
     * @param content
     * @return
     */
    Text setText(String content);


    /**Segments
     * 获取片段
     * @param vs
     * @return
     */
    VideoSegment getSegments(VoSegments vs, String extra_material_refs);


    /**
     * 获取速度
     * @param speed
     * @return
     */
    Speeds getSpeeds(float speed);
}
