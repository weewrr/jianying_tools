package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BOMInputStream;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.example.pojo.texts.Subtitle;
import org.example.service.ToolCategory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 工具类实现
 */
@Slf4j
public class ToolCategoryImpl implements ToolCategory {
    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * 获取视频的时长
     *
     * @param mediaPath 视频文件的路径
     * @return 视频的时长（单位：毫秒），如果发生异常则返回-1
     */
    @Override
    public long GetVideoLength(String mediaPath) {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR); // 降低 FFmpeg 噪音
        FFmpegLogCallback.set(); // 注册日志回调，便于查看 FFmpeg 报错

        Path path = Paths.get(mediaPath);
        if (!Files.exists(path)) {
            log.error("媒体文件不存在: {}", mediaPath);
            return -1;
        }

        // 支持的音视频格式后缀
        String[] supportedExtensions = {".mp4", ".mov", ".avi", ".mp3", ".aac", ".flac", ".wav", ".mkv"};
        boolean isSupported = Arrays.stream(supportedExtensions)
                .anyMatch(ext -> mediaPath.toLowerCase().endsWith(ext));

        if (!isSupported) {
            log.error("不支持的媒体文件格式: {}", mediaPath);
            return -1;
        }

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(mediaPath)) {
            grabber.start();

            long durationInMicroSeconds = grabber.getLengthInTime(); // 微秒级别
            grabber.stop();

            if (durationInMicroSeconds <= 0) {
                log.warn("媒体时长为 0 或无法获取: {}", mediaPath);
            }

            return durationInMicroSeconds;
        } catch (Exception e) {
            log.error("读取媒体文件时发生异常: {}", mediaPath, e);
            return -1;
        }
    }


    /**
     * 获取指定文件夹路径下的所有文件路径
     *
     * @param folderPath 指定的文件夹路径
     * @return 一个包含文件夹路径下的所有文件路径的ArrayList
     */
    @Override
    public List<Path> getAllFilesUnder(String folderPath) {
        List<Path> filePaths = new ArrayList<>();
        Path startPath = Paths.get(folderPath);

        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            log.warn("路径不存在或不是文件夹: {}", folderPath);
            return filePaths;
        }

        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        filePaths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("读取文件夹 [{}] 时发生异常: {}", folderPath, e.getMessage(), e);
        }

        return filePaths;
    }


    /**
     * 生成指定范围内的随机数[min, max)
     *
     * @param min 最小值
     * @param max 最大值
     * @return 浮点数
     */
    @Override
    public double getRandomDouble(double min, double max) {
        if (min > max)
            throw new IllegalArgumentException("最小值不能大于最大值");
        double value = ThreadLocalRandom.current().nextDouble(min, max);
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * 获取随机整数
     * @param min
     * @param max
     * @return
     */
    @Override
    public int getRandomInt(int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("最小值不能大于最大值");
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /**
     * 获取字符串拼接成Content
     *
     * @param str
     * @return
     */
    @Override
    public String getContent(String str) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("text", str);

            // style 构建
            ObjectNode style = mapper.createObjectNode();

            // 字体填充颜色 white
            ObjectNode fill = mapper.createObjectNode();
            ObjectNode fillContent = mapper.createObjectNode();
            ObjectNode solid = mapper.createObjectNode();
            solid.putArray("color").add(1.0).add(1.0).add(1.0);
            fillContent.set("solid", solid);
            fill.set("content", fillContent);
            style.set("fill", fill);

            // 字体
            ObjectNode font = mapper.createObjectNode();
            font.put("path", "/Resources/Font/新青年体.ttf");
            font.put("id", "6740435892441190919");
            style.set("font", font);

            // 描边 strokes
            ObjectNode stroke = mapper.createObjectNode();
            ObjectNode strokeContent = mapper.createObjectNode();
            ObjectNode strokeSolid = mapper.createObjectNode();
            strokeSolid.putArray("color").add(0.0).add(0.0).add(0.0);
            strokeContent.set("solid", strokeSolid);
            stroke.set("content", strokeContent);
            stroke.put("width", 0.08);
            ArrayNode strokes = mapper.createArrayNode();
            strokes.add(stroke);
            style.set("strokes", strokes);

            // 其他属性
            style.put("size", 10.0);
            style.put("bold", true);
            style.put("useLetterColor", true);
            ArrayNode range = mapper.createArrayNode();
            range.add(0).add(str.length());
            style.set("range", range);

            ArrayNode styles = mapper.createArrayNode();
            styles.add(style);
            root.set("styles", styles);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * 解析SRT文件
     *
     * @param filePath SRT文件的路径
     * @return 一个包含SRT文件中的子标题的列表
     * @throws IOException 如果发生IO异常
     */
    @Override
    public List<Subtitle> parseSrtFile(String filePath) {
        List<Subtitle> subtitles = new ArrayList<>();

        try (
                BOMInputStream bomIn = new BOMInputStream(new FileInputStream(filePath));
                BufferedReader reader = new BufferedReader(new InputStreamReader(bomIn, StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    int number = Integer.parseInt(line.trim());  // 这里不会再因为 BOM 报错

                    // 时间行
                    String timeLine = reader.readLine();
                    if (timeLine == null) break;
                    String[] times = timeLine.trim().split(" --> ");
                    if (times.length != 2) {
                        throw new IOException("时间格式无效: " + timeLine);
                    }

                    String startTime = times[0].trim();
                    String endTime = times[1].trim();

                    // 字幕文本
                    StringBuilder textBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                        if (textBuilder.length() > 0) {
                            textBuilder.append("\n");
                        }
                        textBuilder.append(line.trim());
                    }

                    subtitles.add(new Subtitle(number, startTime, endTime, textBuilder.toString()));

                } catch (NumberFormatException e) {
                    log.error("解析字幕编号时出错: {}", e.getMessage(), e);
                }
            }

        } catch (IOException e) {
            log.error("解析SRT文件时发生异常: {}", e.getMessage(), e);
        }

        return subtitles;
    }


    /**
     * 时间转换成微秒
     * @param time
     * @return
     */
    @Override
    public long timeToMicroseconds(String time) {
        String[] parts = time.split("[:,]");

        if (parts.length != 4) {
            throw new IllegalArgumentException("时间格式无效,预期HH:MM:SS,但得到: " + time);
        }
        try {
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = Long.parseLong(parts[2]);
            long millis = Long.parseLong(parts[3]);

            return hours * 3600000000L    // 小时 → 微秒
                    + minutes * 60000000L      // 分钟 → 微秒
                    + seconds * 1000000L       // 秒 → 微秒
                    + millis * 1000L;           // 毫秒 → 微秒
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("时间格式无效,预期是数字,但得到" + time, e);
        }
    }
}
