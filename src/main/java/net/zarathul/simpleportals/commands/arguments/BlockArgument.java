package net.zarathul.simpleportals.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlockArgument implements ArgumentType<Block>
{
	private static final SimpleCommandExceptionType INVALID_ADDRESS = new SimpleCommandExceptionType(new TranslationTextComponent("commands.errors.address_argument"));
	private static final Collection<String> EXAMPLES = Arrays.asList(
		"minecraft:dirt",
		"minecraft:iron_block",
		"minecraft:white_wool");

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
		if (!reader.canRead()) throw INVALID_ADDRESS.createWithContext(reader);

		ResourceLocation loc = ResourceLocation.read(reader);
		if (!ForgeRegistries.BLOCKS.containsKey(loc)) throw INVALID_ADDRESS.createWithContext(reader);

		return ForgeRegistries.BLOCKS.getValue(loc);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		StringReader reader = new StringReader(builder.getInput());
		reader.setCursor(builder.getStart());
		reader.skipWhitespace();

		boolean doFilter = reader.getRemainingLength() > 1;
		List<String> list = Lists.newArrayList();

		if (doFilter)
		{
			String input;

			try
			{
				input = reader.readString();

				ForgeRegistries.BLOCKS.getKeys().stream()
					.sorted()
					.filter(resourceLocation -> resourceLocation.toString().startsWith(input))
					.forEachOrdered(resourceLocation -> list.add(resourceLocation.toString()));
			}
			catch (Exception ex)
			{
			}
		}
		else
		{
			ForgeRegistries.BLOCKS.getKeys().stream()
				.sorted()
				.forEachOrdered(resourceLocation -> list.add(resourceLocation.toString()));
		}

		return ISuggestionProvider.suggest(list, builder);
	}

	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
}
