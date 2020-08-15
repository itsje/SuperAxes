package com.github.levoment.superaxes.Items;

import com.github.levoment.superaxes.SuperAxesMaterialGenerator;
import com.github.levoment.superaxes.TreeChopper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.List;

public class SuperAxeItem extends AxeItem {

    private boolean mineLeaves = false;

    // Constructor
    public SuperAxeItem(ToolMaterial material, Settings settings) {
        super(material, ((SuperAxesMaterialGenerator)material).getAxeAttackDamage(), ((SuperAxesMaterialGenerator) material).getAxeAttackSpeed(), settings);
    }


    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        if(!world.isClient()) {
            // Check if the player is sneaking. If Sneaking, mine as normal
            if (miner.isSneaking()) return super.canMine(state, world, pos, miner);
            // Check if the tool is effective on the block and check for the LOGS tag
            if (state.isIn(BlockTags.LOGS)) {
                // Create an instance of TreeChopper
                TreeChopper treeChopper = new TreeChopper();
                // Create a new thread for chopping the tree
                new Thread(() -> {treeChopper.cutTree(state, world, pos, miner, miner.getMainHandStack());}).start();
            }
        }
        return true;
    }

    public void mineLeaves(BlockState leafBlockState, ServerWorld serverWorld, BlockPos pos, PlayerEntity miner, boolean firstBlockBroken)
    {
        if (leafBlockState.isIn(BlockTags.LEAVES))
        {
            // Set the loot context for mining the leaf block
            LootContext.Builder builder = (new LootContext.Builder(serverWorld)).random(serverWorld.random).luck(miner.getLuck()).optionalParameter(LootContextParameters.POSITION, pos).optionalParameter(LootContextParameters.TOOL, miner.getMainHandStack()).optionalParameter(LootContextParameters.THIS_ENTITY, miner);
            // Get a list of drops if the tool is used to harvest the block
            List<ItemStack> listOfDroppedStacks = leafBlockState.getDroppedStacks(builder);
            listOfDroppedStacks.forEach(itemStack -> {
                // Drop the item on the world
                ItemScatterer.spawn(serverWorld, pos.getX(), pos.getY(), pos.getZ(), itemStack);
            });
            if (miner.getMainHandStack().getItem() instanceof SuperAxeItem) {
                System.out.println("SuperAxeItem");
                // Damage superaxe for each block that is broken
                if (firstBlockBroken) {
                    System.out.println("Damaging item");
                    miner.getMainHandStack().postMine(serverWorld, serverWorld.getBlockState(pos), pos, miner);
                    // Break the block
                    serverWorld.breakBlock(pos, false, miner);
                }
            }
        }
    }

    public void mineBlockWithLootContext(BlockState leafBlockState, ServerWorld serverWorld, BlockPos pos, PlayerEntity miner, boolean firstBlockBroken)
    {
            // Set the loot context for mining the leaf block
            LootContext.Builder builder = (new LootContext.Builder(serverWorld)).random(serverWorld.random).luck(miner.getLuck()).optionalParameter(LootContextParameters.POSITION, pos).optionalParameter(LootContextParameters.TOOL, miner.getMainHandStack()).optionalParameter(LootContextParameters.THIS_ENTITY, miner);
            // Get a list of drops if the tool is used to harvest the block
            List<ItemStack> listOfDroppedStacks = leafBlockState.getDroppedStacks(builder);
            listOfDroppedStacks.forEach(itemStack -> {
                // Drop the item on the world
                ItemScatterer.spawn(serverWorld, pos.getX(), pos.getY(), pos.getZ(), itemStack);
            });

        if (miner.getMainHandStack().getItem() instanceof SuperAxeItem) {
            // Damage superaxe for each block that is broken
            if (firstBlockBroken) {
                miner.getMainHandStack().postMine(serverWorld, serverWorld.getBlockState(pos), pos, miner);
                // Break the block
                serverWorld.breakBlock(pos, false, miner);
            }
        }

    }
}
