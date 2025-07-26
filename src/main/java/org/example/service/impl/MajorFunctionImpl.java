package org.example.service.impl;

import org.example.pojo.texts.Text;
import org.example.pojo.tracks.VideoSegment;
import org.example.pojo.tracks.VideoTrack;
import org.example.pojo.video.Material;
import org.example.pojo.video.Speeds;
import org.example.pojo.vo.VoSegments;
import org.example.service.MajorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MajorFunctionImpl implements MajorFunction {
    /**
     * 设置视频，音频素材
     * @param path
     * @param type
     * @return
     */
    @Override
    public Material setMaterial(String path, String type) {
        Material material = new Material();
        material.setPath(path);
        material.setId(UUID.randomUUID().toString());
        material.setType(type);
        material.setHas_audio(false);
        return material;
    }

    /**
     * 设置文字素材
     * @param content
     * @return
     */
    @Override
    public Text setText(String content) {
        Text text = new Text();
        text.setContent(content);
        text.setId(UUID.randomUUID().toString());
        text.setType("subtitle");
        text.setLine_spacing(0);
        return text;
    }

    /**
     * 设置轨道
     * @param type
     * @param segments
     * @return
     */
    @Override
    public VideoTrack setVideoTrack(String type, List<VideoSegment> segments){
        VideoTrack videoTrack = new VideoTrack();
        videoTrack.setId(UUID.randomUUID().toString());
        videoTrack.setType(type);
        videoTrack.setSegments(segments);
        return videoTrack;
    }


    /**
     * 获取片段
     * @param vs
     * @return
     */
    @Override
    public VideoSegment getSegments(VoSegments vs,String extra) {
        VideoSegment videoSegment = new VideoSegment();
        videoSegment.setSpeed(vs.getSpeed());//变速F
        videoSegment.setVolume(vs.getVolume());
        videoSegment.setTarget_timerange(new VideoSegment.TimeRange(vs.getSpeed_duration(), vs.getSpeed_start()));//视频长度和拼接起始位置
        videoSegment.setSource_timerange(new VideoSegment.TimeRange(vs.getDuration(), vs.getStart()));//视频长度和拼接起始位置
        videoSegment.setMaterial_id(vs.getMaterial_id());//绑定的素材id
        videoSegment.setId(UUID.randomUUID().toString());
        videoSegment.setRender_index(vs.getRender_index());
        List<String> extra_material_refs = new ArrayList<>();
        extra_material_refs.add(extra);
        videoSegment.setExtra_material_refs(extra_material_refs);
        return videoSegment;
    }


    /**
     * 获取变速信息
     * @param speed
     * @return
     */
    public Speeds getSpeeds(float speed) {
        Speeds speeds = new Speeds();
        speeds.setId(UUID.randomUUID().toString());
        speeds.setSpeed(speed);
        speeds.setType("speed");
        return speeds;
    }
}
