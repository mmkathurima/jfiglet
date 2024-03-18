package com.jfiglet;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JFigletUI {
    private final JFrame frame = new JFrame("JFiglet GUI");
    private final JPanel panel = new JPanel();
    private final JTextField input = new JTextField();
    private final JTextArea output = new JTextArea();
    private final JComboBox<Font> monospaceFonts = new JComboBox<>();
    private final JButton figletFonts = new JButton("Choose Figlet font file (*.flf) or leave default");
    private final JButton submit = new JButton("Submit");
    private final GridBagLayout gridBagLayout = new GridBagLayout();
    private final GridBagConstraints gridBagConstraints = new GridBagConstraints();
    private File selectedFlf;

    public JFigletUI() {
        this.setupUI();
    }

    private void setupUI() {
        this.output.setEditable(false);

        for (Font font : this.getMonospaceFonts())
            this.monospaceFonts.addItem(font);

        this.panel.setLayout(this.gridBagLayout);

        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 0;
        this.gridBagConstraints.ipadx = 20;
        this.gridBagConstraints.ipady = 20;
        this.gridBagConstraints.fill = GridBagConstraints.BOTH;
        this.gridBagConstraints.weightx = 10;
        this.panel.add(new JLabel("Enter text:"), this.gridBagConstraints);

        this.gridBagConstraints.gridx = 1;
        this.gridBagConstraints.gridy = 0;
        this.gridBagConstraints.weightx = 90;
        this.panel.add(new JScrollPane(this.input), this.gridBagConstraints);

        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 1;
        this.gridBagConstraints.weightx = 10;
        this.panel.add(new JLabel("Change display fonts"), this.gridBagConstraints);

        this.gridBagConstraints.gridx = 1;
        this.gridBagConstraints.gridy = 1;
        this.gridBagConstraints.weightx = 90;
        this.panel.add(this.monospaceFonts, this.gridBagConstraints);

        this.gridBagConstraints.gridx = 1;
        this.gridBagConstraints.gridy = 2;
        this.gridBagConstraints.weightx = 90;
        this.panel.add(this.figletFonts, this.gridBagConstraints);

        this.gridBagConstraints.gridx = 1;
        this.gridBagConstraints.gridy = 3;
        this.gridBagConstraints.weightx = 90;
        this.panel.add(this.submit, this.gridBagConstraints);

        this.gridBagConstraints.gridx = 1;
        this.gridBagConstraints.gridy = 4;
        this.gridBagConstraints.weighty = 1;
        this.gridBagConstraints.weightx = 90;
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BorderLayout());
        legendPanel.setBorder(BorderFactory.createTitledBorder("Output"));
        legendPanel.add(new JScrollPane(this.output), BorderLayout.CENTER);
        this.panel.add(legendPanel, this.gridBagConstraints);

        this.setupEvents();

        this.frame.setSize(new Dimension(900, 650));
        this.frame.setContentPane(this.panel);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
        //this.frame.pack();
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void setupEvents() {
        this.monospaceFonts.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                this.setText(((Font) value).getFontName());
                return this;
            }
        });
        this.monospaceFonts.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Font newValue = (Font) e.getItem();
                    JFigletUI.this.output.setFont(newValue.deriveFont(18f));
                }
            }
        });
        this.figletFonts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // create an object of JFileChooser class
                JFileChooser j = new JFileChooser();

                // restrict the user to select files of all types
                j.setAcceptAllFileFilterUsed(false);

                // set a title for the dialog
                j.setDialogTitle("Select a .flf file");

                // only allow files of .txt extension
                FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .flf files", "flf");
                j.addChoosableFileFilter(restrict);

                // invoke the showsOpenDialog function to show the save dialog
                int r = j.showOpenDialog(JFigletUI.this.frame);

                // if the user selects a file
                if (r == JFileChooser.APPROVE_OPTION)
                    JFigletUI.this.selectedFlf = j.getSelectedFile();
                    // if the user cancelled the operation
                else
                    JFigletUI.this.selectedFlf = null;
            }
        });
        this.submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File selectedFontFile = JFigletUI.this.selectedFlf;
                String input = JFigletUI.this.input.getText();
                JFigletUI.this.output.setFont(((Font) JFigletUI.this.monospaceFonts.getSelectedItem()).deriveFont(18f));
                try {
                    if (!input.isBlank()) {
                        if (selectedFontFile == null)
                            JFigletUI.this.output.setText(FigletFont.convertOneLine(input));
                        else
                            JFigletUI.this.output.setText(FigletFont.convertOneLine(selectedFontFile, input));
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private List<Font> getMonospaceFonts() {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        List<Font> monoFonts1 = new ArrayList<>();

        FontRenderContext frc = new FontRenderContext(
                null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT
        );
        for (Font font : fonts) {
            Rectangle2D iBounds = font.getStringBounds("i", frc);
            Rectangle2D mBounds = font.getStringBounds("m", frc);
            if (iBounds.getWidth() == mBounds.getWidth())
                monoFonts1.add(font);
        }
        return monoFonts1;
    }

    public static void main(String[] args) {
        new JFigletUI();
    }
}
