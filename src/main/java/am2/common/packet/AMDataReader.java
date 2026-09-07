package am2.common.packet;

import am2.common.LogHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AMDataReader {
    ByteArrayInputStream input;
    DataInputStream dataStream;
    public byte ID;

    public AMDataReader(byte[] data) {
        this(data, true);
    }

    public AMDataReader(byte[] data, boolean getID) {
        input = new ByteArrayInputStream(data);
        dataStream = new DataInputStream(input);

        //get id byte
        if (getID) {
            try {
                ID = dataStream.readByte();
            } catch (IOException e) {
                LogHelper.error("AMDataReader (getID): " + e.toString());
            }
        }
    }

    public int getInt() {
        int value = 0;
        try {
            value = dataStream.readInt();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getInt): " + e.toString());
        }
        return value;
    }

    public float getFloat() {
        float value = 0;
        try {
            value = dataStream.readFloat();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getFloat): " + e.toString());
        }
        return value;
    }

    public double getDouble() {
        double value = 0;
        try {
            value = dataStream.readDouble();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getDouble): " + e.toString());
        }
        return value;
    }

    public boolean getBoolean() {
        boolean value = false;
        try {
            value = dataStream.readBoolean();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getBoolean): " + e.toString());
        }
        return value;
    }

    public String getString() {
        String value = "";
        try {
            value = dataStream.readUTF();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getString): " + e.toString());
        }
        return value;
    }

    public byte getByte() {
        byte value = 0;
        try {
            value = dataStream.readByte();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getByte): " + e.toString());
        }
        return value;
    }

    public short getShort() {
        short value = 0;
        try {
            value = dataStream.readShort();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getShort): " + e.toString());
        }
        return value;
    }

    public long getLong() {
        long value = 0;
        try {
            value = dataStream.readLong();
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getLong): " + e.toString());
        }
        return value;
    }

    public NBTTagCompound getNBTTagCompound() {
        NBTTagCompound data = null;
        try {
            int len = dataStream.readInt();
            byte[] bytes = new byte[len];
            dataStream.read(bytes);
            ByteBuf buf = Unpooled.copiedBuffer(bytes);
            data = ByteBufUtils.readTag(buf);
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getNBTTagCompound): " + e.toString());
        }
        return data;
    }

    public byte[] getRemainingBytes() {
        byte[] remaining = null;
        try {
            remaining = new byte[dataStream.available()];
            dataStream.read(remaining);
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getRemainingBytes): " + e.toString());
        }

        return remaining;
    }

    public ItemStack getItemStack() {
        NBTTagCompound compound = getNBTTagCompound();
        if (compound == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack((compound));
        return stack;
    }

    public int[] getIntArray() {
        try {
            int[] arr = new int[dataStream.readInt()];
            for (int i = 0; i < arr.length; ++i)
                arr[i] = dataStream.readInt();
            return arr;
        } catch (IOException e) {
            LogHelper.error("AMDataReader (getIntArray): " + e.toString());
        }

        return new int[0];
    }
}
