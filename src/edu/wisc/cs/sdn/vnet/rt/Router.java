package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
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
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		// type check
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) {
			return;
		}

		IPv4 payload = (IPv4) etherPacket.getPayload();

		// checksum check
		short precheck = payload.getChecksum();
		payload.resetChecksum();
		payload.serialize();
		short postcheck = payload.getChecksum();
		if (precheck != postcheck) {
			System.out.println("checksum error");
			return;
		}
		
		// decrement ttl, plus ttl check
	 	payload.setTtl((byte) (payload.getTtl() - 1));
		if (payload.getTtl() == 0) {
			System.out.println("ttl error");
			return;
		}
		payload.resetChecksum();
		payload.serialize();

		// check if existing interface
		for (Iface i : this.interfaces.values()) {
			if (i.getIpAddress() == payload.getDestinationAddress()) {
				System.out.println("iface error");
				return;
			}
		}

		// collect/check if existing route entry
		RouteEntry route = routeTable.lookup(payload.getDestinationAddress());
		if (route == null) {
			System.out.println("rtable error");
			return;
		}

		byte[] newDest;
		if (route.getGatewayAddress() != 0) {
			newDest = (arpCache.lookup(route.getGatewayAddress()).getMac()).toBytes();
		} else {
			newDest = (arpCache.lookup(payload.getDestinationAddress()).getMac()).toBytes();
		}

		if (newDest == null) {
			System.out.println("destination error!");
			return;
		}

		// update payload, update source and destination mac addresses, and send!

		etherPacket.setSourceMACAddress((route.getInterface().getMacAddress()).toBytes());	
		// System.out.println(etherPacket.getSourceMACAddress());

		etherPacket.setDestinationMACAddress(newDest);
		// System.out.println(etherPacket.getDestinationMACAddress());

		this.sendPacket(etherPacket, route.getInterface());
		/********************************************************************/
	}
}
