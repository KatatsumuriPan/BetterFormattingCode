package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.acc.ACC_TileEntityFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class HK_TileEntityFurnace {

	public static int getItemBurnTime(ItemStack stack) {
		int burnTime = TileEntityFurnace.getItemBurnTime(stack);
		if (burnTime <= 0)
			return burnTime;
		return Math.max(burnTime / 8, 1);
	}

	public static String getName(TileEntityFurnace self) {
		return "furnace:" + (((ACC_TileEntityFurnace) self).get_cookTime());
	}
}
