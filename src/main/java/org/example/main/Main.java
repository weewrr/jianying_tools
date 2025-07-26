package org.example.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;
import org.example.autoexport.ExportAuxiliaryUtils;
import org.example.pojo.dto.DtoPath;
import org.example.pojo.dto.DtoVodeosData;
import org.example.pojo.tracks.VideoSegment;
import org.example.pojo.vo.VoAudiosList;
import org.example.pojo.vo.VoTextsList;
import org.example.pojo.vo.VoVideosList;
import org.example.service.MajorFunction;
import org.example.service.impl.MajorFunctionImpl;
import org.example.service.impl.OperateJson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Main {
    public static void app(DtoPath dtoPath) {
        try {
            MajorFunction majorFunction = new MajorFunctionImpl();
            ExportJson exportJson = new ExportJson();

            // 读取素材数据
            VoTextsList voTextsList = exportJson.textsJson(dtoPath.getTextPath());
            VoAudiosList voAudiosList = exportJson.audiosJson(dtoPath.getAudiosPath(), dtoPath.getVolume(), voTextsList.getDuration());
            VoAudiosList dubAudioList = exportJson.dubAudiosJson(dtoPath.getDubPath());
            VoVideosList voVideosList = exportJson.vodeosJson(new DtoVodeosData(
                    dtoPath.getVoideosPath(),
                    dtoPath.getShiftStart(),
                    dtoPath.getShiftEnd(),
                    dtoPath.getCutStart(),
                    dtoPath.getCutEnd(),
                    voTextsList.getDuration()
            ));

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            // 读取模板 json
            JsonNode root = mapper.readTree(new File(dtoPath.getJsonPath()));
            ObjectNode jsonObject = (ObjectNode) root;
            jsonObject.put("duration", voTextsList.getDuration());

            // 填充素材列表
            ObjectNode materials = (ObjectNode) jsonObject.get("materials");
            ArrayNode jsonVideos = materials.withArray("videos");
            ArrayNode jsonAudios = materials.withArray("audios");
            ArrayNode jsonTexts = materials.withArray("texts");
            ArrayNode jsonSpeeds = materials.withArray("speeds");

            jsonVideos.removeAll();

            voVideosList.getVideos().forEach(video -> jsonVideos.add(mapper.valueToTree(video)));
            voAudiosList.getAudios().forEach(audio -> jsonAudios.add(mapper.valueToTree(audio)));
            dubAudioList.getAudios().forEach(dub -> jsonAudios.add(mapper.valueToTree(dub)));
            voTextsList.getVideos().forEach(text -> jsonTexts.add(mapper.valueToTree(text)));
            voVideosList.getSpeeds().forEach(speed -> jsonSpeeds.add(mapper.valueToTree(speed)));

            // 轨道处理
            ArrayNode tracks = (ArrayNode) jsonObject.get("tracks");
            ArrayNode videoTrackArray = null;
            for (JsonNode track : tracks) {
                if ("video".equals(track.path("type").asText())) {
                    videoTrackArray = (ArrayNode) track.path("segments");
                    break;
                }
            }

            if (videoTrackArray != null) {
                for (VideoSegment seg : voVideosList.getSegments()) {
                    videoTrackArray.add(mapper.valueToTree(OperateJson.exportToJson(seg)));
                }
            } else {
                log.warn("未找到 video 类型轨道，将不写入视频 segments");
            }

            // 添加音频与文本轨道
            tracks.add(mapper.valueToTree(OperateJson.exportToJson(
                    majorFunction.setVideoTrack("audio", voAudiosList.getSegments())
            )));
            tracks.add(mapper.valueToTree(OperateJson.exportToJson(
                    majorFunction.setVideoTrack("audio", dubAudioList.getSegments())
            )));
            tracks.add(mapper.valueToTree(OperateJson.exportToJson(
                    majorFunction.setVideoTrack("text", voTextsList.getSegments())
            )));

            // 写入 JSON 文件
            Path outputDir = Paths.get(dtoPath.getWritePath(), dtoPath.getProjectName());
            Files.createDirectories(outputDir);

            Path draftPath = outputDir.resolve("draft_content.json");
            Path metaPath = outputDir.resolve("draft_meta_info.json");

            mapper.writeValue(draftPath.toFile(), jsonObject);
            mapper.writeValue(metaPath.toFile(), null);

        } catch (IOException e) {
            log.error("导出失败，发生IO异常: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("导出失败，未知异常: {}", e.getMessage(), e);
        }
    }

    public static void autoExport() {
        try {
            ExportAuxiliaryUtils.restartProcessFromHwnd();
            WinDef.HWND hwnd = null;
            while (hwnd == null) {
                hwnd = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "剪映专业版");
                Thread.sleep(2000);
            }
            Thread.sleep(1000);
            ExportAuxiliaryUtils.click(hwnd,927,648,280,320);

            WinDef.HWND hwndParent;
            while (true) {
                hwndParent = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "导出");
                if (hwndParent != null) {
                    log.info("开始导出");
                    Thread.sleep(1500);
                    ExportAuxiliaryUtils.click(hwndParent,640,663,500,630);
                    break;
                } else {
                    hwnd = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "剪映专业版");
                    ExportAuxiliaryUtils.click(hwnd,1280,901,1130,20);
                    Thread.sleep(1500);
                }
            }
        } catch (Exception e) {
            log.error("导出时发生异常: {}", e.getMessage(), e);
        }
    }
}