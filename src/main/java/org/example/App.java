package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.UI.ProjectSelector;
import javax.swing.*;

@Slf4j
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProjectSelector::new);
    }
}
