package org.hplcretentionpredictor;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;
import org.hplcretentionpredictor.HPLCRetentionPredictorApp.TaskUpdateDatabase;

public class UpdateProgressDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jBtnStart = null;
	public JProgressBar jProgressBar;
	public JButton jBtnCancel;
	private JLabel jLblConnectingTo;
	public JLabel jLblStatus;
	private TaskUpdateDatabase taskUpdateDb = null;
	
	public UpdateProgressDialog(Frame owner, TaskUpdateDatabase taskUpdateDb) {
		super(owner);
		this.taskUpdateDb = taskUpdateDb;
		initialize();
		jBtnStart.addActionListener(this);
		jBtnCancel.addActionListener(this);
	}
	
	private void initialize() {
		this.setSize(480, 200);
		this.setContentPane(getJContentPanel());
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("Update Isocratic Database");
	
	}
	
	private JPanel getJContentPanel(){
		if(jContentPane == null){
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJProgressBar(), null);
			jContentPane.add(getJBtnStart(), null);
			jContentPane.add(getJBtnCancel(), null);
			jContentPane.add(getJLblConnectingTo(), null);
			jContentPane.add(getJLblStatus(), null);
		}
		return jContentPane;
	}
	
	private JLabel getJLblConnectingTo(){
		if(jLblConnectingTo == null){
			jLblConnectingTo = new JLabel();
			jLblConnectingTo.setBounds(new Rectangle(40, 20, 400, 16));
			jLblConnectingTo.setText("Finding updates on www.retentionprediction.org ...");
		}
		return jLblConnectingTo;
	}
	
	private JLabel getJLblStatus(){
		if(jLblStatus == null){
			jLblStatus = new JLabel();
			jLblStatus.setBounds(new Rectangle(40, 50, 400, 16));
			jLblStatus.setText("");
		}
		return jLblStatus;
	}

	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setBounds(new Rectangle(40, 80, 381, 25));
			jProgressBar.setStringPainted(false);
		}
		return jProgressBar;
	}
	
	private JButton getJBtnStart(){
		if(jBtnStart == null){
			jBtnStart = new JButton();
			jBtnStart.setBounds(new Rectangle(40, 120, 160, 36));
			jBtnStart.setActionCommand("Start");
			jBtnStart.setText("Start");
		}
		return jBtnStart;
	}
	
	private JButton getJBtnCancel(){
		if(jBtnCancel == null){
			jBtnCancel = new JButton();
			jBtnCancel.setBounds(new Rectangle(260, 120, 160, 36));
			jBtnCancel.setActionCommand("Cancel");
			jBtnCancel.setText("Cancel");
		}
		return jBtnCancel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "Start"){
    		taskUpdateDb.execute();
    		this.jBtnCancel.setText("Close");
    		this.jBtnCancel.setActionCommand("Close");
		}
		if(e.getActionCommand() == "Cancel"){
			this.setVisible(false);
			this.dispose();
			File original_database = new File(HPLCRetentionPredictorApp.fileName);
			File backup_database = new File(HPLCRetentionPredictorApp.fileName+".bak");
			if(backup_database.exists()){
				if(original_database.exists()){
					original_database.delete();
				}
				try {
					FileUtils.moveFile(backup_database, original_database);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		else if(e.getActionCommand() == "Close"){
			this.setVisible(false);
			this.dispose();
			this.jBtnCancel.setText("Cancel");
		}
		
	}
	
	

}
