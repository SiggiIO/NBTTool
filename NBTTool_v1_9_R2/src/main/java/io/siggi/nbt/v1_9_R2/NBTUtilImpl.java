/*
 * The MIT License
 *
 * Copyright 2017 Siggi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.siggi.nbt.v1_9_R2;

import com.mojang.authlib.GameProfile;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTList;
import io.siggi.nbt.util.NBTUtil;
import net.minecraft.server.v1_9_R2.DataConverterManager;
import net.minecraft.server.v1_9_R2.DataConverterType;
import net.minecraft.server.v1_9_R2.DataConverterTypes;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.Item;
import net.minecraft.server.v1_9_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftSkull;
import org.bukkit.craftbukkit.v1_9_R2.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

final class NBTUtilImpl extends NBTUtil {

	@Override
	public NBTCompound newCompound() {
		return new NBTCompoundImpl();
	}

	@Override
	public NBTList newList() {
		return new NBTListImpl();
	}

	@Override
	public <T> NBTCompound wrapCompound(T compound) {
		return new NBTCompoundImpl((NBTTagCompound) compound);
	}

	@Override
	public <T> NBTList wrapList(T list) {
		return new NBTListImpl((NBTTagList) list);
	}

	@Override
	public NBTCompound getTag(ItemStack stack) {
		net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
		if (nmsStack.hasTag()) {
			return wrapCompound(nmsStack.getTag());
		} else {
			return null;
		}
	}

	@Override
	public ItemStack setTag(ItemStack stack, NBTCompound compound) {
		net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
		if (compound == null) {
			nmsStack.setTag(null);
		} else {
			nmsStack.setTag(compound.getNMSCompound());
		}
		return CraftItemStack.asCraftMirror(nmsStack);
	}

	@Override
	public NBTCompound itemToNBT(ItemStack stack) {
		try {
			net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
			NBTTagCompound nmsCompound = new NBTTagCompound();
			nmsStack.save(nmsCompound);
			NBTCompound compound = new NBTCompoundImpl(nmsCompound);
			compound.setInt("DataVersion", getDataVersion());
			return compound;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public ItemStack itemFromNBT(NBTCompound compound) {
		if (compound == null) {
			return null;
		}
		try {
			int ver = compound.getInt("DataVersion");
			DataConverterManager dataConverterManager = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getDataConverterManager();
			NBTCompound tempItem = compound.copy();
			tempItem.remove("DataVersion");
			NBTTagCompound nmsCompound = dataConverterManager.a((DataConverterType) DataConverterTypes.ITEM_INSTANCE, tempItem.getNMSCompound(), ver);
			net.minecraft.server.v1_9_R2.ItemStack nmsStack = net.minecraft.server.v1_9_R2.ItemStack.createStack(nmsCompound);
			return CraftItemStack.asCraftMirror(nmsStack);
		} catch (Exception e) {
			return null;
		}
	}

	private int dataVersion = -1;

	private int getDataVersion() {
		if (dataVersion == -1) {
			try {
				DataConverterManager dataConverterManager = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getDataConverterManager();
				Field f = DataConverterManager.class.getDeclaredField("d");
				f.setAccessible(true);
				dataVersion = f.getInt(dataConverterManager);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
				ex.printStackTrace();
			}
		}
		return dataVersion;
	}

	@Override
	public Class<NBTCompound> getCompoundClass() {
		return NBTCompound.class;
	}

	@Override
	public Class<NBTList> getListClass() {
		return NBTList.class;
	}

	@Override
	public Class<CraftItemStack> getCraftItemStack() {
		return CraftItemStack.class;
	}
	
	@Override
	public void setAI(Entity entity, boolean ai) {
		net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		if (nmsEntity instanceof EntityInsentient) {
			EntityInsentient entityInsentient = (EntityInsentient) nmsEntity;
			entityInsentient.setAI(!ai); // the definition is reversed for some reason
		}
	}
	
	@Override
	public boolean hasAI(Entity entity) {
		net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		if (nmsEntity instanceof EntityInsentient) {
			EntityInsentient entityInsentient = (EntityInsentient) nmsEntity;
			return !entityInsentient.hasAI(); // the definition is reversed for some reason
		} else {
			return false;
		}
	}

	@Override
	public String getItemName(ItemStack stack) {
		net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound nbttc = new NBTTagCompound();
		nmsStack.save(nbttc);
		NBTCompound compound = wrapCompound(nbttc);
		String id = compound.getString("id");
		Item item = Item.d(id);
		if (item == null) {
			return "null";
		} else {
			return item.a(nmsStack);
		}
	}

	@Override
	public String getEnchantmentName(Enchantment enchantment, int level) {
		net.minecraft.server.v1_9_R2.Enchantment raw = CraftEnchantment.getRaw(enchantment);
		return raw.d(level);
	}

	@Override
	public void setGameProfile(Skull skull, GameProfile profile) {
		try {
			Field profileField = CraftSkull.class.getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(skull, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public void serialize(OutputStream out, NBTCompound compound) throws IOException {
		DataOutputStream dataOut = out instanceof DataOutputStream ? ((DataOutputStream) out) : new DataOutputStream(out);
		NBTCompressedStreamTools.a(compound.getNMSCompound(), (DataOutput) dataOut);
	}

	@Override
	public NBTCompound deserialize(InputStream in) throws IOException {
		DataInputStream dataIn = in instanceof DataInputStream ? ((DataInputStream) in) : new DataInputStream(in);
		return wrapCompound(NBTCompressedStreamTools.a(dataIn));
	}
}