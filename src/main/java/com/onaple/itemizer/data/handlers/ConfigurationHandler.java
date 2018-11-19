package com.onaple.itemizer.data.handlers;

import com.google.common.reflect.TypeToken;
import com.onaple.itemizer.GlobalConfig;
import com.onaple.itemizer.ICraftRecipes;
import com.onaple.itemizer.Itemizer;
import com.onaple.itemizer.data.beans.AttributeBean;
import com.onaple.itemizer.data.beans.ItemBean;
import com.onaple.itemizer.data.beans.MinerBean;
import com.onaple.itemizer.data.beans.PoolBean;
import com.onaple.itemizer.data.serializers.*;
import com.onaple.itemizer.utils.MinerUtil;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
@Singleton
public class ConfigurationHandler {
    public ConfigurationHandler() {}

    private  List<MinerBean> minerList = new ArrayList<>();
    public  List<MinerBean> getMinerList(){
        return minerList;
    }

    private  List<ItemBean> itemList= new ArrayList<>();
    public  List<ItemBean> getItemList(){
        return itemList;
    }

    private  List<PoolBean> poolList= new ArrayList<>();
    public  List<PoolBean> getPoolList(){
        return poolList;
    }

    private  List<ICraftRecipes> craftList= new ArrayList<>();
    public  List<ICraftRecipes> getCraftList(){
        return craftList;
    }



    /**
     * Read items configuration and interpret it
     * @param configurationNode ConfigurationNode to read from
     */
    public int readItemsConfiguration(CommentedConfigurationNode configurationNode) throws ObjectMappingException {
        itemList = new ArrayList<>();
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ItemBean.class), new ItemSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(AttributeBean.class), new AttributeSerializer());
        itemList = configurationNode.getNode("items").getList(TypeToken.of(ItemBean.class));
        Itemizer.getLogger().info(itemList.size() + " items loaded from configuration.");
        return itemList.size();
    }

    /**
     * Read miners configuration and interpret it
     * @param configurationNode ConfigurationNode to read from
     */
    public int readMinerConfiguration(CommentedConfigurationNode configurationNode) throws ObjectMappingException {
        minerList = new ArrayList<>();
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(MinerBean.class), new MinerSerializer());
        minerList = configurationNode.getNode("miners").getList(TypeToken.of(MinerBean.class));
        MinerUtil minerUtil = new MinerUtil(minerList);
        minerList = minerUtil.getExpandedMiners();
        Itemizer.getLogger().info(minerList.size() + " miners loaded from configuration.");
        return minerList.size();
    }

    /**
     * Read Craft configuration and interpret it
     * @param configurationNode ConfigurationNode to read from
     */
    public int readCraftConfiguration(CommentedConfigurationNode configurationNode) throws ObjectMappingException {
        craftList = new ArrayList<>();
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ICraftRecipes.class), new CraftingSerializer());
        craftList = configurationNode.getNode("crafts").getList(TypeToken.of(ICraftRecipes.class));
        Itemizer.getLogger().info( craftList.size() + " craft(s) loaded from configuration.");
        return craftList.size();
    }

    /**
     * Read pools configuration and interpret it. Must be the last config file read.
     * @param configurationNode ConfigurationNode to read from
     */
    public int readPoolsConfiguration(CommentedConfigurationNode configurationNode) throws ObjectMappingException {
        poolList = new ArrayList<>();
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PoolBean.class), new PoolSerializer());
        poolList = configurationNode.getNode("pools").getList(TypeToken.of(PoolBean.class));
        Itemizer.getLogger().info(poolList.size() + " pools loaded from configuration.");
        return poolList.size();
    }

    /**
     * Load configuration from file
     * @param configName Name of the configuration in the configuration folder
     * @return Configuration ready to be used
     */
    public CommentedConfigurationNode loadConfiguration(String configName) throws Exception {
        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(Paths.get(configName)).build();
        CommentedConfigurationNode configNode = null;
        try {
            configNode = configLoader.load();
        } catch (IOException e) {
            throw new Exception("Error while loading configuration '" + configName + "' : " + e.getMessage());
        }
        return configNode;
    }

    public GlobalConfig readGlobalConfiguration(CommentedConfigurationNode configurationNode) {
        boolean DescriptionRewrite = configurationNode.getNode("DescriptionRewrite").getBoolean();
        return new GlobalConfig(DescriptionRewrite);
    }

}
