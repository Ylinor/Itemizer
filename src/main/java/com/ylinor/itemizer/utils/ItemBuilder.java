package com.ylinor.itemizer.utils;

import com.ylinor.itemizer.Itemizer;
import com.ylinor.itemizer.data.beans.AttributeBean;
import com.ylinor.itemizer.data.beans.ItemBean;
import com.ylinor.itemizer.data.beans.MinerBean;
import com.ylinor.itemizer.data.handlers.ConfigurationHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.BreakableData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.*;

public class ItemBuilder {
    /**
     * Build an itemstack from an ItemBean
     * @param itemBean Data of the item to build
     * @return Optional of the itemstack
     */
    public static Optional<ItemStack> buildItemStack(ItemBean itemBean) {
        Optional<ItemType> optionalType = Sponge.getRegistry().getType(ItemType.class, itemBean.getType());
        if (optionalType.isPresent()) {
            ItemStack itemStack = ItemStack.builder().itemType(optionalType.get()).build();
            ItemStack definedItemStack = ItemBuilder.defineItemStack(itemStack, itemBean);
            ItemStack enchantedItemStack = ItemBuilder.enchantItemStack(definedItemStack, itemBean);
            ItemStack miningItemStack = ItemBuilder.grantMining(enchantedItemStack, itemBean);
            ItemStack attributedItemStack = ItemBuilder.setAttribute(miningItemStack,itemBean);
            return Optional.ofNullable(attributedItemStack);
        } else {
            Itemizer.getLogger().warn("Unknown item type : " + itemBean.getType());
        }
        return Optional.empty();
    }
    /**
     * Build an itemstack from this name
     * @param name Data of the item to build
     * @return Optional of the itemstack
     */
    public static Optional<ItemStack> buildItemStack(String name) {
        Optional<ItemType> optionalType = Sponge.getRegistry().getType(ItemType.class,name);
        if (optionalType.isPresent()) {
            ItemStack itemStack = ItemStack.builder().itemType(optionalType.get()).build();
            return Optional.ofNullable(itemStack);
        } else {
            Itemizer.getLogger().warn("Unknown item type : " + name);
        }
        return Optional.empty();
    }

    /**
     * Define the characteristics of an ItemStack from an ItemBean
     * @param itemStack Item to edit
     * @param itemBean Data of the item to define
     * @return ItemStack edited
     */
    private static ItemStack defineItemStack(ItemStack itemStack, ItemBean itemBean) {
        // Item name
        if (itemBean.getName() != null && !itemBean.getName().isEmpty()) {
            itemStack.offer(Keys.DISPLAY_NAME, Text.of(itemBean.getName()));
        }
        // Item lore
        if (itemBean.getLore() != null) {
            List<Text> lore = new ArrayList<>();
            for (String loreLine : itemBean.getLore().split("\n")) {
                lore.add(Text.of(loreLine));
            }
            itemStack.offer(Keys.ITEM_LORE, lore);
        }
        // Item attributes
        itemStack.offer(Keys.UNBREAKABLE, itemBean.isUnbreakable());
        if(itemBean.getDurability()>0){
            itemStack.offer(Keys.ITEM_DURABILITY,itemBean.getDurability());
        }
        return itemStack;
    }

    /**
     * Enchant an ItemStack with an ItemBean data
     * @param itemStack Item to enchant
     * @param itemBean Data of the item to enchant
     * @return Enchanted (or not) ItemStack
     */
    private static ItemStack enchantItemStack(ItemStack itemStack, ItemBean itemBean) {
        Map<String, Integer> enchants = itemBean.getEnchants();
        if (!enchants.isEmpty()) {
            EnchantmentData enchantmentData = itemStack.getOrCreate(EnchantmentData.class).get();
            for (Map.Entry<String, Integer> enchant : enchants.entrySet()) {
                Optional<EnchantmentType> optionalEnchant = Sponge.getRegistry().getType(EnchantmentType.class, enchant.getKey());
                if (optionalEnchant.isPresent()) {
                    enchantmentData.set(enchantmentData.enchantments().add(Enchantment.builder().
                            type(optionalEnchant.get()).
                            level(enchant.getValue()).build()));
                } else {
                    Itemizer.getLogger().warn("Unknown enchant : " + enchant.getKey());
                }
            }
            itemStack.offer(enchantmentData);
        }
        return itemStack;
    }

    /**
     * Grant mining capabilities
     * @param itemStack Item to add mining capability
     * @param itemBean Data of the item
     * @return Item with mining powers
     */
    private static ItemStack grantMining(ItemStack itemStack, ItemBean itemBean) {
        BreakableData breakableData = itemStack.getOrCreate(BreakableData.class).get();
        List<MinerBean> minerList = ConfigurationHandler.getMinerList();
        for (int minerId : itemBean.getMiners()) {
            for (MinerBean minerBean : minerList) {
                if (minerBean.getId() == minerId) {
                    for (String blockType : minerBean.getMineTypes()) {
                        Optional<BlockType> optionalBlockType = Sponge.getRegistry().getType(BlockType.class, blockType);
                        if (optionalBlockType.isPresent()) {
                            breakableData.set(breakableData.breakable().add(optionalBlockType.get()));
                        }
                    }
                }
            }
        }
        itemStack.offer(breakableData);
        return itemStack;
    }

    private static DataContainer createAttributeModifier(AttributeBean attribute){

        UUID uuid = UUID.randomUUID();// UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
        DataContainer dataContainer = DataContainer.createNew();
        dataContainer.set(DataQuery.of("AttributeName"),attribute.getName());
        dataContainer.set(DataQuery.of("Name"),attribute.getName());
        dataContainer.set(DataQuery.of("Amount"),attribute.getAmount());
        dataContainer.set(DataQuery.of("Operation"),attribute.getOperation());
        dataContainer.set(DataQuery.of("Slot"),attribute.getSlot());
        dataContainer.set(DataQuery.of("UUIDMost"),uuid.getMostSignificantBits());
        dataContainer.set(DataQuery.of("UUIDLeast"),uuid.getLeastSignificantBits());
        return dataContainer;
    }

    private static ItemStack setAttribute(ItemStack itemStack, ItemBean itemBean){



        List<DataContainer> containers = new ArrayList();

        for(AttributeBean att : itemBean.getAttributeList()){
            DataContainer dc = createAttributeModifier(att);
            containers.add(dc);
        }

        DataContainer c = itemStack.toContainer();
        c.set(DataQuery.of("UnsafeData","AttributeModifiers"),containers);

        ItemStack modifiedItem = ItemStack.builder().fromContainer(c).build();

        return  modifiedItem;

    }







}
