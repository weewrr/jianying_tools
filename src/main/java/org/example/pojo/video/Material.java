package org.example.pojo.video;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Material {
//    public long duration;
    public String id;
    public String path;
    public String type;
    public boolean has_audio;
}