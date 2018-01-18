package com.ylinor.itemizer;
import com.ylinor.itemizer.commands.FetchCommand;
import com.ylinor.itemizer.commands.RetrieveCommand;
import com.ylinor.itemizer.data.handlers.ConfigurationHandler;
import com.ylinor.itemizer.utils.CraftingRegister;
import com.ylinor.itemizer.service.ItemService;
import com.ylinor.itemizer.service.IItemService;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import javax.inject.Inject;
import java.nio.file.Path;

@Plugin(id = "itemizer", name = "Itemizer", version = "0.0.1")
public class Itemizer {

	@Inject
	@ConfigDir(sharedRoot=true)
	private Path configDir;

	private static Logger logger;
	@Inject
	private void setLogger(Logger logger) {
		this.logger = logger;
	}
	public static Logger getLogger() {
		return logger;
	}
	@Listener
	public void preInit(GamePreInitializationEvent event) {
		CraftingRecipeRegister craftingRecipe = new CraftingRecipeRegister();
		craftingRecipe.setContent(ItemStack.of(ItemTypes.DIAMOND_ORE,1));
		ItemStackSnapshot itemStackSnapshot = ItemTypes.WOODEN_SWORD.getTemplate();
		logger.info(itemStackSnapshot.getValue(Keys.ATTACK_DAMAGE).get().toString());
		craftingRecipe.setResult(ItemStack.of(ItemTypes.DIAMOND,1));
		Sponge.getGame().getRegistry().getCraftingRecipeRegistry().register((CraftingRecipe) craftingRecipe.register());

	}

	@Listener
	public void onGamePreInitialization(GamePreInitializationEvent event) {
		logger.info("Initalisation");
		Sponge.getServiceManager().setProvider(getInstance(), IItemService.class,new ItemService());
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) throws Exception {
		ConfigurationHandler.readItemsConfiguration(ConfigurationHandler.loadConfiguration(configDir+"/itemizer_items.conf"));
		ConfigurationHandler.readMinerConfiguration(ConfigurationHandler.loadConfiguration(configDir+"/itemizer_miners.conf"));
		ConfigurationHandler.readPoolsConfiguration(ConfigurationHandler.loadConfiguration(configDir+"/itemizer_pools.conf"));

		CommandSpec retrieve = CommandSpec.builder()
				.description(Text.of("Retrieve an item from a configuration file with its id."))
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("id"))))
				.executor(new RetrieveCommand()).build();
		Sponge.getCommandManager().register(this, retrieve, "retrieve");

		CommandSpec fetch = CommandSpec.builder()
				.description(Text.of("Try to retrieve an item from a pool describes in a configuration file with its id."))
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("id"))))
				.executor(new FetchCommand()).build();
		Sponge.getCommandManager().register(this, fetch, "fetch");

		logger.info("ITEMIZER initialized.");

	}
	public static PluginContainer getInstance(){
		return  Sponge.getPluginManager().getPlugin("itemizer").get();
	}

}