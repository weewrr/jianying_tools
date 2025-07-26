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
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Main {
    public static void app(DtoPath dtoPath) {
        MajorFunction majorFunction = new MajorFunctionImpl();
        ExportJson exportJson = new ExportJson();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            //1. 并行读取文本素材
            CompletableFuture<VoTextsList> textsFuture = CompletableFuture.supplyAsync(() ->
                    exportJson.textsJson(dtoPath.getTextPath())
            );

            //2. 并行读取配音音频（与 texts 无关）
            CompletableFuture<VoAudiosList> dubFuture = CompletableFuture.supplyAsync(() ->
                    exportJson.dubAudiosJson(dtoPath.getDubPath())
            );

            //3. audios 和 videos 依赖 texts 的 duration，等 texts 结果出来后异步执行
            CompletableFuture<VoAudiosList> audiosFuture = textsFuture.thenApplyAsync(voTextsList ->
                    exportJson.audiosJson(dtoPath.getAudiosPath(), dtoPath.getVolume(), voTextsList.getDuration())
            );

            CompletableFuture<VoVideosList> videosFuture = textsFuture.thenApplyAsync(voTextsList ->
                    exportJson.vodeosJson(new DtoVodeosData(
                            dtoPath.getVoideosPath(),
                            dtoPath.getStartPath(),
                            dtoPath.getShiftStart(),
                            dtoPath.getShiftEnd(),
                            dtoPath.getCutStart(),
                            dtoPath.getCutEnd(),
                            voTextsList.getDuration()
                    ))
            );

            //等待所有素材加载完
            VoTextsList voTextsList = textsFuture.get();
            VoAudiosList voAudiosList = audiosFuture.get();
            VoAudiosList dubAudioList = dubFuture.get();
            VoVideosList voVideosList = videosFuture.get();

            //读取模板 JSON 并组装
            String json = Paths.get(dtoPath.getJsonPath(), "draft_content.json").toString();
            JsonNode root = mapper.readTree(new File(json));
            ObjectNode jsonObject = (ObjectNode) root;
            jsonObject.put("duration", voTextsList.getDuration());

            ObjectNode materials = (ObjectNode) jsonObject.get("materials");
            ArrayNode jsonVideos = materials.withArray("videos");
            ArrayNode jsonAudios = materials.withArray("audios");
            ArrayNode jsonTexts = materials.withArray("texts");
            ArrayNode jsonSpeeds = materials.withArray("speeds");

            jsonVideos.removeAll();
            voVideosList.getVideos().forEach(v -> jsonVideos.add(mapper.valueToTree(v)));
            voAudiosList.getAudios().forEach(a -> jsonAudios.add(mapper.valueToTree(a)));
            dubAudioList.getAudios().forEach(d -> jsonAudios.add(mapper.valueToTree(d)));
            voTextsList.getVideos().forEach(t -> jsonTexts.add(mapper.valueToTree(t)));
            voVideosList.getSpeeds().forEach(s -> jsonSpeeds.add(mapper.valueToTree(s)));

            //构建 tracks
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

            //添加音频与文本轨道
            tracks.add(mapper.valueToTree(OperateJson.exportToJson(
                    majorFunction.setVideoTrack("audio", voAudiosList.getSegments())
            )));
            tracks.add(mapper.valueToTree(OperateJson.exportToJson(
                    majorFunction.setVideoTrack("audio", dubAudioList.getSegments())
            )));
            tracks.add(mapper.valueToTree(OperateJson.exportToJson(
                    majorFunction.setVideoTrack("text", voTextsList.getSegments())
            )));

            //写入输出 JSON 文件
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            String projectPath = props.getProperty("projectPath");
            Path outputDir = Paths.get(projectPath, dtoPath.getProjectName());
            Files.createDirectories(outputDir);
            Path draftPath = outputDir.resolve("draft_content.json");
            Path metaPath = outputDir.resolve("draft_meta_info.json");

            mapper.writeValue(draftPath.toFile(), jsonObject);
            mapper.writeValue(metaPath.toFile(), null);

        } catch (Exception e) {
            log.error("导出失败: {}", e.getMessage(), e);
        }
    }



    public static void autoExport(String projectName) {
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
            Thread.sleep(60000 * 3);
            WinDef.HWND Parent = null;
            while (true){
                Parent = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "导出");
                if (ExportAuxiliaryUtils.hasWindowSizeChanged(Parent)){
                    Parent = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "导出");
                    Thread.sleep(1000);
                    ExportAuxiliaryUtils.click(Parent, 640, 488,587,463);
                    break;
                }
                Thread.sleep(30000);
            }

            ExportAuxiliaryUtils.closeAllJianyingPro();
            Thread.sleep(1000);
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            String projectPath = props.getProperty("projectPath");
            Path projectPathFile = Paths.get(projectPath, projectName);

            ExportAuxiliaryUtils.deleteDirectory(projectPathFile.toFile());
        } catch (Exception e) {
            log.error("导出时发生异常: {}", e.getMessage(), e);
        }
    }
}