package se.johannes.pctestapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI {
    public static final int LF = 0;
    public static final int LB = 1;
    public static final int RB = 2;
    public static final int RF = 3;
    private Callback callback;

    Zone[] zones = new Zone[4];
    JCheckBox powered;

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.showGUI(new Callback());
        gui.enablePowerControl(true);
    }

    public void showGUI(final Callback callback) {
        JFrame frame = new JFrame("Simple GUI to control my induction cooker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < zones.length; i++) {
            zones[i] = new Zone(i);
            c.gridx = i / 2;
            switch (i) {
                case 3:
                case 0:
                    c.gridy = 1;
                    break;
                case 2:
                case 1:
                default:
                    c.gridy = 0;
                    break;
            }
            frame.getContentPane().add(zones[i], c);
        }

        c.gridx = 0;
        c.gridy = 2;
        JCheckBox power = new JCheckBox("Power");
        frame.getContentPane().add(power, c);
        power.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                boolean powered = event.getStateChange() == ItemEvent.SELECTED;
                callback.onPowerOnOffChanged(powered);
            }
        });

        powered = new JCheckBox("Powered");
        powered.setEnabled(false);
        c.gridx = 1;
        c.gridy = 2;
        frame.getContentPane().add(powered, c);

        frame.pack();
        frame.setVisible(true);
        this.callback = callback;
    }

    public void setPowered(boolean on) {
        powered.setSelected(on);
    }

    public void enablePowerControl(boolean enable) {
        for (int i = 0; i < zones.length; i++) {
            zones[i].enableControl(enable);
        }
    }

    public void setPotPresent(boolean[] potPresent) {
        System.out.println("setPotpres..");
        for (int i = 0; i < potPresent.length; i++) {
            zones[i].setPotPresent(potPresent[i]);
        }
    }

    public void setPotHot(boolean[] potHot) {
        for (int i = 0; i < potHot.length; i++) {
            zones[i].setPotHot(potHot[i]);
        }
    }

    public void setLimitPower(int[] values) {
        for (int i = 0; i < values.length; i++) {
            zones[i].setLimitPower(values[i]);
        }
    }

    public void setPower(int[] values) {
        for (int i = 0; i < values.length; i++) {
            zones[i].setPower(values[i]);
        }
    }

    public static class Callback {
        public void onPowerOnOffChanged(boolean power) {
            System.out.println("Powered o" + (power ? "n" : "ff"));
        }

        public void onPowerLevelChanged(int zone, int powerLevel) {
            System.out.println("Power level in zone " + zone + " changed" +
                    " to " + powerLevel + " in GUI");
        }
    }

    @SuppressWarnings("serial")
    private class Zone extends JPanel implements ChangeListener {
        JSlider control = new JSlider(JSlider.HORIZONTAL, 0, 11, 0);
        JSlider  actual = new JSlider(JSlider.HORIZONTAL, 0, 11, 0);
        JCheckBox hot = new JCheckBox("Hot");
        JCheckBox present = new JCheckBox("Present");
        int zone;

        public Zone(int zone) {
            this.zone = zone;
            control.setMajorTickSpacing(1);
            control.setPaintTicks(true);
            control.setPaintLabels(true);
            control.setEnabled(false);

            actual.setMajorTickSpacing(1);
            actual.setPaintTicks(true);
            actual.setPaintLabels(true);
            actual.setEnabled(false);

            control.addChangeListener(this);
            hot.setEnabled(false);
            present.setEnabled(false);

            setLayout(new BorderLayout());
            add(hot, BorderLayout.LINE_START);
            add(present, BorderLayout.LINE_END);

            JPanel sliders = new JPanel();
            sliders.setLayout(new BorderLayout());
            sliders.add(actual, BorderLayout.PAGE_START);
            sliders.add(control, BorderLayout.PAGE_END);
            add(sliders, BorderLayout.PAGE_END);

            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        public void setPotPresent(boolean potPresent) {
            present.setSelected(potPresent);
        }

        public void setPotHot(boolean potHot) {
            hot.setSelected(potHot);
        }

        public void setLimitPower(int value) {
            actual.setValue(value);
        }

        public void setPower(int value) {
            control.setValue(value);
        }

        public void enableControl(boolean enable) {
            control.setEnabled(enable);
        }

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            JSlider slider = (JSlider) changeEvent.getSource();
            if (slider != null) {
                callback.onPowerLevelChanged(zone, slider.getValue());
            }
        }
    }
}
