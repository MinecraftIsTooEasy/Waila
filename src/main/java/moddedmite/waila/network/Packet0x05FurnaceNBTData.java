package moddedmite.waila.network;

import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.ResourceLocation;

public class Packet0x05FurnaceNBTData implements Packet {
    private final ItemStack input;
    private final ItemStack fuel;
    private final ItemStack output;
    private final int cookProgress;


    public Packet0x05FurnaceNBTData(ItemStack input, ItemStack fuel, ItemStack output, int cookProgress) {
        this.input = input;
        this.fuel = fuel;
        this.output = output;
        this.cookProgress = cookProgress;
    }

    public Packet0x05FurnaceNBTData(PacketByteBuf packetByteBuf) {
        this(packetByteBuf.readItemStack(), packetByteBuf.readItemStack(), packetByteBuf.readItemStack(), packetByteBuf.readInt());
    }

    @Override
    public void write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeItemStack(this.input);
        packetByteBuf.writeItemStack(this.fuel);
        packetByteBuf.writeItemStack(this.output);
        packetByteBuf.writeInt(this.cookProgress);
    }

    @Override
    public void apply(EntityPlayer entityPlayer) {

    }

    @Override
    public ResourceLocation getChannel() {
        return WailaPackets.FurnaceData;
    }

    public ItemStack getInput() {
        return this.input;
    }

    public ItemStack getFuel() {
        return this.fuel;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public int getCookProgress() {
        return this.cookProgress;
    }
}
