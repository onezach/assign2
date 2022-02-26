package edu.wisc.cs.sdn.vnet.sw;

import java.util.ArrayList;

public class SwitchTableTimer implements Runnable {
    private ArrayList<SwitchNode> switchTable;

    public SwitchTableTimer(ArrayList<SwitchNode> switchTable) {
        this.switchTable = switchTable;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            for (SwitchNode node : this.switchTable) {
                if (System.currentTimeMillis() - node.getTimeCreated() > 15000) {
                    switchTable.remove(node);
                }
            }
            
        }
        
    }
    
}
