package org.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoVodeosData {
    private String materialCollection;//素材集合路径
    private double shiftStart; //变速最小速度
    private double shiftEnd; //变速最大速度
    private int cutStart; //随机截取视频片段的最小
    private int cutEnd; //随机截取视频片段的最大
    private long ZongDuration;
}
