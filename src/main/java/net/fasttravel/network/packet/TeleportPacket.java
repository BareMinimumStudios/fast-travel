//package net.fasttravel.network.packet;
//
//import net.fasttravel.FastTravelMain;
//import net.minecraft.util.Identifier;
//
//public record TeleportPacket(int entityId, float velocity) implements CustomPayload {
//
//    public static final CustomPayload.Id<TeleportPacket> PACKET_ID = new CustomPayload.Id<>(FastTravelMain.identifierOf("teleport_packet"));
//
//    public static final PacketCodec<RegistryByteBuf, TeleportPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
//        buf.writeInt(value.entityId);
//        buf.writeFloat(value.velocity);
//    }, buf -> new VelocityPacket(buf.readInt(), buf.readFloat()));
//
//    @Override
//    public Id<? extends CustomPayload> getId() {
//        return PACKET_ID;
//    }
//
//}
