package net.darktree;

import javax.swing.*;
import javax.swing.plaf.basic.DefaultMenuLayout;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MainWindow extends JFrame {

    private KeywordStyledDocument currentDocument = null;
    private final Map<String, KeywordStyledDocument> openDocuments = new HashMap<>();

    private final Style defaultStyle;
    private final Style highlightStyle;
    private final Font defaultFont;

    private final JPanel codePanelWrapper;

    final int windowWidth = 1200;
    final int windowHeight = 800;

    private void registerLocalFont(String path) {
        try {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            Objects.requireNonNull(stream);
            environment.registerFont(Font.createFont(Font.TRUETYPE_FONT, stream));
        } catch(FontFormatException | IOException e){
            e.printStackTrace();
        }
    }

    public MainWindow() {
        // default font
        registerLocalFont("fonts/JetBrainsMono-Regular.ttf");
        defaultFont = new Font("JetBrains Mono Regular", Font.PLAIN, 13);

        // default text style
        StyleContext styleContext = new StyleContext();
        defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setLineSpacing(defaultStyle, 1.2f);

        // style of highlighted words
        highlightStyle = styleContext.addStyle("ConstantWidth", null);
        StyleConstants.setForeground(highlightStyle, new Color(0x729fcf));
        StyleConstants.setLineSpacing(highlightStyle, 1.2f);

        setTitle("Ratify | Untitled Document");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(windowWidth, windowHeight));

        codePanelWrapper = new JPanel(new BorderLayout());
        add(codePanelWrapper, BorderLayout.CENTER);

        // setting the save(ctrl+s) action
        KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKeyStroke, "saveCurrentFile");
        getRootPane().getActionMap().put("saveCurrentFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentFile();
            }
        });

        addMenuBar();

        pack();
        setLocationRelativeTo(null);
    }

    public void openFile(String path) throws IOException {
        if (openDocuments.containsKey(path)) {
            currentDocument = openDocuments.get(path);
        } else {
            currentDocument = new KeywordStyledDocument(defaultFont, defaultStyle, highlightStyle, path);
            openDocuments.put(path, currentDocument);
        }

        codePanelWrapper.removeAll();
        codePanelWrapper.add(currentDocument.getPanel(), BorderLayout.CENTER);

        setTitle("Ratify | " + currentDocument.getFileName());

        codePanelWrapper.revalidate();
        codePanelWrapper.repaint();
    }

    private void saveCurrentFile() {
        try {
            currentDocument.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chooseAndOpenFile() {
        Optional<String> pathOpt = chooseFileAndGetPath();
        if (pathOpt.isEmpty()) {
            return;
        }

        String path = pathOpt.get();
        try {
            openFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<String> chooseFileAndGetPath() {
        FileDialog dialog = new FileDialog((Frame)null, "Select file to open");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);

        String path = dialog.getDirectory() + dialog.getFile();
        dialog.dispose();

        if ("nullnull".equals(path)) {
            return Optional.empty();
        }

        return Optional.of(path.substring(System.getProperty("user.dir").length() + 1));
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new DefaultMenuLayout(menuBar, BoxLayout.X_AXIS));

        JMenu file = new JMenu("File");
        JMenu sharing = new JMenu("Sharing");

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(e -> {
            chooseAndOpenFile();
        });

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> {
            saveCurrentFile();
        });

        JMenuItem hostMenuItem = new JMenuItem("Host");
        hostMenuItem.addActionListener(e -> {
            System.out.println("Host");
        });

        JMenuItem joinMenuItem = new JMenuItem("Join");
        joinMenuItem.addActionListener(e -> {
            System.out.println("Join");
        });

        file.add(openMenuItem);
        file.add(saveMenuItem);
        sharing.add(hostMenuItem);
        sharing.add(joinMenuItem);

        menuBar.add(file);
        menuBar.add(sharing);
        setJMenuBar(menuBar);
    }
}
