package boogiepants.display;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.vecmath.Color3f;

import boogiepants.instruments.InstrumentManager;
import boogiepants.model.PelvicCircleInstrument;

public class DisplayColorsEditor extends JDialog {

    public static final int CANCELLED = 0;
    public static final int SUCCESS = 1;

 /*   public static int makeDialog(JFrame frame){
        JDialog d = createDialog(frame);
        
        int retval = ShapeEditor.CANCELLED;

        d.pack();
        d.show();
        return retval;
        JDialog dialog = new DisplayColorsEditor(frame);
        dialog.setVisible(true);
        return ShapeEditor.SUCCESS;
    }
*/
    /**
     * create dialog 
     * 
     * @param frame
     * @param instrument
     */
    public DisplayColorsEditor(JFrame frame){
        super(frame, "edit background/stick colors", true);
        Box box= Box.createVerticalBox();
        Box box2= Box.createHorizontalBox();
        JTabbedPane tabbedPane = new JTabbedPane();
        
        final JColorChooser colorChooser1 = addPanel("background", tabbedPane, InstrumentManager.getBackgroundColor());
        final JColorChooser colorChooser2 = addPanel("sticks", tabbedPane, InstrumentManager.getSticksColor());
        box.add(tabbedPane); 
        
        JButton create = new JButton("create");
        create.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Color c = colorChooser1.getColor();
                Color3f color1= new Color3f(c);
                c = colorChooser2.getColor();
                Color3f color2= new Color3f(c);

                DisplayColorsEditor.this.setVisible(false);
                DisplayColorsEditor.this.dispose();
            }
        });
        box2.add(create);
        
        JButton cancel = new JButton("cancel");
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                DisplayColorsEditor.this.setVisible(false);
                DisplayColorsEditor.this.dispose();
            }
        });
        box2.add(cancel);
        box.add(box2);
        this.add(box);
        this.pack();
    }

    /**
     * @param tabbedPane 
     * @param box
     * @return 
     */
    private JColorChooser addPanel(String name, JTabbedPane tabbedPane, Color3f cf) {
        JComponent panel;
        Color c;
        panel = new JPanel(false);
        tabbedPane.addTab(name, panel);
        Box box = Box.createVerticalBox();
        panel.add(box);
        c = new Color(cf.x, cf.y, cf.z);
        final JColorChooser colorChooser = new JColorChooser(c);
        box.add(colorChooser);
        
        return colorChooser;
    }
}
