package org.example.UI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsDialog extends JDialog {
    private JTextField projectPathField;
    private JTextField exePathField;
    private static final String CONFIG_FILE = "config.properties";

    public SettingsDialog(JFrame parent) {
        super(parent, "设置", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(550, 220);
        setLocationRelativeTo(parent);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 第1行: 剪映项目存放位置标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        contentPanel.add(new JLabel("剪映项目存放位置:"), gbc);

        // 第1行: 剪映项目存放路径输入框
        projectPathField = new JTextField(loadConfig("projectPath", ""));
        gbc.gridx = 1;
        gbc.weightx = 1;
        contentPanel.add(projectPathField, gbc);

        // 第1行: 浏览按钮
        JButton browseProjectBtn = new JButton("浏览...");
        browseProjectBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(projectPathField.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = chooser.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File dir = chooser.getSelectedFile();
                projectPathField.setText(dir.getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        gbc.weightx = 0;
        contentPanel.add(browseProjectBtn, gbc);

        // 第2行: 剪映启动程序路径标签
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        contentPanel.add(new JLabel("剪映启动程序路径:"), gbc);

        // 第2行: 启动程序路径输入框
        exePathField = new JTextField(loadConfig("exePath", ""));
        gbc.gridx = 1;
        gbc.weightx = 1;
        contentPanel.add(exePathField, gbc);

        // 第2行: 浏览按钮
        JButton browseExeBtn = new JButton("浏览...");
        browseExeBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(exePathField.getText());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("Executable Files (*.exe)", "exe"));
            int ret = chooser.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file.getName().toLowerCase().endsWith(".exe")) {
                    exePathField.setText(file.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "请选择一个 .exe 可执行文件", "文件类型错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        gbc.gridx = 2;
        gbc.weightx = 0;
        contentPanel.add(browseExeBtn, gbc);

        // 第3行: 按钮面板，右对齐
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> {
            String projectPath = projectPathField.getText().trim();
            String exePath = exePathField.getText().trim();

            File projDir = new File(projectPath);
            if (!projDir.exists() || !projDir.isDirectory()) {
                JOptionPane.showMessageDialog(this, "剪映项目存放位置必须是存在的文件夹", "路径错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File exeFile = new File(exePath);
            if (!exeFile.exists() || !exeFile.isFile() || !exeFile.getName().toLowerCase().endsWith(".exe")) {
                JOptionPane.showMessageDialog(this, "剪映启动程序路径必须是存在的 .exe 文件", "路径错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            saveConfig("projectPath", projectPath);
            saveConfig("exePath", exePath);
            JOptionPane.showMessageDialog(this, "设置已保存");
            dispose();
        });

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(btnPanel, gbc);

        setContentPane(contentPanel);
        setResizable(false);
        setVisible(true);
    }

    private String loadConfig(String key, String defaultValue) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            return props.getProperty(key, defaultValue);
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private void saveConfig(String key, String value) {
        Properties props = new Properties();
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
            }
            props.setProperty(key, value);
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "剪映工具配置");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

