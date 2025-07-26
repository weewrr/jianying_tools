package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.pojo.dto.DtoPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class YamlConfigUtil {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    public static void saveConfig(DtoPath config, String projectName) {
        Path folderPath = Paths.get("data");
        Path configPath = folderPath.resolve(projectName + ".yml");
        try {
            // 确保 data 文件夹存在
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            // 保存配置
            mapper.writeValue(configPath.toFile(), config);

        } catch (IOException e) {
            log.error("保存配置文件失败: " + configPath);
            e.printStackTrace();
        }
    }


    public static DtoPath loadConfig(String configPath) {
        File file = new File(configPath);
        if (!file.exists()) {
            return null;
        }

        try {
            return mapper.readValue(file, DtoPath.class);
        } catch (IOException e) {
            log.error("读取配置文件失败: " + configPath);
            e.printStackTrace();
            return null;
        }
    }

}
