package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;

import java.util.ArrayList;
import java.util.Arrays;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

/**
 * @author Aaron Gember-Jacobson
 */


class SwitchNode {
	private byte[] _MACaddr;
	private Iface _interface;
	private int secondsLeft;

	public SwitchNode(byte[] _MACaddr, Iface _interface, int secondsLeft) {
		this._MACaddr = _MACaddr;
		this._interface = _interface;
		this.secondsLeft = secondsLeft;
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

public class Switch extends Device
{	
	private ArrayList<SwitchNode> switchTable; //This is the Switch Table
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);

	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
				etherPacket.toString().replace("\n", "\n\t"));
		
		MACAddress MACin = etherPacket.getSourceMAC();
		MACAddress MACout = etherPacket.getDestinationMAC();
		byte[] MACinBytes = MACin.toBytes();
		byte[] MACoutBytes = MACout.toBytes();
		SwitchNode table_node_in = new SwitchNode(MACinBytes, inIface, 15);

		// check if the table is empty, if so add source address to table
		if (!switchTable.contains(table_node_in)) 
			switchTable.add(table_node_in);

		// check if the table contains the destination adress
		if(!switchTable.contains(new SwitchNode(MACoutBytes, null, 0))) {
			// flood, find end host, add to table
		}
		// destination host is in table, send packet

		
		
	}
}
