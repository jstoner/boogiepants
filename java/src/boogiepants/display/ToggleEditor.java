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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.vecmath.Color3f;

import javax.swing.JDialog;

import boogiepants.model.ToggleInstrument;

/**
 * Let user define settings for toggle instrument
 * 
 * @author jstoner
 *
 */
public class ToggleEditor extends JDialog {

    public static final int CANCELLED = 0;
    public static final int SUCCESS = 1;

    public static int makeDialog(JFrame frame, final ToggleInstrument instrument){
        JDialog dialog = new ToggleEditor(frame, instrument);
        dialog.setVisible(true);
        
        if (instrument.getOSCAddress()==null){
            return ShapeEditor.CANCELLED;
        }else{
            return ShapeEditor.SUCCESS;
        }
    }

    /**
     * create dialog 
     * 
     * @param frame
     * @param instrument
     */
    public ToggleEditor(JFrame frame, final ToggleInstrument instrument){
        super(frame, "edit toggle", true);
        Box box = Box.createVerticalBox();
        Box box2= Box.createHorizontalBox();
        this.add(box);
        final JColorChooser colorChooser = new JColorChooser(Color.GRAY);
        box.add(colorChooser);
        
        final JLabel label = new JLabel("OSC address");
        label.setFocusable(false);
        box.add(label);
        
        final JTextField oscAddr = new JTextField();
        box.add(oscAddr);
        
        JButton create = new JButton("create");
        create.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String osc= oscAddr.getText();
                if(oscAddr.getText()==null){
                    JOptionPane.showMessageDialog(ToggleEditor.this, "please enter an OSC address", 
                            "error", JOptionPane.ERROR_MESSAGE);
                }else{
                    Color c = colorChooser.getColor();
                    Color3f color= new Color3f(c);
                    instrument.setOSCAddress(osc);
                    instrument.setColor(color);
                    ToggleEditor.this.setVisible(false);
                    ToggleEditor.this.dispose();
                }
            }
        });
        box2.add(create);
        
        JButton cancel = new JButton("cancel");
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ToggleEditor.this.setVisible(false);
                ToggleEditor.this.dispose();
            }
        });
        box2.add(cancel);
        box.add(box2);
        this.pack();
    }

}
