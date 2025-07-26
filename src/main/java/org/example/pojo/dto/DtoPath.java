package org.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoPath {
    private String textPath;//字幕路径（.srt）
    private String voideosPath;//视频素材路径（文件夹）
    private String dubPath;//配音路径（.mp3）
    private String audiosPath; //音频素材路径（文件夹）
    private String JsonPath;//模板json文件路径
    private String writePath;//输出json文件路径

    private double shiftStart; //变速最小速度
    private double shiftEnd; //变速最大速度
    private int cutStart; //随机截取视频片段的最小
    private int cutEnd; //随机截取视频片段的最大

    private double volume;//音频素材音量
    private String projectName;//项目名称
}
