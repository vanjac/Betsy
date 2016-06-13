package betsy.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.*;
import betsy.grammar.*;

/**
 * The LogFrame is used by Betsy to get user input and to write chatbot
 * responses. It has a textbox at the bottom for user input, which can be
 * disabled, and a large, resizable text area for output. These have
 * InputStreams and OutputStreams controlling them.
 * It also has a reset button for clearing the bot's memory, and a graphical
 * display of the WordTree the bot generated from the user's input sentence.
 * @author jacob
 *
 */
@SuppressWarnings("serial")
public class LogFrame extends JFrame {
	private LogPanel panel;
	private BetsyControlPanel control;
	
	private LogOutStream outputStream;
	
	private PipedOutputStream inputPipeOut;
	private PipedInputStream inputPipeIn;
	
	private volatile boolean ready;
	private volatile boolean reset;
	
	private boolean isEnabled;
	
	public LogFrame() {
		super("Bot");
		ready = false;
		reset = false;
		isEnabled = false;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGUI();
				ready = true;
			}
		});
		
		outputStream = new LogOutStream();
		
		inputPipeOut = new PipedOutputStream();
		try {
			inputPipeIn = new PipedInputStream(inputPipeOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check if the frame GUI is fully set up and ready for input
	 * @return true if the frame is ready
	 */
	public boolean isReady() {
		return ready;
	}
	
	/**
	 * Check if the reset button has been clicked
	 * @return true if it has
	 */
	public boolean resetClicked() {
		if(reset) {
			reset = false;
			return true;
		}
		return false;
	}
	
	/**
	 * Enable the user input textbox.
	 */
	public void enable() {
		isEnabled = true;
	}
	
	/**
	 * Disable the user input textbox.
	 */
	public void disable() {
		isEnabled = false;
	}
	
	private void createGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new LogPanel();
		add(panel, BorderLayout.WEST);
		control = new BetsyControlPanel();
		add(control, BorderLayout.CENTER);
		
		setMinimumSize(new Dimension(786, 0));
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Close the LogFrame.
	 */
	public void close() {
		setVisible(false);
		dispose();
	}
	
	/**
	 * Display a WordTree graphically in the log window. Appends to any
	 * existing WordTrees.
	 * @param tree The WordTree to add to the display
	 */
	public void addTree(WordTree<StructureTag> tree) {
		control.addTree(tree);
	}
	
	/**
	 * Remove all WordTrees from the graphical display.
	 */
	public void clearTree() {
		control.clearTree();
	}
	
	/*
	 * Based on:
	 * http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TextDemoProject/src/components/TextDemo.java
	 */
	public class LogPanel extends JPanel
			implements ActionListener, FocusListener {
		protected JTextArea textArea;
		protected JTextField textField;

		public LogPanel() {
			super(new BorderLayout(0, 0));

			textArea = new JTextArea(24, 40);
			textArea.setEditable(true);
			textArea.addFocusListener(this);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			JScrollPane scrollPane = new JScrollPane(textArea);
			
			textField = new JTextField(24);
			textField.addActionListener(this);
			
			add(scrollPane, BorderLayout.CENTER);
			add(textField, BorderLayout.PAGE_END);
		}
		
		public void write(int i) {
	        textArea.append(Character.toString((char)i));
	        textArea.setCaretPosition(textArea.getDocument().getLength());
		}
		
		public void clear() {
			textArea.setText("");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(!isEnabled)
				return;
			String text = textField.getText() + "\n";
			textField.setText("");
			
			textArea.append(text);
			try {
				for(char c : text.toCharArray())
					inputPipeOut.write(c);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void focusGained(FocusEvent arg0) {
			//called by text area
			textField.requestFocusInWindow();
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class BetsyControlPanel extends JPanel implements ActionListener {
		
		JScrollPane wordTreeScroll;
		JTree wordTree;
		DefaultMutableTreeNode wordTreeRoot;
		DefaultTreeModel treeModel;
		
		JPanel buttonPanel;
		JButton resetButton;
		
		public BetsyControlPanel() {
			super(new BorderLayout(0, 0));
			
			buttonPanel = new JPanel();
			
			resetButton = new JButton("Reset");
			resetButton.addActionListener(this);
			buttonPanel.add(resetButton);
			
			wordTreeRoot =
					new DefaultMutableTreeNode("Input");
			treeModel = new DefaultTreeModel(wordTreeRoot);
			wordTree = new JTree(treeModel);
			wordTree.setLargeModel(true); //prevent text truncation
			wordTreeScroll = new JScrollPane(wordTree);
			
			add(buttonPanel, BorderLayout.PAGE_START);
			add(wordTreeScroll, BorderLayout.CENTER);
		}
		
		public void clearTree() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					while(wordTreeRoot.getChildCount() != 0) {
						treeModel.removeNodeFromParent(
								(MutableTreeNode)wordTreeRoot.getFirstChild());
					}
				}
			});
			
		}
		
		public void addTree(WordTree<StructureTag> tree) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					treeModel.insertNodeInto(toTreeNode(tree), wordTreeRoot,
							wordTreeRoot.getChildCount());
					expandRecursive(wordTreeRoot);
				}
			});
			
		}
		
		private void expandRecursive(DefaultMutableTreeNode n) {
			TreePath p = new TreePath(n.getPath());
			wordTree.expandPath(p);
			for(int i = 0; i < n.getChildCount(); i++) {
				expandRecursive((DefaultMutableTreeNode)n.getChildAt(i));
			}
		}
		
		private DefaultMutableTreeNode toTreeNode(
				WordTree<StructureTag> t) {
			DefaultMutableTreeNode tree;
			
			StructureTag type = t.getType();
			
			String label = type.toString().toLowerCase();
			label = label.substring(0, 1).toUpperCase() + label.substring(1);
			label = label.replace('_', ' ');
			
			if(type.equals(StructureTag.ROOT)) {
				if(t.numChildren() == 1) {
					return toTreeNode(t.getChild(0));
				} else {
					label = "Sentence";
				}
			}
			
			if(type.equals(StructureTag.NOUN_PHRASE)
					|| type.equals(StructureTag.VERB_PHRASE)
					|| type.equals(StructureTag.ADJECTIVE_PHRASE)
					|| type.equals(StructureTag.ADVERB_PHRASE)
					|| type.equals(StructureTag.PREPOSITION_PHRASE)
					|| type.equals(StructureTag.PARTICLE_PHRASE)
					) {
				if(t.numChildren() == 1) {
					return toTreeNode(t.getChild(0));
				}
			}
			
			List<WordTree<StructureTag>> children = t.getChildren();
			if(type.isA(StructureTag.CategoryTag.SINGLE_CHILD)
					&& !type.equals(StructureTag.QUESTION_FRAGMENT)) {
				if(t.numChildren() == 1) {
					children = t.getChild(0).getChildren();
				}
			}
			
			if(t.isLeaf()) {
				tree = new DefaultMutableTreeNode(label + ": " + t.getWord());
			} else {
				tree = new DefaultMutableTreeNode(label);
				for(WordTree<StructureTag> child : children) {
					tree.add(toTreeNode(child));
				}
			}
			return tree;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == resetButton) {
				// have the log panel write whatever the user has written
				panel.actionPerformed(null);
				panel.clear();
				reset = true;
			}
		}
		
	}
	
	public class LogOutStream extends OutputStream {
		
		@Override
		public void write(int i) throws IOException {
			panel.write(i);
		}

	}
	
	/**
	 * Get an OutputStream that can be used to write text to the text area.
	 * @return an OutputStream tied to this LogFrame
	 */
	public OutputStream outputStream() {
		return outputStream;
	}
	
	/**
	 * Get the InputStream used to get text that the user has written in the
	 * textbox.
	 * @return an InputStream tied to this LogFrame
	 */
	public InputStream inputStream() {
		return inputPipeIn;
	}
}
