package org.example.main;

import org.example.pojo.dto.DtoVodeosData;
import org.example.pojo.texts.Subtitle;
import org.example.pojo.texts.Text;
import org.example.pojo.tracks.VideoSegment;
import org.example.pojo.video.Material;
import org.example.pojo.video.Speeds;
import org.example.pojo.vo.VoAudiosList;
import org.example.pojo.vo.VoSegments;
import org.example.pojo.vo.VoTextsList;
import org.example.pojo.vo.VoVideosList;
import org.example.service.MajorFunction;
import org.example.service.ToolCategory;
import org.example.service.impl.MajorFunctionImpl;
import org.example.service.impl.OperateJson;
import org.example.service.impl.ToolCategoryImpl;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ExportJson {

    /**
     * 批量导入视频
     * @param dtoVodeosData
     * @return
     */
    public VoVideosList vodeosJson(DtoVodeosData dtoVodeosData) {
        ToolCategory toolCategory = new ToolCategoryImpl();
        MajorFunction majorFunction = new MajorFunctionImpl();

        List<VideoSegment> segments = new ArrayList<>();
        List<Map<String, Object>> videos = new ArrayList<>();
        List<Speeds> speeds = new ArrayList<>();

        long startSum = 0;
        List<Path> paths = toolCategory.getAllFilesUnder(dtoVodeosData.getMaterialCollection());
        if (paths.isEmpty()) return new VoVideosList(segments, videos, speeds);

        Set<Path> usedPaths = new HashSet<>();
        int maxAttempts = paths.size() * 3; // 更宽松防止误杀
        int attempts = 0;

        while (startSum < dtoVodeosData.getZongDuration() && attempts++ < maxAttempts) {
            Path path = getNextAvailablePath(paths, usedPaths, toolCategory);
            if (path == null) break;

            long duration = toolCategory.GetVideoLength(path.toString());
            if (duration <= 0) continue;

            // 截取时长调整
            if (dtoVodeosData.getCutStart() != 0 || dtoVodeosData.getCutEnd() != 100) {
                double ratio = toolCategory.getRandomDouble(dtoVodeosData.getCutStart(), dtoVodeosData.getCutEnd());
                duration = (long) (duration * (ratio / 100));
            }

            // 变速处理
            float speed = 1.0f;
            if (dtoVodeosData.getShiftStart() < dtoVodeosData.getShiftEnd()) {
                speed = (float) toolCategory.getRandomDouble(dtoVodeosData.getShiftStart(), dtoVodeosData.getShiftEnd());
            }

            Speeds speedSegment = majorFunction.getSpeeds(speed);
            long clipDuration = (long) (duration / speed);
            long remain = dtoVodeosData.getZongDuration() - startSum;

            if (clipDuration > remain) {
                clipDuration = remain;
                duration = (long) (clipDuration * speed);
            }
            if (clipDuration <= 0) break;

            Material material = majorFunction.setMaterial(path.toString(), "video");
            VoSegments vs = new VoSegments(speed, duration, clipDuration, 0, startSum, material.getId(), 0, 0);
            VideoSegment segment = majorFunction.getSegments(vs, speedSegment.getId());

            segments.add(segment);
            videos.add(OperateJson.exportToJson(material));
            speeds.add(speedSegment);

            startSum += clipDuration;
        }

        // return 保证数据完整，释放资源交由 GC
        return new VoVideosList(segments, videos, speeds);
    }

    private Path getNextAvailablePath(List<Path> paths, Set<Path> usedPaths, ToolCategory toolCategory) {
        List<Path> available = paths.stream()
                .filter(p -> !usedPaths.contains(p))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            usedPaths.clear(); // 重置
            if (paths.isEmpty()) return null;
            return paths.get(toolCategory.getRandomInt(0, paths.size()));
        }

        Path selected = available.get(toolCategory.getRandomInt(0, available.size()));
        usedPaths.add(selected);
        return selected;
    }

    /**
     * 批量导入音频
     * @param materialCollection
     * @return
     */
    public VoAudiosList audiosJson(String materialCollection, double dB, long zongoDuration) {
        ToolCategory toolCategory = new ToolCategoryImpl();
        MajorFunction majorFunction = new MajorFunctionImpl();

        List<VideoSegment> segments = new ArrayList<>();
        List<Map<String, Object>> audios = new ArrayList<>();
        long startSum = 0;
        double volume = Math.pow(10, dB / 20.0);

        List<Path> paths = toolCategory.getAllFilesUnder(materialCollection);
        Collections.shuffle(paths); // 打乱顺序
        Set<Path> usedPaths = new HashSet<>();

        int maxAttempts = paths.size() * 2; // 防止死循环
        int attempts = 0;

        while (startSum < zongoDuration && attempts < maxAttempts) {
            attempts++;

            Path path = null;
            for (Path p : paths) {
                if (!usedPaths.contains(p)) {
                    path = p;
                    break;
                }
            }

            if (path == null) {
                usedPaths.clear();
                Collections.shuffle(paths); // 再次打乱
                if (paths.isEmpty()) break;
                path = paths.get(toolCategory.getRandomInt(0, paths.size()));
            }

            usedPaths.add(path);

            long duration = toolCategory.GetVideoLength(path.toString());
            if (duration < 0) continue;

            long remainingDuration = zongoDuration - startSum;
            if (duration > remainingDuration) {
                duration = remainingDuration;
            }

            if (duration <= 0) break;

            Material material = majorFunction.setMaterial(path.toString(), "extract_music");
            VoSegments vs = new VoSegments(1.0f, duration, duration, startSum, startSum, material.getId(), 0, volume);
            startSum += duration;

            VideoSegment segment = majorFunction.getSegments(vs, "");
            Map<String, Object> materialsMap = OperateJson.exportToJson(material);

            segments.add(segment);
            audios.add(materialsMap);
        }

        // 清理引用
        paths.clear();
        usedPaths.clear();

        return new VoAudiosList(segments, audios);
    }




    /**
     * 导入配音
     * @param materialCollection
     * @return
     */
    public VoAudiosList dubAudiosJson(String materialCollection) {
        ToolCategory toolCategory = new ToolCategoryImpl();
        MajorFunction majorFunction = new MajorFunctionImpl();

        List<VideoSegment> segments = new ArrayList<>();
        List<Map<String, Object>> audios = new ArrayList<>();

        long duration = toolCategory.GetVideoLength(materialCollection);
        Material material = majorFunction.setMaterial(materialCollection, "extract_music");
        VoSegments vs = new VoSegments(1.0f, duration,duration,duration, 0, material.getId(), 0,3);
        VideoSegment segment = majorFunction.getSegments(vs,"");
        Map<String, Object> materialsMap = OperateJson.exportToJson(material);
        segments.add(segment);
        audios.add(materialsMap);

        return new VoAudiosList(segments, audios);
    }

    /**
     * 导入字幕
     * @param materialCollection
     * @return
     */
    public VoTextsList textsJson(String materialCollection) {
        // 初始化必要组件
        ToolCategory toolCategory = new ToolCategoryImpl();
        MajorFunction majorFunction = new MajorFunctionImpl();

        List<VideoSegment> segments = new ArrayList<>();
        List<Map<String, Object>> texts = new ArrayList<>();
        long startSum = 0;

        List<Subtitle> subtitles = toolCategory.parseSrtFile(materialCollection);
        if (subtitles == null || subtitles.isEmpty()) {
            return new VoTextsList(Collections.emptyList(), Collections.emptyList(), 0L);
        }

        for (int i = 0; i < subtitles.size(); i++) {
            Subtitle subtitle = subtitles.get(i);
            if (subtitle == null || subtitle.getText() == null || subtitle.getText().isBlank()) continue;

            // 提取时间戳并验证
            long startTime = toolCategory.timeToMicroseconds(subtitle.getStartTime());
            long endTime = toolCategory.timeToMicroseconds(subtitle.getEndTime());
            long duration = endTime - startTime;
            if (duration <= 0) continue;

            // 构造字幕内容 JSON
            String content = toolCategory.getContent(subtitle.getText());

            // 创建素材与片段
            Text text = majorFunction.setText(content);
            VoSegments segmentMeta = new VoSegments(1.0f, duration,duration,startSum,startSum, text.getId(), 0, 1.0);
            VideoSegment segment = majorFunction.getSegments(segmentMeta,"");
            Map<String, Object> materialJson = OperateJson.exportToJson(text);

            // 更新数据
            startSum += duration;
            segments.add(segment);
            texts.add(materialJson);
        }

        // 释放原始列表引用
        subtitles.clear();

        return new VoTextsList(segments, texts, startSum);
    }

}
