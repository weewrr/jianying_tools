package org.example.service;

import org.example.pojo.texts.Subtitle;

import java.nio.file.Path;
import java.util.List;

/**
 * 工具类接口
 */
public interface ToolCategory {
    /**
     * 获取视频的时长
     *
     * @param videoPath 视频文件的路径
     * @return 视频的时长（单位：毫秒），如果发生异常则返回-1
     */
    long GetVideoLength(String videoPath);

    /**
     * 获取指定文件夹路径下的所有文件路径
     *
     * @param folderPath 指定的文件夹路径
     * @return 一个包含文件夹路径下的所有文件路径的ArrayList
     */
    List<Path> getAllFilesUnder(String folderPath);

    /**
     * 生成指定范围内的随机数[min, max)
     *
     * @param min 最小值
     * @param max 最大值
     * @return 浮点数
     */
    double getRandomDouble(double min, double max);

    /**
     * 生成指定范围内的随机数[min, max]
     * @param min
     * @param max
     * @return
     */
    int getRandomInt(int min, int max);

    /**
     * 获取字符串拼接成Content
     * @param str
     * @return
     */
    String getContent(String str);

    /**
     * 解析SRT文件
     *
     * @param filePath SRT文件的路径
     * @return 一个包含SRT文件中的子标题的列表
     */
    List<Subtitle> parseSrtFile(String filePath);

    /**
     * 时间转换
     * @param time
     */
    long timeToMicroseconds(String time);
}
