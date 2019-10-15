package net.zarathul.simpleportals.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class BlockArgument implements ArgumentType<Block>
{
	private static final DynamicCommandExceptionType INVALID_ADDRESS = new DynamicCommandExceptionType((args) -> new TranslationTextComponent("commands.errors.block_argument", args));
	private static final Collection<String> EXAMPLES = Arrays.asList(
		"minecraft:dirt",
		"minecraft:iron_block",
		"minecraft:white_wool");

	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestionFuture;

	public static BlockArgument block()
	{
		return new BlockArgument();
	}

	public static Block getBlock(CommandContext<CommandSource> context, String name)
	{
		return context.getArgument(name, Block.class);
	}

	@Override
	public Block parse(StringReader reader) throws CommandSyntaxException
	{
		suggestionFuture = (builder) -> ISuggestionProvider.suggestIterable(ForgeRegistries.BLOCKS.getKeys(), builder);

		int i = reader.getCursor();
		ResourceLocation blockResourceLocation = ResourceLocation.read(reader);

		if (!ForgeRegistries.BLOCKS.containsKey(blockResourceLocation))
		{
			reader.setCursor(i);
			throw INVALID_ADDRESS.createWithContext(reader, blockResourceLocation);
		}

		suggestionFuture = (builder) -> builder.buildFuture();

		return ForgeRegistries.BLOCKS.getValue(blockResourceLocation);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		StringReader reader = new StringReader(builder.getInput());
		reader.setCursor(builder.getStart());

		try
		{
			parse(reader);
		}
		catch (CommandSyntaxException ex) {}

		return this.suggestionFuture.apply(builder.createOffset(reader.getCursor()));
	}

	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
}
