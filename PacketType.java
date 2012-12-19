// org.timecrunch;

/* 'a --> b' implies 'b extends a'
 * IRoutable --> ISourceRoutable --> ISlickPackets
 *
 * ISlickPackets:
 * SLICK_PACKET
 * SLICK_PACKET_EXT
 *
 * ISourceRoutable:
 * SOURCE_ROUTED
 * PROBE_PACKET
 * PROBE_ACK (may change to ISlickPackets)
 *
 * IRoutable:
 * NO_TYPE
 * CONGESTION_STATE_UPDATE
 *
 * NOT IMPLEMENTED:
 * IP
 */

public enum PacketType {
	NO_TYPE,
	SOURCE_ROUTED,
	SLICK_PACKET,
	SLICK_PACKET_EXT,
	PROBE_PACKET,
	PROBE_ACK,
	CONGESTION_STATE_UPDATE,
	IP
}
