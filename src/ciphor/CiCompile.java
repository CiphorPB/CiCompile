package ciphor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import net.miginfocom.swing.MigLayout;

/**
 * @author Ciphor
 *
 */
public class CiCompile {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
					CiCompile window = new CiCompile();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private JButton btnClearText;
	private JButton btnCompile;
	private JButton btnImport;
	private JButton btnJdkPath;
	private JButton btnStartRSBot;
	private String className;
	private JPanel contentPane;
	private JFileChooser fileChooser;
	private FileNameExtensionFilter filter;
	private JFrame frame;
	private List<Image> icons = new ArrayList<Image>();
	private boolean isZip = false;
	private List<File> javaFilesToCompile = new ArrayList<File>();
	private String JDKpath;
	private String JRE;
	private JLabel lblInstruct;
	private JLabel lblLogo;
	private Component lblMemory;
	private JLabel lblOutput;
	private HyperLinkLabel lblPowerbotForumAccount;
	private PrintStream out;
	private DefaultCaret outputCaret;
	private final String outputFolder = "bin\\";
	private Properties prop;
	private String rsbotVersion;
	private JScrollPane scrollTextArea;
	private JScrollPane scrollTextOutput;
	private final String sourceFolder = "src\\";
	private JTextArea textArea;
	private JTextArea textAreaOutput;
	private JTextField textFieldMemory;

	/**
	 * Create the application.
	 */
	public CiCompile() {
		initialize();
		sayHello();
		findRSBot();
		main();
	}

	private void compile() {
		try {
			System.out.println("Retrieving JDK path from CiCompile.properties...");

			getJDK();

			System.setProperty("java.home", JDKpath);

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

			StandardJavaFileManager fileManager = compiler
					.getStandardFileManager(diagnostics, null, null);

			Iterable<? extends JavaFileObject> compilationUnits = null;

			if (!isZip) {
				try {
					Writer output = null;
					File file = new File(sourceFolder + className + ".java");
					output = new BufferedWriter(new FileWriter(file));
					output.write(textArea.getText());
					output.close();
					javaFilesToCompile.add(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			compilationUnits = fileManager
					.getJavaFileObjectsFromFiles(javaFilesToCompile);

			String[] optionList = new String[] { "-cp", rsbotVersion, "-d",
					outputFolder };
			Iterable<String> options = Arrays.asList(optionList);

			CompilationTask task = compiler.getTask(null, fileManager,
					diagnostics, options, null, compilationUnits);

			System.out.println("I'm now compiling the code...");

			boolean success = task.call();

			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics
					.getDiagnostics()) {
				System.out.println(diagnostic.getKind() + " on line:"
						+ diagnostic.getLineNumber() + ": "
						+ diagnostic.getMessage(null) + " in "
						+ diagnostic.getSource().toUri());
			}

			if (success)
				System.out.println("Woohoo successfully compiled!");
			else
				System.out.println("Damn, seems there are some errors.");

			try {
				fileManager.close();
				javaFilesToCompile.clear();
				isZip = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			System.out.println("Invalid JDK.");
		}
	}

	private void createBinFolder() {
		File bin = new File("bin");
		if (!bin.exists()) {
			System.out.println("I'm creating creating the bin directory...");
			boolean success = bin.mkdir();
			if (success)
				System.out.println("Yup, done that!");
			else
				System.out.println("Damn, I was unable to create it.");
		}
	}

	private void createSrcFolder() {
		File src = new File("src");
		if (!src.exists()) {
			System.out.println("I'm creating the src directory...");
			boolean success = src.mkdir();
			if (success)
				System.out.println("Yup, done that!");
			else
				System.out.println("Damn, I was unable to create it.");
		}
	}

	private void disable() {
		btnJdkPath.setEnabled(false);
		btnImport.setEnabled(false);
		btnClearText.setEnabled(false);
		btnCompile.setEnabled(false);
		btnStartRSBot.setEnabled(false);
	}

	private void findPublicClass() {
		System.out.println("I'm searching for the public class...");
		Pattern pattern = Pattern.compile("\\s*public\\s+class\\s+(\\w+)");
		Matcher matcher = pattern.matcher(textArea.getText());
		if (matcher.find()) {
			System.out.println("I found the public class, clever me!");
			className = matcher.group(1);
		} else
			System.out.println("Damn, I couldn't find the public class.");
	}

	private void findRSBot() {
		System.out.println("I'm searching for RSBot...");
		File currDir = new File(System.getProperty("user.dir"));
		File[] Files = currDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches("RSBot-\\d\\d\\d\\d.jar");
			}
		});
		if (Files.length == 0) {
			System.out
					.println("I couldn't find RSBot, I have to be in the same folder.");
			disable();
		} else if (Files.length > 1) {
			System.out
					.println("I found multiple versions of RSBot, please remove duplicates or older versions.");
			disable();
		} else {
			System.out.println("I found RSBot, clever me!");
			rsbotVersion = Files[0].toString();
		}
	}

	private void getFilesFromSourceFolder(String dir) {
		File src = new File(dir);
		File[] allFiles = src.listFiles();
		for (File file : allFiles) {
			if (file.isFile() && file.getName().endsWith(".java")) {
				javaFilesToCompile.add(file);
			}
			if (file.isDirectory()) {
				getFilesFromSourceFolder(file.getAbsolutePath());
			}
		}
	}

	private void getJDK() {
		try {
			prop = new Properties();
			prop.load(new FileInputStream("CiCompile.properties"));
			JDKpath = prop.getProperty("path");
		} catch (IOException e) {
			System.out.println("Please set up the path to JDK.");
		}
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("CiCompile");
		frame.setMinimumSize(new Dimension(530, 300));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		icons.add(new ImageIcon(getClass().getResource(
				"/ciphor/images/16x16.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource(
				"/ciphor/images/32x32.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource(
				"/ciphor/images/64x64.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource(
				"/ciphor/images/128x128.png")).getImage());
		frame.setIconImages(icons);

		contentPane = new JPanel();
		contentPane.setBackground(new Color(51, 51, 51));

		lblLogo = new JLabel("");
		lblLogo.setIcon(new ImageIcon(CiCompile.class
				.getResource("/ciphor/images/logo.png")));
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);

		lblInstruct = new JLabel(
				"Please paste code below or import a zip archive.");
		lblInstruct.setFont(UIManager.getFont("Label.font"));
		lblInstruct.setForeground(new Color(51, 255, 51));

		btnJdkPath = new JButton("JDK Path");
		btnJdkPath.setForeground(new Color(0, 0, 0));
		btnJdkPath.setFont(UIManager.getFont("Button.font"));
		btnJdkPath.setBackground(new Color(51, 51, 51));

		btnImport = new JButton("Import...");
		btnImport.setForeground(new Color(0, 0, 0));
		btnImport.setFont(UIManager.getFont("Button.font"));
		btnImport.setBackground(new Color(51, 51, 51));

		fileChooser = new JFileChooser();
		filter = new FileNameExtensionFilter(".zip", "zip");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(filter);

		btnClearText = new JButton("Clear text");
		btnClearText.setForeground(new Color(0, 0, 0));
		btnClearText.setFont(UIManager.getFont("Button.font"));
		btnClearText.setBackground(new Color(51, 51, 51));

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setToolTipText("Ctrl + V or Command + V");
		scrollTextArea = new JScrollPane(textArea);
		scrollTextArea.setPreferredSize(new Dimension(0, 100));

		btnCompile = new JButton("Compile");
		btnCompile.setForeground(new Color(0, 0, 0));
		btnCompile.setFont(UIManager.getFont("Button.font"));
		btnCompile.setBackground(new Color(51, 51, 51));
		btnCompile.setPreferredSize(new Dimension(40, 50));

		lblOutput = new JLabel("Output:");
		lblOutput.setFont(UIManager.getFont("Label.font"));
		lblOutput.setForeground(new Color(51, 255, 51));

		textAreaOutput = new JTextArea();
		textAreaOutput.setForeground(new Color(255, 255, 255));
		textAreaOutput.setBackground(new Color(102, 102, 102));
		textAreaOutput.setLineWrap(true);
		textAreaOutput.setWrapStyleWord(true);
		textAreaOutput.setEditable(false);
		scrollTextOutput = new JScrollPane(textAreaOutput);
		scrollTextOutput.setPreferredSize(new Dimension(0, 100));
		outputCaret = (DefaultCaret) textAreaOutput.getCaret();
		outputCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		out = new PrintStream(new TextAreaOutputStream(textAreaOutput));
		System.setOut(out);
		System.setErr(out);

		lblPowerbotForumAccount = new HyperLinkLabel(
				"http://www.powerbot.org/community/user/481557-ciphor/",
				"Ciphor Profile");
		lblPowerbotForumAccount.setFont(UIManager.getFont("Label.font"));
		lblPowerbotForumAccount.setForeground(new Color(0, 204, 255));

		lblMemory = new JLabel("Allocate memory:");
		lblMemory.setFont(UIManager.getFont("Label.font"));
		lblMemory.setForeground(new Color(51, 255, 51));

		textFieldMemory = new JTextField();
		textFieldMemory.setText("-Xmx1024m");
		textFieldMemory.setMinimumSize(new Dimension(60, 10));
		textFieldMemory.setFont(UIManager.getFont("TextField.font"));

		btnStartRSBot = new JButton("Start RSBot");
		btnStartRSBot.setForeground(new Color(0, 0, 0));
		btnStartRSBot.setFont(UIManager.getFont("Button.font"));
		btnStartRSBot.setBackground(new Color(51, 51, 51));

		contentPane.setLayout(new MigLayout());
		contentPane.add(lblLogo, "grow, span, wrap");
		contentPane.add(lblInstruct);
		contentPane.add(btnJdkPath, "split 3, alignx right");
		contentPane.add(btnImport);
		contentPane.add(btnClearText, "wrap");
		contentPane.add(scrollTextArea, "span, push, grow, wrap");
		contentPane.add(btnCompile, "span, push, grow");
		contentPane.add(lblOutput, "wrap");
		contentPane.add(scrollTextOutput, "span, push, grow, wrap");
		contentPane.add(lblPowerbotForumAccount);
		contentPane.add(lblMemory, "split 3, alignx right");
		contentPane.add(textFieldMemory);
		contentPane.add(btnStartRSBot);
		frame.getContentPane().add(contentPane);
		frame.pack();
		frame.setLocationRelativeTo(null);
		textArea.requestFocusInWindow();
	}

	private void main() {

		btnJdkPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane
						.showInputDialog("Path to JDK, for example: C:\\Program Files\\Java\\jdk1.7.0_07");
				if (input != null)
					storeJDK(input);
			}
		});

		btnClearText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.setText(null);
			}
		});

		btnImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileChooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					isZip = true;
					ZipExtractor zipExtractor = new ZipExtractor();
					zipExtractor.unzip(fileChooser.getSelectedFile(),
							sourceFolder);
					getFilesFromSourceFolder(sourceFolder);
				} else {
					isZip = false;
				}
			}
		});

		btnCompile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Clear Output.
				textAreaOutput.setText("");
				// Create src and bin folders if we have found RSBot.
				if (rsbotVersion != null) {
					createSrcFolder();
					createBinFolder();
					// Compile
					if (javaFilesToCompile != null) {
						if (!isZip) {
							findPublicClass();
							if (className != null)
								compile();
						} else {
							compile();
						}
					}
				}
			}
		});

		btnStartRSBot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					System.setProperty("java.home", JRE);
					ProcessBuilder builder = new ProcessBuilder("java",
							textFieldMemory.getText(), "-jar", rsbotVersion);
					Process process = builder.start();
					Thread.sleep(2000);
					frame.toFront();
					final InputStream inputStream = process.getInputStream();
					final Scanner in = new Scanner(inputStream);
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
						protected Void doInBackground() throws Exception {
							while (in.hasNextLine()) {
								String line = in.nextLine();
								String separator = System
										.getProperty("line.separator");
								System.out.print(line + separator);
							}
							in.close();
							inputStream.close();
							return null;
						}
					};
					worker.execute();
				} catch (Exception e1) {
				}
			}
		});

	} // end main

	private void sayHello() {
		System.out.println("Hello " + System.getProperty("user.name")
				+ ", I'm CiCompile!");
		JRE = System.getProperty("java.home");
	}

	private void storeJDK(String path) {
		try {
			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				prop = new Properties();
				prop.setProperty("path", path);
				prop.store(new FileOutputStream("CiCompile.properties"), null);
				System.out.println("\"" + path + "\""
						+ " stored in: CiCompile.properties");
			} else {
				System.out.println("Not a valid file path.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

} // end class
