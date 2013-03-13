/*
 *
 * Copyright (c) 2008-2009, John Stoner
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * Neither the name of the John Stoner nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without specific 
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 * John Stoner is reachable at johnstoner2 [[at]] gmail [[dot]] com.
 * His current physical address is
 * 
 * 2358 S Marshall 
 * Chicago, IL 60623
 */

package boogiepants.display;

//import javax.jnlp.ServiceManager;
//import javax.jnlp.BasicService;
//import javax.jnlp.UnavailableServiceException;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import boogiepants.instruments.InstrumentManager;
import boogiepants.util.Util;

/**
 * Provides a unified application menu bar for all Boogiepants windows
 * 
 * @author jstoner
 * 
 */
public class MenuBar extends JMenuBar {

    private ArrayList<JMenuItem> editModeAvailableItems;

    /**
     * 
     */
    public MenuBar() {
        editModeAvailableItems = new ArrayList<JMenuItem>();
        int keymask = 0;
        if(Util.isMac()){
            keymask = ActionEvent.META_MASK;
        }else if (Util.isWindows()){
            keymask = ActionEvent.CTRL_MASK;
        }
        final JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                keymask));
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    if (manager.getInstrumentContainer().isUpdated()){
                        int answer = JOptionPane.showConfirmDialog(fileMenu,
                                        "current file has been modified.",
                                        "Warning",
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.WARNING_MESSAGE);

                        if(answer==JOptionPane.OK_OPTION){
                            
                           manager.newPantsFile();
                        }
                    }else{
                        manager.newPantsFile();
                    }
                }
            }
        });
        fileMenu.add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                keymask));
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    if (manager.getInstrumentContainer().isUpdated()){
                        int answer = JOptionPane.showConfirmDialog(fileMenu,
                                        "current file has been modified.",
                                        "Warning",
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.WARNING_MESSAGE);

                        if(answer==JOptionPane.OK_OPTION){
                            
                           manager.openPantsFile();
                        }
                    }else{
                        manager.openPantsFile();
                    }
                }
            }
        });
        fileMenu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                keymask));
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    manager.savePantsFile();
                }
            }
        });
        fileMenu.add(saveMenuItem);

        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                (keymask | ActionEvent.SHIFT_MASK)));
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    manager.saveAsPantsFile();
                }
            }
        });
        fileMenu.add(saveAsMenuItem);

        JMenuItem importMenuItem = new JMenuItem("Import Shape");
        importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, keymask));
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    manager.importShapeDialog();
                }
            }
        });
        fileMenu.add(importMenuItem);

        if(Util.isWindows()){
            JMenuItem exitMenuItem = new JMenuItem("Exit");
            exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    InstrumentManager manager= InstrumentManager.getInstance();
                    if(manager != null){
                        if (manager.getInstrumentContainer().isUpdated()){
                            int answer = JOptionPane.showConfirmDialog(fileMenu,
                                            "current file has been modified.",
                                            "Warning",
                                            JOptionPane.OK_CANCEL_OPTION,
                                            JOptionPane.WARNING_MESSAGE);

                            if(answer==JOptionPane.OK_OPTION){
                                
                                System.exit(0);
                            }
                        }else {
                            System.exit(0);
                        }
                    }else {
                        System.exit(0);
                    }
                }
            });
            fileMenu.add(exitMenuItem);
        }

        JMenu editMenu = new JMenu("Edit");
        JMenuItem editMenuItem = new JMenuItem("Edit Mode");
        editMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                keymask));
        editMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    InstrumentManager.getInstance().toggleEditMode();
                }
            }
        });
        editMenu.add(editMenuItem);

        JMenuItem strikeMenuItem = new JMenuItem("Add Strike Instrument");
        strikeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
                keymask));
        strikeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    manager.addStrikeInstrument();
                }
            }
        });
        
        strikeMenuItem.setEnabled(false);
        strikeMenuItem.setToolTipText("available in edit mode");
        editModeAvailableItems.add(strikeMenuItem);
        editMenu.add(strikeMenuItem);

        JMenuItem scaleMenuItem = new JMenuItem("Add Scale Instrument");
        scaleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
                keymask));
        scaleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    manager.addScaleInstrument();
                }
            }
        });
        scaleMenuItem.setEnabled(false);
        scaleMenuItem.setToolTipText("available in edit mode");
        editModeAvailableItems.add(scaleMenuItem);
        editMenu.add(scaleMenuItem);

        JMenuItem pelvicMenuItem = new JMenuItem("Add Pelvic Circle Instrument");
        pelvicMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
                keymask));
        pelvicMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    manager.addPelvicCircleInstrument();
                }
            }
        });
        pelvicMenuItem.setEnabled(false);
        pelvicMenuItem.setToolTipText("available in edit mode");
        editModeAvailableItems.add(pelvicMenuItem);
        editMenu.add(pelvicMenuItem);

        JMenuItem toggleMenuItem = new JMenuItem("Add Toggle Instrument");
        toggleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,
                keymask));
        toggleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstrumentManager manager= InstrumentManager.getInstance();
                if(manager != null){
                    manager.addToggleInstrument();
                }
            }
        });
        toggleMenuItem.setEnabled(false);
        toggleMenuItem.setToolTipText("available in edit mode");
        editModeAvailableItems.add(toggleMenuItem);
        editMenu.add(toggleMenuItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem fullScreenMenuItem = new JMenuItem("Full Screen");
        fullScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                keymask));
        fullScreenMenuItem
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        BoogiepantsDisplayWindow bdw = BoogiepantsDisplayWindow
                                .getInstance();
                        bdw.toggleFullScreen();
                    }
                });
        viewMenu.add(fullScreenMenuItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpMenuItem = new JMenuItem("Boogiepants Help");
        helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                keymask));
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
////                    Object service = ServiceManager.lookup("javax.jnlp.BasicService");
////                    ((BasicService)service).showDocument(new URL("http://boogiepants.typepad.com/boogiepants/doc/index.html"));
////                } catch (UnavailableServiceException e) {
////                    e.printStackTrace();
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
            }
        });
        helpMenu.add(helpMenuItem);

        this.add(fileMenu);
        this.add(editMenu);
        this.add(viewMenu);
        this.add(helpMenu);

    }

    /**
     * changes enabled status of menu items on entering/leaving edit mode
     * @param editMode
     */
    public void editDisplay(boolean editMode){
        for (JMenuItem i: editModeAvailableItems){
            if (editMode){
                i.setToolTipText(null);
            }else{
                i.setToolTipText("available in edit mode");
            }
            i.setEnabled(editMode);
            }
    }
}
