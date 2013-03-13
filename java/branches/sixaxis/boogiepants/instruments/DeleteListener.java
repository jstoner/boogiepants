package boogiepants.instruments;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import boogiepants.display.KnowsSelected;

public class DeleteListener implements KeyListener {

    KnowsSelected knowsSelected;
    
    public DeleteListener(KnowsSelected knows) {
        knowsSelected = knows;
   }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        knowsSelected.delete();
    }

}
