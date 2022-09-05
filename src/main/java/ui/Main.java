package ui;

import com.alee.laf.WebLookAndFeel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new WebLookAndFeel());
        Controller c = new Controller();
        c.init();
    }
}