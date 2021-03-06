package org.mars_sim.msp.ui.sodium.swidgets.app;

import javax.swing.*;

import org.mars_sim.msp.core.sodium.*;
import org.mars_sim.msp.ui.sodium.swidgets.SButton;
import org.mars_sim.msp.ui.sodium.swidgets.SLabel;
import org.mars_sim.msp.ui.sodium.swidgets.STextField;

import java.awt.*;
import java.awt.event.*;

public class translate {
    public static void main(String[] args) {
        JFrame view = new JFrame("Translate");
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setLayout(new FlowLayout());
        STextField english = new STextField("I like FRP");
        SButton translate = new SButton("Translate");
        Stream<String> sLatin =
            translate.sClicked.snapshot(english.text, (u, txt) ->
                txt.trim().replaceAll(" |$", "us ").trim()
            );
        Cell<String> latin = sLatin.hold("");
        SLabel lblLatin = new SLabel(latin);
        view.add(english);
        view.add(translate);
        view.add(lblLatin);
        view.setSize(400, 160);
        view.setVisible(true);
    }
}

