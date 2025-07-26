package org.example.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class ProjectSelector extends JFrame {
    private static final String PROJECT_DIR = "data";
    private static final int CARD_SIZE = 150;
    private static final int GAP = 20;

    public ProjectSelector() {
        setTitle("选择或创建项目");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 设置窗口图标
        ImageIcon icon = new ImageIcon("ico/UI_ico.png");
        setIconImage(icon.getImage());

        // 顶部：开始创作按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, 40));
        topPanel.setPreferredSize(new Dimension(getWidth(), getHeight() / 4));
        JButton createBtn = new JButton("开始创作");
        createBtn.setFont(new Font("微软雅黑", Font.BOLD, 22));
        createBtn.setPreferredSize(new Dimension(200, 50));
        createBtn.addActionListener(e -> showInputDialog());
        topPanel.add(createBtn);

        // 中部：项目卡片区（带滚动）
        JPanel projectPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, GAP, GAP));
        JScrollPane scrollPane = new JScrollPane(projectPanel);
        scrollPane.setBorder(null);

        int maxRows = 2;
        int scrollHeight = (CARD_SIZE + GAP) * maxRows;
        scrollPane.setPreferredSize(new Dimension(getWidth(), scrollHeight));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 加载本地 .yml 文件并生成卡片
        File projectDir = new File(PROJECT_DIR);
        if (!projectDir.exists()) projectDir.mkdirs();

        File[] yamlFiles = projectDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".yml") || name.toLowerCase().endsWith(".yaml"));

        if (yamlFiles != null) {
            for (File yamlFile : yamlFiles) {
                String fileName = yamlFile.getName();
                String projectName = fileName.replaceFirst("\\.(yml|yaml)$", "");
                JPanel card = createProjectCard(projectName, yamlFile.getAbsolutePath());
                projectPanel.add(card);
            }
        }

        // 页面布局组合
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    // 弹窗输入新项目名
    private void showInputDialog() {
        String name = JOptionPane.showInputDialog(this, "请输入项目名：", "创建新项目", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            String configPath = PROJECT_DIR + File.separator + name + ".yml";
            new AutoEditDragUI(name, configPath);
            dispose();
        }
    }

    // 创建一个项目的展示卡片
    private JPanel createProjectCard(String name, String path) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(CARD_SIZE, CARD_SIZE));
        panel.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2));
        panel.setBackground(new Color(245, 250, 255));
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("<html><center>" + name + "</center></html>", JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        panel.add(label, BorderLayout.CENTER);

        panel.setToolTipText(path);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new AutoEditDragUI(name, path);
                dispose();
            }
        });

        return panel;
    }

    // 支持自动换行的布局类
    public static class WrapLayout extends FlowLayout {
        public WrapLayout() {
            super();
        }

        public WrapLayout(int align) {
            super(align);
        }

        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0 && target.getParent() != null)
                    targetWidth = target.getParent().getWidth();
                if (targetWidth == 0)
                    targetWidth = Integer.MAX_VALUE;

                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right - getHgap() * 2;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;

                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + getVgap();
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0)
                            rowWidth += getHgap();
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }

                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight;

                dim.width += insets.left + insets.right + getHgap() * 2;
                dim.height += insets.top + insets.bottom + getVgap() * 2;

                return dim;
            }
        }
    }
}
