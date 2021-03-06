package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;

import java.util.ArrayList;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

/**
 * @author Aaron Gember-Jacobson
 */

public class Switch extends Device
{	
	private ArrayList<SwitchNode> switchTable; //This is the Switch Table
	private Thread switchTableTimer;

	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.switchTable = new ArrayList<SwitchNode>(20); // guessing no more than 20 devcies, should check
		this.switchTableTimer = new Thread(new SwitchTableTimer(this.switchTable));
		switchTableTimer.start();

	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) {
			return;
		}

		System.out.println("*** -> Received packet: " +
				etherPacket.toString().replace("\n", "\n\t"));
		
		MACAddress MACin = etherPacket.getSourceMAC();
		MACAddress MACout = etherPacket.getDestinationMAC();
		byte[] MACinBytes = MACin.toBytes();
		byte[] MACoutBytes = MACout.toBytes();
		
		SwitchNode table_node_in = new SwitchNode(MACinBytes, inIface, System.currentTimeMillis());

		// if reciving MAC already exists delete it
		// add new entry
		synchronized(switchTable)
		{
		if (switchTable.contains(table_node_in)) {
			//System.out.println("Mac already in table");
			switchTable.remove(table_node_in);
		}
		switchTable.add(table_node_in);
		//System.out.println("added " + table_node_in.getIface().getName());
		

		// check if the table contains the destination adress
		if(!switchTable.contains(new SwitchNode(MACoutBytes, null, 0))) {
			// flood
			//System.out.println("flooding");
			for (Iface interFace : interfaces.values()) {
				if (!interFace.getName().equals(table_node_in.getIface().getName())) {
					//System.out.println("sending to " + interFace.getName());
					this.sendPacket(etherPacket, interFace);
					
				}
			}
			return;

		}
		else {
			// find out Interface in table and send
			//System.out.println("MAC found, sending packet");
			SwitchNode outNode = switchTable.get(switchTable.indexOf(new SwitchNode(MACoutBytes, null, 0)));
			this.sendPacket(etherPacket, outNode.getIface());
		}
	}
	}
}

/*
			if (System.currentTimeMillis() - outNode.getTimeCreated() > 15000) {
				switchTable.remove(outNode);
				// flood
				for (SwitchNode node : switchTable) {
					this.sendPacket(etherPacket, node.getIface());
				}
				return;
			}
			*/