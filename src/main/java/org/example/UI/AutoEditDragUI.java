package org.example.UI;

import lombok.extern.slf4j.Slf4j;
import org.example.autoexport.ExportAuxiliaryUtils;
import org.example.main.Main;
import org.example.pojo.dto.DtoPath;
import org.example.service.impl.YamlConfigUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AutoEditDragUI extends JFrame {
    private JSlider speedMinSlider, speedMaxSlider, trimStartSlider, trimEndSlider, volumeSlider;
    private final Map<JLabel, File> dropBoxMap = new LinkedHashMap<>();
    private final String projectName, dataPath;

    public static void showGlobalTopDialog(String message, String title) {
        JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION
        );
        JDialog dialog = optionPane.createDialog(null, title);
        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    public AutoEditDragUI(String projectName, String dataPath) {
        this.projectName = projectName;
        this.dataPath = dataPath;

        setTitle(projectName + " - 设置");
        setIconImage(new ImageIcon("ico/UI_ico.png").getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        centerPanel.add(createSliderPanel("变速最小值 (x)", speedMinSlider = createSlider(0, 1000, 10)));
        centerPanel.add(createSliderPanel("变速最大值 (x)", speedMaxSlider = createSlider(0, 1000, 10)));
        centerPanel.add(createSliderPanel("截取起始 (%)", trimStartSlider = createSlider(0, 100, 0)));
        centerPanel.add(createSliderPanel("截取结束 (%)", trimEndSlider = createSlider(0, 100, 100)));
        centerPanel.add(createSliderPanel("音量 (dB)", volumeSlider = createSlider(-60, 20, 0)));

        // 拖拽文件区
        String[] labels = {"字幕", "配音", "模板", "视频素材集合", "音频素材集合", "导出位置"};
        boolean[] isFile = {true, true, true, false, false, false};

        JPanel dragGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        for (int i = 0; i < labels.length; i++) {
            JPanel boxWrapper = new JPanel(new BorderLayout(5, 5));
            JLabel title = new JLabel(labels[i], JLabel.CENTER);
            JLabel dropBox = createDropBox(isFile[i]);
            dropBoxMap.put(dropBox, null);
            boxWrapper.add(title, BorderLayout.NORTH);
            boxWrapper.add(dropBox, BorderLayout.CENTER);
            dragGrid.add(boxWrapper);
        }

        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(dragGrid);

        // 底部按钮区
        JPanel btnPanel = new JPanel();
        JButton cancelBtn = new JButton("取消");
        JButton startBtn = new JButton("开始");

        cancelBtn.addActionListener(e -> {
            dispose();
            new ProjectSelector();
        });

        startBtn.addActionListener(e -> {
            DtoPath dtoPath = handleSubmit();
            try {
                long start = System.nanoTime();
                Main.app(dtoPath);
                double elapsed = (System.nanoTime() - start) / 1e9;
                showGlobalTopDialog(
                        String.format("运行成功！用时 %.2f 秒", elapsed),
                        "完成提示"
                );
                log.info("运行成功,用时{}秒",elapsed);

                Main.autoExport();
                showGlobalTopDialog("正在导出，请耐心等待！", "完成提示");
                System.exit(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "运行失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                log.error("运行失败：{}",ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(startBtn);

        add(centerPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        initFromConfig();
        setVisible(true);
    }

    private JSlider createSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        return slider;
    }

    private JPanel createSliderPanel(String labelText, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel();
        updateSliderLabel(slider, labelText, label);
        slider.addChangeListener(e -> updateSliderLabel(slider, labelText, label));
        panel.add(label, BorderLayout.WEST);
        panel.add(slider, BorderLayout.CENTER);
        return panel;
    }

    private void updateSliderLabel(JSlider slider, String name, JLabel label) {
        String value = switch (name) {
            case "音量 (dB)" -> slider.getValue() + " dB";
            case "截取起始 (%)", "截取结束 (%)" -> slider.getValue() + "%";
            default -> String.format("%.1fx", slider.getValue() / 10.0);
        };
        label.setText(name + ": " + value);
    }

    private JLabel createDropBox(boolean isFile) {
        JLabel label = new JLabel("+", JLabel.CENTER);
        label.setPreferredSize(new Dimension(100, 100));
        label.setFont(new Font("微软雅黑", Font.BOLD, 24));
        label.setBorder(new LineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setToolTipText("拖入" + (isFile ? "文件" : "文件夹"));

        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                dropBoxMap.put(label, null);
                label.setText("+");
                label.setBackground(Color.WHITE);
            }
        });

        new DropTarget(label, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.isEmpty()) return;
                    File f = files.get(0);
                    if ((isFile && f.isFile()) || (!isFile && f.isDirectory())) {
                        dropBoxMap.put(label, f);
                        label.setText("<html><center>" + f.getName() + "</center></html>");
                        label.setBackground(new Color(173, 216, 230));
                        label.setToolTipText(f.getAbsolutePath());
                    } else {
                        JOptionPane.showMessageDialog(null, "请拖入" + (isFile ? "文件" : "文件夹"), "类型错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return label;
    }

    private void initFromConfig() {
        DtoPath config = YamlConfigUtil.loadConfig(this.dataPath);
        if (config == null) return;

        speedMinSlider.setValue((int) (config.getShiftStart() * 10));
        speedMaxSlider.setValue((int) (config.getShiftEnd() * 10));
        trimStartSlider.setValue(config.getCutStart());
        trimEndSlider.setValue(config.getCutEnd());
        volumeSlider.setValue((int) config.getVolume());

        Map<String, String> labelToPath = Map.of(
                "字幕", config.getTextPath(),
                "配音", config.getDubPath(),
                "模板", config.getJsonPath(),
                "视频素材集合", config.getVoideosPath(),
                "音频素材集合", config.getAudiosPath(),
                "导出位置", config.getWritePath()
        );

        dropBoxMap.forEach((label, unused) -> {
            String title = ((JLabel) label.getParent().getComponent(0)).getText();
            String path = labelToPath.get(title);
            if (path != null) {
                File f = new File(path);
                if (f.exists()) {
                    dropBoxMap.put(label, f);
                    label.setText("<html><center>" + f.getName() + "</center></html>");
                    label.setBackground(new Color(173, 216, 230));
                    label.setToolTipText(path);
                }
            }
        });
    }

    private DtoPath handleSubmit() {
        DtoPath dto = new DtoPath();
        dto.setProjectName(projectName);
        dto.setShiftStart(speedMinSlider.getValue() / 10.0);
        dto.setShiftEnd(speedMaxSlider.getValue() / 10.0);
        dto.setCutStart(trimStartSlider.getValue());
        dto.setCutEnd(trimEndSlider.getValue());
        dto.setVolume(volumeSlider.getValue());

        dropBoxMap.forEach((label, file) -> {
            if (file == null) return;
            String labelName = ((JLabel) label.getParent().getComponent(0)).getText();
            switch (labelName) {
                case "字幕" -> dto.setTextPath(file.getAbsolutePath());
                case "配音" -> dto.setDubPath(file.getAbsolutePath());
                case "模板" -> dto.setJsonPath(file.getAbsolutePath());
                case "视频素材集合" -> dto.setVoideosPath(file.getAbsolutePath());
                case "音频素材集合" -> dto.setAudiosPath(file.getAbsolutePath());
                case "导出位置" -> dto.setWritePath(file.getAbsolutePath());
            }
        });

        YamlConfigUtil.saveConfig(dto, this.projectName);
        return dto;
    }
}

