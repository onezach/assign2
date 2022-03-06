package edu.wisc.cs.sdn.vnet.sw;

import java.util.Arrays;
import edu.wisc.cs.sdn.vnet.Iface;

public class SwitchNode {
	private byte[] _MACaddr;
	private Iface _interface;
	private long timeCreated;

	public SwitchNode(byte[] _MACaddr, Iface _interface, long timeCreated) {
		this._MACaddr = _MACaddr;
		this._interface = _interface;
		this.timeCreated = timeCreated;
	}

	public Iface getIface() {
		return this._interface;
	}

    public long getTimeCreated() {
        return this.timeCreated;
    }

	public byte[] get_MACaddr() {
		return this._MACaddr;
	}

	@Override
    public boolean equals(Object o) {
 
        if (o == this) {
            return true;
        }
        if (!(o instanceof SwitchNode)) {
            return false;
        }
        SwitchNode swn = (SwitchNode) o;
         
        return Arrays.equals(this._MACaddr, swn._MACaddr);
	}
}
